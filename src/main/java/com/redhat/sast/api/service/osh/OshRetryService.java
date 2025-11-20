package com.redhat.sast.api.service.osh;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.sast.api.config.OshConfiguration;
import com.redhat.sast.api.enums.OshFailureReason;
import com.redhat.sast.api.model.OshUncollectedScan;
import com.redhat.sast.api.repository.OshRetryStatisticsRepository;
import com.redhat.sast.api.repository.OshUncollectedScanRepository;
import com.redhat.sast.api.v1.dto.osh.OshScanDto;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing OSH scan retry lifecycle.
 *
 * This service provides the core retry mechanism for failed OSH scans:
 * 1. Recording scan processing failures with classification
 * 2. Fetching retry-eligible scans with backoff enforcement
 * 3. Managing retry attempt tracking and success cleanup
 * 4. Automated retention policy cleanup
 *
 * All failure reasons (OshFailureReason) are eligible for retry. Retry eligibility
 * is determined by backoff timing and maximum attempt limits, not by failure type.
 */
@ApplicationScoped
@Slf4j
public class OshRetryService {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 2000;
    private static final String TRUNCATION_SUFFIX = "... (truncated)";
    private static final String UNLIMITED_ATTEMPTS = "unlimited";

    @Inject
    OshUncollectedScanRepository uncollectedScanRepository;

    @Inject
    OshRetryStatisticsRepository retryStatisticsRepository;

    @Inject
    OshConfiguration oshConfiguration;

