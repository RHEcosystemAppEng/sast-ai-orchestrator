package com.redhat.sast.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.redhat.sast.api.config.OshConfiguration;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.OshSchedulerCursor;
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
 * This service implements the main automation loop that:
 * 1. Polls OSH for new completed scans at regular intervals
 * 2. Filters scans by configured package list
 * 3. Creates SAST-AI workflow jobs for eligible scans
 * 4. Maintains cursor state for incremental polling
 * 5. Handles failures gracefully without stopping the scheduler
 *
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

    /**
     * Main scheduler method that polls OSH for new scans and creates jobs.
     *
     * Execution pattern:
     * 1. Check if OSH integration is enabled
     * 2. Determine starting scan ID from cursor
     * 3. Fetch new finished scans from OSH
     * 4. Process each scan individually in separate transactions
     * 5. Update cursor position after successful batch processing
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
            LOGGER.debug("Starting OSH polling cycle");

            PollConfiguration config = preparePollConfiguration();
            List<OshScanResponse> finishedScans = fetchFinishedScans(config);

            if (finishedScans.isEmpty()) {
                handleEmptyBatch(config);
                return;
            }

            LOGGER.debug(
                    "Found {} finished scans in range {}-{}",
                    finishedScans.size(),
                    config.startScanId(),
                    config.startScanId() + config.batchSize() - 1);

            ProcessingResults results = processScans(finishedScans);
            updateCursorInNewTransaction(config.startScanId() + config.batchSize());
            logProcessingResults(results);

        } catch (Exception e) {
            LOGGER.error("OSH polling cycle failed, will retry next scheduled interval", e);
        }
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
     */
    private ProcessingResults processScans(List<OshScanResponse> scans) {
        int processedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (OshScanResponse scan : scans) {
            try {
                ProcessingResult result = processSingleScan(scan);
                switch (result) {
                    case PROCESSED -> processedCount++;
                    case SKIPPED -> skippedCount++;
                }
            } catch (Exception e) {
                failedCount++;
                LOGGER.error("Failed to process OSH scan {}, continuing with next scan", scan.getScanId(), e);
            }
        }

        return new ProcessingResults(processedCount, skippedCount, failedCount);
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
     * Logs the final processing results.
     */
    private void logProcessingResults(ProcessingResults results) {
        LOGGER.debug(
                "OSH polling cycle completed: {} processed, {} skipped, {} failed",
                results.processedCount(),
                results.skippedCount(),
                results.failedCount());
    }

    /**
     * Configuration record for polling parameters.
     */
    private record PollConfiguration(int startScanId, int batchSize) {}

    /**
     * Results record for processing statistics.
     */
    private record ProcessingResults(int processedCount, int skippedCount, int failedCount) {}

    /**
     * Enum representing the result of processing a single scan.
     */
    private enum ProcessingResult {
        PROCESSED,
        SKIPPED
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
