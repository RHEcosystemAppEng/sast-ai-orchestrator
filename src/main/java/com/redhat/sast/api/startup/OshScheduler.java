package com.redhat.sast.api.startup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redhat.sast.api.config.OshConfiguration;
import com.redhat.sast.api.enums.OshFailureReason;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.OshSchedulerCursor;
import com.redhat.sast.api.model.OshUncollectedScan;
import com.redhat.sast.api.repository.OshSchedulerCursorRepository;
import com.redhat.sast.api.service.osh.OshClientService;
import com.redhat.sast.api.service.osh.OshJobCreationService;
import com.redhat.sast.api.service.osh.OshRetryService;
import com.redhat.sast.api.v1.dto.osh.OshScan;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler for automated OSH (Open Scan Hub) scan discovery and job creation.
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
public class OshScheduler {

    private static final String PHASE_INCREMENTAL = "incremental";
    private static final String PHASE_RETRY = "retry";

    @FunctionalInterface
    private interface ScanProcessor<T> {
        ProcessingResult process(T item);
    }

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

    /**
     * Main scheduler method that polls OSH for new scans and creates jobs.
     *
     * Execution pattern (two-phase):
     * PHASE 1 - Incremental processing:
     * 1. Check if OSH integration is enabled
     * 2. Determine starting scan ID from cursor
     * 3. Fetch new finished scans from OSH (filters by CLOSED state)
     * 4. Process each scan individually in separate transactions
     * 5. Update cursor to (highest processed scan ID + 1)
     *    - Cursor only advances when scans are actually processed
     *    - Cursor remains unchanged if no finished scans found (may be in progress)
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
     * Cursor advancement strategy:
     * - Only advances when scans are successfully processed
     * - Advances to (highest processed scan ID + 1)
     * - Does NOT advance if no finished scans found (they might be in progress)
     *
     * @return processing results for incremental scans
     */
    private ProcessingResults processIncrementalScans() {
        try {
            LOGGER.debug("Starting incremental scan processing");

            int startScanId = getStartScanId();
            int batchSize = oshConfiguration.getBatchSize();

            LOGGER.debug("Polling OSH for scans starting from ID {} with batch size {}", startScanId, batchSize);

            List<OshScan> scansToProcess = oshClientService.fetchOshScansForProcessing(startScanId, batchSize);

            if (scansToProcess.isEmpty()) {
                LOGGER.debug(
                        "No finished scans found in range {}-{}(exclusive), cursor unchanged (scans may be in progress)",
                        startScanId,
                        startScanId + batchSize);
                return new ProcessingResults(0, 0, 0, PHASE_INCREMENTAL);
            }

            LOGGER.debug(
                    "Found {} finished scans in range {}-{}(exclusive)",
                    scansToProcess.size(),
                    startScanId,
                    startScanId + batchSize);

            var finalResults = triggerWorkflows(scansToProcess, this::processSingleScan, PHASE_INCREMENTAL);

            int highestProcessedId =
                    scansToProcess.stream().mapToInt(OshScan::getScanId).max().orElse(startScanId - 1);
            int nextScanId = highestProcessedId + 1;
            updateCursorInNewTransaction(nextScanId);

            LOGGER.debug(
                    "Incremental scan processing completed: {} processed, {} skipped, {} failed. "
                            + "Cursor advanced to {}",
                    finalResults.processedCount(),
                    finalResults.skippedCount(),
                    finalResults.failedCount(),
                    nextScanId);

            return finalResults;

        } catch (Exception e) {
            LOGGER.error("Incremental scan processing failed: {}", e.getMessage(), e);
            return new ProcessingResults(0, 0, 0, PHASE_INCREMENTAL);
        }
    }

