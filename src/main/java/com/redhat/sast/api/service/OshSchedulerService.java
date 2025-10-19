package com.redhat.sast.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redhat.sast.api.config.OshConfiguration;
import com.redhat.sast.api.config.OshRetryConfiguration;
import com.redhat.sast.api.enums.OshFailureReason;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.OshSchedulerCursor;
import com.redhat.sast.api.model.OshUncollectedScan;
import com.redhat.sast.api.repository.OshSchedulerCursorRepository;
import com.redhat.sast.api.v1.dto.osh.OshScanResponse;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler service for automated OSH (Open Scan Hub) scan discovery and job creation.
 *
 * This service implements a two-phase automation loop:
 *
 * PHASE 1 - Incremental Scan Processing:
 * 1. Polls OSH for new completed scans at regular intervals
 * 2. Filters scans by configured package list
 * 3. Creates SAST-AI workflow jobs for eligible scans
 * 4. Maintains cursor state for incremental polling
 * 5. Records failures for retry processing
 *
 * PHASE 2 - Retry Processing (if retry enabled):
 * 1. Fetches failed scans from retry queue
 * 2. Attempts retry processing with backoff timing
 * 3. Removes successful retries from queue
 * 4. Updates retry attempt counters for failures
 *
 * Both phases handle failures gracefully without stopping the scheduler.
 */
@ApplicationScoped
@Slf4j
public class OshSchedulerService {

    @Inject
    OshClientService oshClientService;

    @Inject
    OshJobCreationService oshJobCreationService;

    @Inject
    OshSchedulerCursorRepository cursorRepository;

    @Inject
    OshConfiguration oshConfiguration;

    @Inject
    OshRetryService oshRetryService;

    @Inject
    OshRetryConfiguration retryConfiguration;

