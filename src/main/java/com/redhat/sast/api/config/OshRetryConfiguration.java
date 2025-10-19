package com.redhat.sast.api.config;

import java.time.Duration;
import java.time.LocalDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for OSH scan retry mechanism.
 *
 * Provides validated configuration properties for:
 * - Retry behavior (max attempts, backoff timing)
 * - Batch sizing for retry processing
 * - Retention policies for failed scans
 * - Feature toggle support
 *
 * This configuration works alongside OshConfiguration to provide retry capabilities
 * for failed OSH scan processing.
 */
@ApplicationScoped
@Getter
@Slf4j
public class OshRetryConfiguration {

    private static final int MAX_RETRY_BATCH_SIZE = 50;
    private static final long MAX_BACKOFF_MINUTES = 1440; // 24 hours

    /**
     * Master toggle for OSH retry mechanism.
     * When false, failed scans are permanently skipped.
     * When true, failed scans are stored and retried according to other settings.
     */
    @ConfigProperty(name = "osh.retry.enabled", defaultValue = "false")
    boolean retryEnabled;

    /**
     * Maximum retry attempts before permanent failure.
     * Set to 0 to disable retry limit (use retention-days only).
     */
    @ConfigProperty(name = "osh.retry.max-attempts", defaultValue = "3")
    int maxAttempts;

    /**
     * Number of scans to retry per polling cycle.
     * Lower values reduce impact on incremental scan processing.
     */
    @ConfigProperty(name = "osh.retry.batch-size", defaultValue = "5")
    int retryBatchSize;

    /**
     * Minimum wait time (minutes) before retrying a failed scan.
     * Prevents immediate hammering of failing resources.
     * Used as base time for exponential backoff if enabled.
     */
    @ConfigProperty(name = "osh.retry.backoff-minutes", defaultValue = "20")
    int backoffMinutes;

    /**
     * Use exponential backoff strategy for retry timing.
     * If true: wait time = backoff-minutes * (2^attemptNumber)
     * If false: wait time = backoff-minutes (constant)
     */
    @ConfigProperty(name = "osh.retry.exponential-backoff", defaultValue = "true")
    boolean exponentialBackoff;

    /**
     * Maximum days to retain failed scans in retry queue.
     * After this period, scans are permanently removed regardless of attempts.
     * Prevents unbounded growth of retry table.
     */
    @ConfigProperty(name = "osh.retry.retention-days", defaultValue = "7")
    int retentionDays;

    /**
     * Cleanup schedule interval for removing expired retry records.
     * Runs as separate scheduled job to avoid impacting polling cycle.
     */
    @ConfigProperty(name = "osh.retry.cleanup-interval", defaultValue = "24h")
    String cleanupInterval;

    /**
     * Validates retry configuration at startup.
     * Fails fast if invalid values are provided.
     * Only validates when retry is enabled to avoid startup failures.
     */
    @PostConstruct
    public void validateConfiguration() {
        if (!retryEnabled) {
            LOGGER.info("OSH retry mechanism is disabled (legacy skip-on-failure mode)");
            return;
        }

        LOGGER.info("OSH retry mechanism is enabled - validating configuration");

        if (maxAttempts < 0) {
            throw new IllegalStateException(
                    "Invalid osh.retry.max-attempts: " + maxAttempts + " (must be non-negative, 0 = unlimited)");
        }

        if (retryBatchSize <= 0) {
            throw new IllegalStateException("Invalid osh.retry.batch-size: " + retryBatchSize + " (must be positive)");
        }

        if (backoffMinutes <= 0) {
            throw new IllegalStateException(
                    "Invalid osh.retry.backoff-minutes: " + backoffMinutes + " (must be positive)");
        }

        if (retentionDays <= 0) {
            throw new IllegalStateException(
                    "Invalid osh.retry.retention-days: " + retentionDays + " (must be positive)");
        }

        try {
            Duration.parse(
                    "PT" + cleanupInterval.replace("h", "H").replace("m", "M").replace("s", "S"));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid osh.retry.cleanup-interval: " + cleanupInterval
                    + " (must be valid duration like '1h', '30m', '24h')");
        }

        LOGGER.debug("OSH retry configuration validated successfully:");
        LOGGER.debug("  Retry enabled: {}", retryEnabled);
        LOGGER.debug("  Max attempts: {}", maxAttempts == 0 ? "unlimited" : maxAttempts);
        LOGGER.debug("  Retry batch size: {}", retryBatchSize);
        LOGGER.debug("  Backoff minutes: {}", backoffMinutes);
        LOGGER.debug("  Exponential backoff: {}", exponentialBackoff);
        LOGGER.debug("  Retention days: {}", retentionDays);
        LOGGER.debug("  Cleanup interval: {}", cleanupInterval);
    }

    /**
     * Calculates actual backoff time for a given attempt number.
     * Implements exponential backoff strategy if enabled.
     *
     * @param attemptNumber current attempt number
     * @return backoff time in minutes
     */
    public long calculateBackoffMinutes(int attemptNumber) {
        if (!exponentialBackoff || attemptNumber <= 1) {
            return backoffMinutes;
        }

        long calculatedBackoff = backoffMinutes * (long) Math.pow(2, attemptNumber - 1);

        return Math.min(calculatedBackoff, MAX_BACKOFF_MINUTES);
    }

    /**
     * Gets the cutoff time for retry eligibility based on backoff strategy.
     * Scans are only eligible for retry if their last attempt was before this time.
     *
     * @param attemptNumber attempt number for exponential backoff calculation
     * @return cutoff timestamp - only retry scans attempted before this time
     */
    public LocalDateTime getRetryCutoffTime(int attemptNumber) {
        long backoffMinutes = calculateBackoffMinutes(attemptNumber);
        return LocalDateTime.now().minusMinutes(backoffMinutes);
    }

    /**
     * Gets the cutoff time for standard retry eligibility.
     * Uses base backoff time for scans with unknown attempt counts.
     *
     * @return cutoff timestamp for basic retry eligibility
     */
    public LocalDateTime getStandardRetryCutoffTime() {
        return LocalDateTime.now().minusMinutes(backoffMinutes);
    }

    /**
     * Gets the cutoff time for retention cleanup.
     * Scans created before this time should be permanently removed.
     *
     * @return cutoff timestamp for retention policy
     */
    public LocalDateTime getRetentionCutoffTime() {
        return LocalDateTime.now().minusDays(retentionDays);
    }

    /**
     * Checks if retry attempts should be limited.
     * Returns false if maxAttempts is 0 (unlimited retries).
     *
     * @return true if retry attempts should be counted and limited
     */
    public boolean hasRetryLimit() {
        return maxAttempts > 0;
    }

    /**
     * Returns effective retry batch size, ensuring it doesn't exceed reasonable limits.
     *
     * @return safe retry batch size to use
     */
    public int getEffectiveRetryBatchSize() {
        return Math.min(retryBatchSize, MAX_RETRY_BATCH_SIZE);
    }
}