    /**
     * PHASE 2: Process retry scans.
     * Handles failed scans from the retry queue with backoff enforcement.
     *
     * @return processing results for retry scans
     */
    private ProcessingResults processRetryScans() {
        try {
            LOGGER.debug("Starting retry scan processing");

            List<OshUncollectedScan> retryScans = oshRetryService.fetchRetryableScans();

            if (retryScans.isEmpty()) {
                LOGGER.debug("No retry-eligible scans found");
                return new ProcessingResults(0, 0, 0, PHASE_RETRY);
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
            return new ProcessingResults(0, 0, 0, PHASE_RETRY);
        }
    }

    /**
     * Processes a list of retry scans from the uncollected scan queue.
     *
     * @param retryScans list of uncollected scans to retry
     * @return processing results for the retry batch
     */
    private ProcessingResults processRetryScans(List<OshUncollectedScan> retryScans) {
        return triggerWorkflows(
                retryScans,
                uncollectedScan -> {
                    try {
                        OshScan scan = oshRetryService.reconstructScanFromJson(uncollectedScan.getScanDataJson());
                        return processSingleRetryScan(uncollectedScan, scan);
                    } catch (Exception e) {
                        LOGGER.error(
                                "Failed to reconstruct scan from JSON for retry scan {}: {}",
                                uncollectedScan.getId(),
                                e.getMessage(),
                                e);
                        OshFailureReason failureReason = classifyFailure(e);
                        oshRetryService.recordRetryAttempt(uncollectedScan.getId(), failureReason, e.getMessage());
                        return ProcessingResult.FAILED;
                    }
                },
                PHASE_RETRY);
    }

    /**
     * Processes a single retry scan with attempt tracking.
     *
     * @param uncollectedScan the uncollected scan record
     * @param scan the reconstructed OSH scan response
     * @return processing result
     */
    private ProcessingResult processSingleRetryScan(OshUncollectedScan uncollectedScan, OshScan scan) {
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
            LOGGER.error(
                    "Retry attempt failed for scan {} (attempt {}): {}",
                    scan.getScanId(),
                    uncollectedScan.getAttemptCount() + 1,
                    e.getMessage(),
                    e);

            OshFailureReason failureReason = classifyFailure(e);
            oshRetryService.recordRetryAttempt(uncollectedScan.getId(), failureReason, e.getMessage());

            return ProcessingResult.FAILED;
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
        int totalProcessed = incrementalResults.processedCount() + retryResults.processedCount();
        int totalSkipped = incrementalResults.skippedCount() + retryResults.skippedCount();
        int totalFailed = incrementalResults.failedCount() + retryResults.failedCount();

        if (totalProcessed > 0 || totalFailed > 0) {
            LOGGER.info(
                    "OSH polling cycle completed: {} total processed, {} skipped, {} failed | "
                            + "Breakdown - Incremental: {}/{}/{}, Retry: {}/{}/{}",
                    totalProcessed,
                    totalSkipped,
                    totalFailed,
                    incrementalResults.processedCount(),
                    incrementalResults.skippedCount(),
                    incrementalResults.failedCount(),
                    retryResults.processedCount(),
                    retryResults.skippedCount(),
                    retryResults.failedCount());
        } else {
            LOGGER.debug(
                    "OSH polling cycle completed: no scans processed | " + "Incremental: {} skipped, Retry: {} skipped",
                    incrementalResults.skippedCount(),
                    retryResults.skippedCount());
        }

        String queueStatus = oshRetryService.getRetryQueueStatus();
        LOGGER.debug("OSH retry queue status: {}", queueStatus);
    }

    /**
     * Generic method to process a list of items (scans or retry scans).
     *
     * @param oshScanList list of items to process
     * @param processor function to process each item (handles its own exceptions)
     * @param phase phase name for logging and results
     * @return processing results with counts
     */
    private <T> ProcessingResults triggerWorkflows(List<T> oshScanList, ScanProcessor<T> processor, String phase) {
        int processedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (T e : oshScanList) {
            ProcessingResult result = processor.process(e);
            switch (result) {
                case PROCESSED -> processedCount++;
                case SKIPPED -> skippedCount++;
                case FAILED -> failedCount++;
            }
        }

        return new ProcessingResults(processedCount, skippedCount, failedCount, phase);
    }

    private ProcessingResult processSingleScan(OshScan scan) {
        try {
            if (oshJobCreationService.canProcessScan(scan)) {
                if (oshConfiguration.shouldMonitorPackage(scan.getPackageName())) {

                    Optional<Job> job = oshJobCreationService.createJobFromOshScan(scan);
                    if (job.isPresent()) {
                        return ProcessingResult.PROCESSED;
                    }
                    LOGGER.debug("Skipped OSH scan {} (already processed or no JSON available)", scan.getScanId());
                }

            } else {
                LOGGER.debug("Skipped OSH scan {} (scan not eligible for processing)", scan.getScanId());
            }
            return ProcessingResult.SKIPPED;

        } catch (Exception e) {
            LOGGER.error("Failed to process OSH scan {}: {}", scan.getScanId(), e.getMessage(), e);
            oshRetryService.recordFailedScan(scan, classifyFailure(e), e.getMessage());
            return ProcessingResult.FAILED;
        }
    }

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
    private int getStartScanId() {
        try {
            Optional<OshSchedulerCursor> cursor = cursorRepository.getCurrentCursor();
            if (cursor.isPresent() && cursor.get().getLastSeenToken() != null) {
                return Integer.parseInt(cursor.get().getLastSeenToken());
            }
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid cursor token format, fallback to configured start ID. Reason: {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.warn("Error reading cursor. Reason: {}", e.getMessage());
        }

        int defaultStartId = oshConfiguration.getStartScanId();
        LOGGER.info("Using configured start scan ID: {}", defaultStartId);
        return defaultStartId;
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