    @Inject
    OshJobCreationService oshJobCreationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Records a failed scan for potential retry.
     * Uses isolated transaction to ensure failure recording doesn't affect main polling.
     *
     * @param scan OSH scan that failed processing
     * @param failureReason classification of the failure type
     * @param errorMessage detailed error message from the failure
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void recordFailedScan(OshScanDto scan, OshFailureReason failureReason, String errorMessage) {
        if (scan == null || scan.getScanId() == null) {
            LOGGER.warn("Cannot record null scan or scan with null ID for retry");
            return;
        }

        try {
            if (uncollectedScanRepository.existsByOshScanId(scan.getScanId())) {
                LOGGER.debug("Scan {} already in retry queue, skipping duplicate recording", scan.getScanId());
                return;
            }

            String scanDataJson = serializeScanData(scan);

            OshUncollectedScan uncollectedScan = new OshUncollectedScan(
                    scan.getScanId(),
                    scan.getPackageName(),
                    failureReason,
                    scanDataJson,
                    limitErrorMessage(errorMessage));

            setPackageNvrSafely(uncollectedScan, scan);

            uncollectedScanRepository.persist(uncollectedScan);

            LOGGER.info(
                    "Recorded failed OSH scan {} for retry (reason: {}, package: {})",
                    scan.getScanId(),
                    failureReason,
                    scan.getPackageName());

        } catch (jakarta.persistence.PersistenceException e) {
            LOGGER.error(
                    "Database error recording uncollected scan {} for retry: {}", scan.getScanId(), e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(
                    "Unexpected error recording uncollected scan {} for retry: {}",
                    scan.getScanId(),
                    e.getMessage(),
                    e);
        }
    }

    /**
     * Retrieves retry-eligible scans using database locking for concurrency safety.
     * Uses configured backoff timing and retry limits.
     *
     * Note: All failure reasons (OshFailureReason) are eligible for retry.
     * Eligibility is determined by backoff timing and attempt count limits only.
     *
     * @param batchSize maximum number of scans to return
     * @return list of scans eligible for retry, locked for update
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public List<OshUncollectedScan> fetchRetryableScans() {
        try {
            LocalDateTime cutoffTime = oshConfiguration.getStandardRetryCutoffTime();
            int maxAttempts = oshConfiguration.hasRetryLimit() ? oshConfiguration.getRetryMaxAttempts() : -1;
            int effectiveBatchSize = oshConfiguration.getEffectiveRetryBatchSize();

            List<OshUncollectedScan> eligibleScans =
                    uncollectedScanRepository.findRetryableScansWithLock(cutoffTime, maxAttempts, effectiveBatchSize);

            if (!eligibleScans.isEmpty()) {
                LOGGER.debug(
                        "Found {} scans eligible for retry (cutoff: {}, max attempts: {})",
                        eligibleScans.size(),
                        cutoffTime,
                        maxAttempts == -1 ? UNLIMITED_ATTEMPTS : maxAttempts);
            }

            return eligibleScans;

        } catch (jakarta.persistence.PersistenceException e) {
            LOGGER.error("Database error fetching retry-eligible scans: {}", e.getMessage(), e);
            return List.of();
        } catch (Exception e) {
            LOGGER.error("Unexpected error fetching retry-eligible scans: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Records successful retry completion by removing scan from retry queue.
     * Uses isolated transaction so removal failures don't affect job creation.
     *
     * @param scanId OSH scan ID that was successfully processed
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void markRetrySuccessful(Integer scanId) {
        if (scanId == null) {
            return;
        }

        try {
            long deletedCount = uncollectedScanRepository.deleteByOshScanId(scanId);
            if (deletedCount > 0) {
                LOGGER.debug("Removed scan {} from retry queue (successful processing)", scanId);
            } else {
                LOGGER.debug("Scan {} not found in retry queue for removal", scanId);
            }
        } catch (jakarta.persistence.PersistenceException e) {
            LOGGER.error("Database error removing successful retry scan {}: {}", scanId, e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error removing successful retry scan {}: {}", scanId, e.getMessage(), e);
        }
    }

    /**
     * Records a retry attempt by incrementing attempt count and updating failure info.
     * Uses optimistic locking to handle concurrent modifications safely.
     *
     * @param uncollectedScanId database ID of the uncollected scan record
     * @param newFailureReason updated failure reason from retry attempt
     * @param errorMessage error message from the retry attempt
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void recordRetryAttempt(Long uncollectedScanId, OshFailureReason newFailureReason, String errorMessage) {
        if (uncollectedScanId == null) {
            return;
        }

        try {
            int updatedCount = uncollectedScanRepository.updateRetryAttempt(
                    uncollectedScanId, newFailureReason, limitErrorMessage(errorMessage));

            if (updatedCount > 0) {
                LOGGER.debug("Updated retry attempt for uncollected scan ID {}", uncollectedScanId);
            } else {
                LOGGER.warn(
                        "Failed to update retry attempt for uncollected scan ID {} (not found or conflict)",
                        uncollectedScanId);
            }

        } catch (OptimisticLockException e) {
            LOGGER.warn("Concurrent modification detected for uncollected scan {}, skipping update", uncollectedScanId);
        } catch (jakarta.persistence.PersistenceException e) {
            LOGGER.error(
                    "Database error recording retry attempt for uncollected scan {}: {}",
                    uncollectedScanId,
                    e.getMessage(),
                    e);
        } catch (Exception e) {
            LOGGER.error(
                    "Unexpected error recording retry attempt for uncollected scan {}: {}",
                    uncollectedScanId,
                    e.getMessage(),
                    e);
        }
    }

    /**
     * Reconstructs OshScanDto from stored JSON data.
     * Used when processing retry attempts to avoid re-fetching from OSH.
     *
     * @param scanDataJson JSON representation of the original scan
     * @return reconstructed OSH scan response
     * @throws JsonProcessingException if JSON parsing fails
     */
    public OshScanDto reconstructScanFromJson(String scanDataJson) throws JsonProcessingException {
        if (scanDataJson == null || scanDataJson.trim().isEmpty()) {
            throw new IllegalArgumentException("Scan data JSON is null or empty");
        }

        return objectMapper.readValue(scanDataJson, OshScanDto.class);
    }

    /**
     * Scheduled cleanup of expired retry records.
     * Removes scans that exceed retention period to prevent unbounded table growth.
     */
    @Scheduled(every = "${osh.retry.cleanup-interval:24h}")
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void cleanupExpiredRetries() {
        LocalDateTime startTime = LocalDateTime.now();
        try {
            LocalDateTime cutoffTime = oshConfiguration.getRetentionCutoffTime();
            long totalBefore = uncollectedScanRepository.count();
            long deletedCount = uncollectedScanRepository.deleteOlderThan(cutoffTime);

            if (deletedCount > 0) {
                LOGGER.info(
                        "OSH retry cleanup completed: {} expired records removed (retention cutoff: {}, "
                                + "total before: {}, total after: {})",
                        deletedCount,
                        cutoffTime,
                        totalBefore,
                        totalBefore - deletedCount);
            } else {
                LOGGER.debug(
                        "OSH retry cleanup completed: no expired records found (retention cutoff: {}, total: {})",
                        cutoffTime,
                        totalBefore);
            }

        } catch (Exception e) {
            LOGGER.error(
                    "OSH retry cleanup failed after {} minutes: {}",
                    java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes(),
                    e.getMessage(),
                    e);
        }
    }

    /**
     * Cleanup scans that have exceeded maximum retry attempts.
     * Separate from retention cleanup to handle different failure scenarios.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public int cleanupExceededRetries() {
        if (!oshConfiguration.hasRetryLimit()) {
            return 0;
        }

        try {
            int maxAttempts = oshConfiguration.getRetryMaxAttempts();
            long totalBefore = uncollectedScanRepository.count();
            long deletedCount = uncollectedScanRepository.deleteExceededRetries(maxAttempts);

            if (deletedCount > 0) {
                LOGGER.info(
                        "OSH retry exceeded cleanup completed: {} scans removed (max attempts: {}, "
                                + "total before: {}, total after: {})",
                        deletedCount,
                        maxAttempts,
                        totalBefore,
                        totalBefore - deletedCount);
            } else {
                LOGGER.debug(
                        "OSH retry exceeded cleanup: no scans exceeded max attempts ({}, total: {})",
                        maxAttempts,
                        totalBefore);
            }

            return (int) deletedCount;

        } catch (Exception e) {
            LOGGER.error("OSH retry exceeded cleanup failed: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Gets current retry queue statistics for monitoring.
     *
     * @return summary of retry queue status
     */
    public String getRetryQueueStatus() {
        try {
            long totalInQueue = uncollectedScanRepository.count();
            LocalDateTime cutoffTime = oshConfiguration.getStandardRetryCutoffTime();
            int maxAttempts = oshConfiguration.hasRetryLimit() ? oshConfiguration.getRetryMaxAttempts() : -1;
            long eligibleNow = retryStatisticsRepository.countEligibleForRetry(cutoffTime, maxAttempts);

            return String.format(
                    "Retry queue: %d total, %d eligible now (cutoff: %s, max attempts: %s)",
                    totalInQueue, eligibleNow, cutoffTime, maxAttempts == 0 ? UNLIMITED_ATTEMPTS : maxAttempts);

        } catch (Exception e) {
            return "Error reading retry queue status: " + e.getMessage();
        }
    }

    /**
     * Finds retry information for a specific scan ID.
     *
     * @param scanId OSH scan ID to look up
     * @return retry record if found
     */
    public Optional<OshUncollectedScan> findRetryInfo(Integer scanId) {
        if (scanId == null) {
            return Optional.empty();
        }

        try {
            return uncollectedScanRepository.findByOshScanId(scanId);
        } catch (Exception e) {
            LOGGER.error("Error finding retry info for scan {}: {}", scanId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Gets detailed retry queue statistics for admin monitoring.
     *
     * @return detailed statistics object
     */
    public RetryQueueStatistics getDetailedRetryStatistics() {
        try {
            long totalInQueue = uncollectedScanRepository.count();
            LocalDateTime cutoffTime = oshConfiguration.getStandardRetryCutoffTime();
            int maxAttempts = oshConfiguration.hasRetryLimit() ? oshConfiguration.getRetryMaxAttempts() : -1;
            long eligibleNow = retryStatisticsRepository.countEligibleForRetry(cutoffTime, maxAttempts);

            long awaitingBackoff = totalInQueue - eligibleNow;
            long exceededMax = maxAttempts >= 0 ? retryStatisticsRepository.countExceededRetries(maxAttempts) : 0;

            return new RetryQueueStatistics(
                    true, totalInQueue, eligibleNow, awaitingBackoff, exceededMax, getConfigurationSummary());

        } catch (Exception e) {
            LOGGER.error("Failed to get detailed retry statistics: {}", e.getMessage(), e);
            return new RetryQueueStatistics(true, 0, 0, 0, 0, "Error: " + e.getMessage());
        }
    }

    /**
     * Gets a limited view of the retry queue for admin inspection.
     *
     * @param limit maximum number of records to return
     * @param sortBy sort field (currently limited to "created")
     * @return list of retry records
     */
    public List<OshUncollectedScan> getRetryQueueSnapshot(int limit, String sortBy) {
        try {
            return retryStatisticsRepository.findRecentScans(limit);

        } catch (Exception e) {
            LOGGER.error("Failed to get retry queue snapshot: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Gets configuration summary for admin display.
     */
    private String getConfigurationSummary() {
        long backoffMinutes = java.time.Duration.parse(oshConfiguration.getRetryBackoffDuration())
                .toMinutes();
        return String.format(
                "Max attempts: %s, Backoff: %dm, Exponential: %s, Retention: %dd",
                oshConfiguration.hasRetryLimit() ? oshConfiguration.getRetryMaxAttempts() : UNLIMITED_ATTEMPTS,
                backoffMinutes,
                oshConfiguration.isRetryExponentialBackoff() ? "yes" : "no",
                oshConfiguration.getRetryRetentionDays());
    }

    /**
     * Serializes OSH scan response to JSON for storage.
     */
    private String serializeScanData(OshScanDto scan) {
        try {
            return objectMapper.writeValueAsString(scan);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Failed to serialize scan data for {}: {}", scan.getScanId(), e.getMessage());
            return String.format("{\"scanId\": %d, \"error\": \"serialization_failed\"}", scan.getScanId());
        }
    }

    /**
     * Limits error message length to prevent database issues.
     */
    private String limitErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }

        if (errorMessage.length() > MAX_ERROR_MESSAGE_LENGTH) {
            return errorMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH) + TRUNCATION_SUFFIX;
        }

        return errorMessage;
    }

    /**
     * Safely sets the package NVR for an uncollected scan.
     * Extracts package NVR from scan data if possible, logs debug message on failure.
     *
     * @param uncollectedScan the uncollected scan to update
     * @param scan the original scan data
     */
    private void setPackageNvrSafely(OshUncollectedScan uncollectedScan, OshScanDto scan) {
        try {
            String packageNvr = oshJobCreationService.extractPackageNvr(scan);
            uncollectedScan.setPackageNvr(packageNvr);
        } catch (Exception e) {
            LOGGER.debug("Could not extract package NVR for scan {}: {}", scan.getScanId(), e.getMessage());
        }
    }

    /**
     * Statistics container for detailed retry queue information.
     */
    public static class RetryQueueStatistics {
        public final boolean enabled;
        public final long totalInQueue;
        public final long eligibleForRetry;
        public final long awaitingBackoff;
        public final long exceededMaxAttempts;
        public final String configurationSummary;

        public RetryQueueStatistics(boolean enabled) {
            this(enabled, 0, 0, 0, 0, "Retry disabled");
        }

        public RetryQueueStatistics(
                boolean enabled,
                long totalInQueue,
                long eligibleForRetry,
                long awaitingBackoff,
                long exceededMaxAttempts,
                String configurationSummary) {
            this.enabled = enabled;
            this.totalInQueue = totalInQueue;
            this.eligibleForRetry = eligibleForRetry;
            this.awaitingBackoff = awaitingBackoff;
            this.exceededMaxAttempts = exceededMaxAttempts;
            this.configurationSummary = configurationSummary;
        }
    }
}