    /**
     * Main scheduler method that polls OSH for new scans and creates jobs.
     *
     * Execution pattern (two-phase):
     * PHASE 1 - Incremental processing:
     * 1. Check if OSH integration is enabled
     * 2. Determine starting scan ID from cursor
     * 3. Fetch new finished scans from OSH
     * 4. Process each scan individually in separate transactions
     * 5. Update cursor position after successful batch processing
     *
     * PHASE 2 - Retry processing (if retry enabled):
     * 1. Fetch failed scans from retry queue
     * 2. Process retry scans with backoff enforcement
     * 3. Update retry attempt counters and cleanup successes
     *
     * Transaction boundary: NEVER - scheduler must not participate in transactions
     * Concurrency: SKIP - prevents overlapping executions if one takes too long
     */
    @Scheduled(every = "${osh.poll.interval:5m}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    @Transactional(Transactional.TxType.NEVER)
    public void pollOshForNewScans() {
        if (!oshConfiguration.isEnabled()) {
            LOGGER.trace("OSH integration disabled, skipping poll cycle");
            return;
        }

        try {
            LOGGER.debug("Starting OSH two-phase polling cycle");

            ProcessingResults incrementalResults = processIncrementalScans();

            ProcessingResults retryResults = processRetryScans();

            logCombinedResults(incrementalResults, retryResults);

        } catch (Exception e) {
            LOGGER.error("OSH polling cycle failed, will retry next scheduled interval", e);
        }
    }

    /**
     * PHASE 1: Process incremental scans.
     * Handles new scans from OSH with cursor advancement.
     *
     * @return processing results for incremental scans
     */
    private ProcessingResults processIncrementalScans() {
        try {
            LOGGER.debug("Starting incremental scan processing");

            PollConfiguration config = preparePollConfiguration();
            List<OshScanResponse> finishedScans = fetchFinishedScans(config);

            if (finishedScans.isEmpty()) {
                handleEmptyBatch(config);
                return new ProcessingResults(0, 0, 0, "incremental");
            }

            LOGGER.debug(
                    "Found {} finished scans in range {}-{}",
                    finishedScans.size(),
                    config.startScanId(),
                    config.startScanId() + config.batchSize() - 1);

            ProcessingResults results = processScans(finishedScans, "incremental");
            updateCursorInNewTransaction(config.startScanId() + config.batchSize());

            LOGGER.debug(
                    "Incremental scan processing completed: {} processed, {} skipped, {} failed",
                    results.processedCount(),
                    results.skippedCount(),
                    results.failedCount());

            return results;

        } catch (Exception e) {
            LOGGER.error("Incremental scan processing failed: {}", e.getMessage(), e);
            return new ProcessingResults(0, 0, 0, "incremental");
        }
    }

    /**
     * PHASE 2: Process retry scans.
     * Handles failed scans from the retry queue with backoff enforcement.
     *
     * @return processing results for retry scans
     */
    private ProcessingResults processRetryScans() {
        if (!retryConfiguration.isRetryEnabled()) {
            LOGGER.trace("Retry disabled, skipping retry phase");
            return new ProcessingResults(0, 0, 0, "retry");
        }

        try {
            LOGGER.debug("Starting retry scan processing");

            List<OshUncollectedScan> retryScans =
                    oshRetryService.fetchRetryableScans(retryConfiguration.getEffectiveRetryBatchSize());

            if (retryScans.isEmpty()) {
                LOGGER.debug("No retry-eligible scans found");
                return new ProcessingResults(0, 0, 0, "retry");
            }

            LOGGER.debug("Processing {} retry-eligible scans", retryScans.size());

            ProcessingResults results = processRetryScans(retryScans);

            LOGGER.debug(
                    "Retry scan processing completed: {} processed, {} skipped, {} failed",
                    results.processedCount(),
                    results.skippedCount(),
                    results.failedCount());

            return results;

        } catch (Exception e) {
            LOGGER.error("Retry scan processing failed: {}", e.getMessage(), e);
            return new ProcessingResults(0, 0, 0, "retry");
        }
    }

    /**
     * Processes a list of retry scans from the uncollected scan queue.
     *
     * @param retryScans list of uncollected scans to retry
     * @return processing results for the retry batch
     */
    private ProcessingResults processRetryScans(List<OshUncollectedScan> retryScans) {
        int processedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (OshUncollectedScan uncollectedScan : retryScans) {
            try {
                OshScanResponse scan = oshRetryService.reconstructScanFromJson(uncollectedScan.getScanDataJson());

                ProcessingResult result = processSingleRetryScan(uncollectedScan, scan);

                switch (result) {
                    case PROCESSED -> processedCount++;
                    case SKIPPED -> skippedCount++;
                    case FAILED -> failedCount++;
                }

            } catch (Exception e) {
                failedCount++;
                LOGGER.error("Failed to process retry scan {}: {}", uncollectedScan.getOshScanId(), e.getMessage(), e);

                OshFailureReason failureReason = classifyFailure(e);
                oshRetryService.recordRetryAttempt(uncollectedScan.getId(), failureReason, e.getMessage());
            }
        }

        return new ProcessingResults(processedCount, skippedCount, failedCount, "retry");
    }

    /**
     * Processes a single retry scan with attempt tracking.
     *
     * @param uncollectedScan the uncollected scan record
     * @param scan the reconstructed OSH scan response
     * @return processing result
     */
    private ProcessingResult processSingleRetryScan(OshUncollectedScan uncollectedScan, OshScanResponse scan) {
        try {
            Optional<Job> createdJob = oshJobCreationService.createJobFromOshScan(scan);

            if (createdJob.isPresent()) {
                oshRetryService.markRetrySuccessful(scan.getScanId());

                LOGGER.info(
                        "Retry successful: OSH scan {} -> job {} (attempt {})",
                        scan.getScanId(),
                        createdJob.get().getId(),
                        uncollectedScan.getAttemptCount() + 1);

                return ProcessingResult.PROCESSED;

            } else {
                oshRetryService.markRetrySuccessful(scan.getScanId());

                LOGGER.debug("Retry scan {} skipped (already processed or no JSON)", scan.getScanId());
                return ProcessingResult.SKIPPED;
            }

        } catch (Exception e) {
            LOGGER.warn(
                    "Retry attempt failed for scan {} (attempt {}): {}",
                    scan.getScanId(),
                    uncollectedScan.getAttemptCount() + 1,
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Classifies an exception into a failure reason for retry tracking.
     *
     * @param exception the exception that occurred
     * @return appropriate failure reason
     */
    private OshFailureReason classifyFailure(Exception exception) {
        if (exception == null) {
            return OshFailureReason.UNKNOWN_ERROR;
        }

        String message = exception.getMessage() != null ? exception.getMessage().toLowerCase() : "";

        // Network/IO failures
        if (exception instanceof java.net.ConnectException
                || exception instanceof java.net.SocketTimeoutException
                || exception instanceof java.io.IOException
                || message.contains("connection")
                || message.contains("timeout")) {
            return OshFailureReason.JSON_DOWNLOAD_NETWORK_ERROR;
        }

        // JSON parsing failures
        if (exception instanceof JsonProcessingException || message.contains("json") || message.contains("parsing")) {
            return OshFailureReason.JSON_DOWNLOAD_HTTP_ERROR;
        }

        // Data validation failures
        if (exception instanceof IllegalArgumentException
                || exception instanceof IllegalStateException
                || message.contains("invalid")
                || message.contains("missing")) {
            return OshFailureReason.SCAN_DATA_ERROR;
        }

        // Database failures
        if (exception instanceof jakarta.persistence.PersistenceException
                || message.contains("database")
                || message.contains("constraint")) {
            return OshFailureReason.DATABASE_ERROR;
        }

        // API failures
        if (message.contains("http") || message.contains("api") || message.contains("osh")) {
            return OshFailureReason.OSH_API_ERROR;
        }

        // Job creation failures
        if (message.contains("job") || message.contains("creation")) {
            return OshFailureReason.JOB_CREATION_ERROR;
        }

        // Default to unknown
        return OshFailureReason.UNKNOWN_ERROR;
    }

    /**
     * Logs combined results from both incremental and retry phases.
     *
     * @param incrementalResults results from incremental processing
     * @param retryResults results from retry processing
     */
    private void logCombinedResults(ProcessingResults incrementalResults, ProcessingResults retryResults) {
        LOGGER.info(
                "OSH polling cycle completed: " + "Incremental: {} processed, {} skipped, {} failed | "
                        + "Retry: {} processed, {} skipped, {} failed",
                incrementalResults.processedCount(),
                incrementalResults.skippedCount(),
                incrementalResults.failedCount(),
                retryResults.processedCount(),
                retryResults.skippedCount(),
                retryResults.failedCount());
    }

    /**
     * Prepares polling configuration with start scan ID and batch size.
     */
    private PollConfiguration preparePollConfiguration() {
        int startScanId = determineStartScanId();
        int batchSize = oshConfiguration.getBatchSize();

        LOGGER.debug("Polling OSH for scans starting from ID {} with batch size {}", startScanId, batchSize);

        return new PollConfiguration(startScanId, batchSize);
    }

    /**
     * Fetches finished scans from OSH using the provided configuration.
     */
    private List<OshScanResponse> fetchFinishedScans(PollConfiguration config) {
        return oshClientService.getFinishedScans(config.startScanId(), config.batchSize());
    }

    /**
     * Handles the case when no finished scans are found in the current batch.
     */
    private void handleEmptyBatch(PollConfiguration config) {
        LOGGER.debug("No new finished scans found starting from ID {}", config.startScanId());
        updateCursorInNewTransaction(config.startScanId() + config.batchSize());
    }

    /**
     * Processes a list of OSH scans and returns processing statistics.
     * Records failures for retry when retry is enabled.
     *
     * @param scans list of OSH scans to process
     * @param phase processing phase ("incremental" or "retry") for logging
     * @return processing statistics
     */
    private ProcessingResults processScans(List<OshScanResponse> scans, String phase) {
        int processedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (OshScanResponse scan : scans) {
            try {
                ProcessingResult result = processSingleScan(scan);
                switch (result) {
                    case PROCESSED -> processedCount++;
                    case SKIPPED -> skippedCount++;
                    case FAILED -> failedCount++;
                }
            } catch (Exception e) {
                failedCount++;
                LOGGER.error(
                        "Failed to process OSH scan {} ({}), continuing with next scan", scan.getScanId(), phase, e);

                if ("incremental".equals(phase) && retryConfiguration.isRetryEnabled()) {
                    OshFailureReason failureReason = classifyFailure(e);
                    oshRetryService.recordFailedScan(scan, failureReason, e.getMessage());
                }
            }
        }

        return new ProcessingResults(processedCount, skippedCount, failedCount, phase);
    }

    /**
     * Processes a single OSH scan and returns the result.
     */
    private ProcessingResult processSingleScan(OshScanResponse scan) {
        if (!shouldProcessScan(scan)) {
            LOGGER.debug("Skipped OSH scan {} (package not monitored or scan not eligible)", scan.getScanId());
            return ProcessingResult.SKIPPED;
        }

        Optional<Job> createdJob = oshJobCreationService.createJobFromOshScan(scan);
        if (createdJob.isPresent()) {
            LOGGER.debug(
                    "Successfully processed OSH scan {} -> job {}",
                    scan.getScanId(),
                    createdJob.get().getId());
            return ProcessingResult.PROCESSED;
        } else {
            LOGGER.debug("Skipped OSH scan {} (already processed or no JSON available)", scan.getScanId());
            return ProcessingResult.SKIPPED;
        }
    }

    /**
     * Configuration record for polling parameters.
     */
    private record PollConfiguration(int startScanId, int batchSize) {}

    /**
     * Results record for processing statistics with phase information.
     */
    private record ProcessingResults(int processedCount, int skippedCount, int failedCount, String phase) {}

    /**
     * Enum representing the result of processing a single scan.
     */
    private enum ProcessingResult {
        PROCESSED,
        SKIPPED,
        FAILED
    }

    /**
     * Determines the starting scan ID for the current polling cycle.
     * Uses cursor from database, or falls back to configured start ID.
     */
    private int determineStartScanId() {
        try {
            Optional<OshSchedulerCursor> cursor = cursorRepository.getCurrentCursor();
            if (cursor.isPresent() && cursor.get().getLastSeenToken() != null) {
                int lastSeenId = Integer.parseInt(cursor.get().getLastSeenToken());
                LOGGER.debug("Using cursor position: last seen scan ID {}", lastSeenId);
                return lastSeenId;
            }
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid cursor token format, falling back to configured start ID: {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.warn("Error reading cursor, falling back to configured start ID: {}", e.getMessage());
        }

        int configuredStartId = oshConfiguration.getStartScanId();
        LOGGER.info("Using configured start scan ID: {}", configuredStartId);
        return configuredStartId;
    }

    /**
     * Updates the polling cursor in a separate transaction.
     * This ensures cursor advances even if some individual scans failed to process.
     *
     * @param newCursorPosition the new cursor position (next scan ID to check)
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateCursorInNewTransaction(int newCursorPosition) {
        try {
            cursorRepository.updateCursor(String.valueOf(newCursorPosition), LocalDateTime.now());
            LOGGER.debug("Updated OSH cursor to position {}", newCursorPosition);
        } catch (Exception e) {
            LOGGER.error("Failed to update OSH cursor to position {}, will retry next cycle", newCursorPosition, e);
        }
    }

    /**
     * Determines whether a specific OSH scan should be processed.
     *
     * Checks:
     * 1. Scan is eligible for processing (CLOSED state, has component name)
     * 2. Package is in the monitored package list (if configured)
     */
    private boolean shouldProcessScan(OshScanResponse scan) {
        if (!oshJobCreationService.canProcessScan(scan)) {
            return false;
        }

        String packageName = scan.getPackageName();
        if (!oshConfiguration.shouldMonitorPackage(packageName)) {
            LOGGER.debug("Package '{}' not in monitoring list for scan {}", packageName, scan.getScanId());
            return false;
        }

        return true;
    }

    /**
     * Manual trigger for OSH polling (for testing purposes).
     * Uses the same logic as scheduled polling but can be called on-demand.
     *
     * @return Summary of processing results
     */
    public String manualPollOsh() {
        if (!oshConfiguration.isEnabled()) {
            return "OSH integration is disabled";
        }

        LOGGER.info("Manual OSH poll triggered");

        try {
            pollOshForNewScans();
            return "Manual OSH poll completed successfully";
        } catch (Exception e) {
            LOGGER.error("Manual OSH poll failed", e);
            return "Manual OSH poll failed: " + e.getMessage();
        }
    }

    /**
     * Gets the current cursor status for monitoring/debugging.
     */
    public String getCursorStatus() {
        try {
            Optional<OshSchedulerCursor> cursor = cursorRepository.getCurrentCursor();
            if (cursor.isPresent()) {
                OshSchedulerCursor c = cursor.get();
                return String.format(
                        "Cursor: lastSeenToken=%s, lastSeenTimestamp=%s, updatedAt=%s",
                        c.getLastSeenToken(), c.getLastSeenTimestamp(), c.getUpdatedAt());
            } else {
                return "No cursor found - will start from configured startScanId: " + oshConfiguration.getStartScanId();
            }
        } catch (Exception e) {
            return "Error reading cursor: " + e.getMessage();
        }
    }
}
