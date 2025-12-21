package com.redhat.sast.api.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for OSH (Open Scan Hub) integration.
 *
 * Provides validated configuration properties for:
 * - OSH API connection settings
 * - Package filtering options
 * - Polling behavior configuration
 * - Retry mechanism settings (attempts, backoff, retention)
 * - Feature toggle support
 *
 */
@ApplicationScoped
@Getter
@Slf4j
public class OshConfiguration {

    private static final int MAX_RETRY_BATCH_SIZE = 50;
    private static final long MAX_BACKOFF_MINUTES = 1440; // 24 hours

    /**
     * Master toggle for OSH integration.
     * When false, all OSH operations are disabled.
     */
    @ConfigProperty(name = "osh.integration.enabled", defaultValue = "false")
    boolean enabled;

    /**
     * OSH API base URL.
     * Default value is provided in application.properties.
     */
    @ConfigProperty(name = "osh.api.base-url")
    String baseUrl;

    /**
     * Set of package names to monitor.
     * Loaded from /deployments/config/packages.txt file.
     * Only scans for these packages will be processed.
     * Initialized in @PostConstruct.
     */
    private Set<String> packageNameSet = Set.of();

    /**
     * Flag to indicate if packageNameSet was set for testing purposes.
     * When true, file loading is skipped during validation.
     */
    private boolean packageNameSetOverridden = false;

    /**
     * Path to the packages.txt file containing the list of packages to monitor.
     * Default is /deployments/config/packages.txt (mounted from ConfigMap).
     */
    @ConfigProperty(name = "osh.packages.file.path", defaultValue = "/deployments/config/packages.txt")
    String packagesFilePath;

    /**
     * Number of sequential scan IDs to check in each polling batch.
     */
    @ConfigProperty(name = "osh.batch.size", defaultValue = "10")
    int batchSize;

    /**
     * Interval between OSH polling cycles.
     */
    @ConfigProperty(name = "osh.poll.interval", defaultValue = "30s")
    String pollInterval;

    /**
     * Starting scan ID for discovery when no cursor exists.
     * Should be set to a reasonable recent scan ID to avoid
     * processing very old scans on first run.
     * Default is 1,000,000 based on current (10-2025) OSH scan ID range.
     */
    @ConfigProperty(name = "osh.scan.start-id", defaultValue = "1000000")
    int startScanId;

    /**
     * Maximum number of scans to process in a single polling cycle.
     * Prevents runaway processing if many scans accumulate.
     */
    @ConfigProperty(name = "osh.scan.max-per-cycle", defaultValue = "50")
    int maxScansPerCycle;

    // ==================== Retry Configuration ====================

    /**
     * Maximum retry attempts before permanent failure.
     * Set to 0 for no retries (try once, fail immediately).
     * Set to -1 to disable retry limit (unlimited retries, use retention-days only).
     */
    @Setter
    @ConfigProperty(name = "osh.retry.max-attempts", defaultValue = "3")
    int retryMaxAttempts;

    /**
     * Number of scans to retry per polling cycle.
     * Lower values reduce impact on the incremental scan processing.
     */
    @ConfigProperty(name = "osh.retry.batch-size", defaultValue = "5")
    int retryBatchSize;

    /**
     * Minimum wait time (minutes) before retrying a failed scan.
     * Prevents immediate hammering of failing resources.
     * Used as base time for exponential backoff if enabled.
     */
    @Setter
    @ConfigProperty(name = "osh.retry.initial-backoff-duration", defaultValue = "PT20M")
    String retryBackoffDuration;

    /**
     * Use exponential backoff strategy for retry timing.
     * If true: wait time = backoff-minutes * (2^attemptNumber)
     * If false: wait time = backoff-minutes (constant)
     */
    @ConfigProperty(name = "osh.retry.exponential-backoff", defaultValue = "true")
    boolean retryExponentialBackoff;

    /**
     * Maximum days to retain failed scans in retry queue.
     * After this period, scans are permanently removed regardless of attempts.
     * Prevents unbounded growth of retry table.
     */
    @ConfigProperty(name = "osh.retry.retention-days", defaultValue = "7")
    int retryRetentionDays;

    /**
     * Cleanup schedule interval for removing expired retry records.
     * Runs as separate scheduled job to avoid impacting polling cycle.
     */
    @ConfigProperty(name = "osh.retry.cleanup-interval", defaultValue = "24h")
    String retryCleanupInterval;

    /**
     * Validates configuration at startup.
     * Fails fast if required properties are missing when OSH is enabled.
     */
    @PostConstruct
    public void validateConfiguration() {
        if (!enabled) {
            LOGGER.info("OSH integration is disabled");
            return;
        }

        LOGGER.info("OSH integration is enabled - validating configuration");

        // Load packages from file (skip if already set for testing)
        if (!packageNameSetOverridden) {
            packageNameSet = loadPackageListFromFile();
        } else {
            LOGGER.debug("Package set already configured (test mode), skipping file load");
        }

        if (packageNameSet.isEmpty()) {
            LOGGER.warn("OSH package filter is empty - no packages will be monitored");
        } else {
            LOGGER.info("Loaded {} packages for monitoring", packageNameSet.size());
            LOGGER.debug("Packages to monitor: {}", packageNameSet);
        }

        if (batchSize <= 0) {
            throw new IllegalStateException("Invalid osh.batch.size: " + batchSize + " (must be positive)");
        }

        if (startScanId < 0) {
            throw new IllegalStateException("Invalid osh.scan.start-id: " + startScanId + " (must be non-negative)");
        }

        if (maxScansPerCycle <= 0) {
            throw new IllegalStateException(
                    "Invalid osh.scan.max-per-cycle: " + maxScansPerCycle + " (must be positive)");
        }

        // Validate retry configuration
        if (retryMaxAttempts < -1) {
            throw new IllegalStateException(
                    "Invalid osh.retry.max-attempts: " + retryMaxAttempts + " (must be >= -1, where -1 = unlimited)");
        }

        if (retryBatchSize <= 0) {
            throw new IllegalStateException("Invalid osh.retry.batch-size: " + retryBatchSize + " (must be positive)");
        }

        try {
            Duration.parse(retryBackoffDuration);
        } catch (DateTimeParseException e) {
            throw new IllegalStateException(
                    "Invalid osh.retry.backoff-minutes: " + retryBackoffDuration + " (must be positive)");
        }

        if (retryRetentionDays <= 0) {
            throw new IllegalStateException(
                    "Invalid osh.retry.retention-days: " + retryRetentionDays + " (must be positive)");
        }

        try {
            Duration.parse("PT"
                    + retryCleanupInterval.replace("h", "H").replace("m", "M").replace("s", "S"));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid osh.retry.cleanup-interval: " + retryCleanupInterval
                    + " (must be valid duration like '1h', '30m', '24h')");
        }

        LOGGER.debug("OSH configuration validated successfully:");
        LOGGER.debug("  Base URL: {}", baseUrl);
        LOGGER.debug("  Packages: {}", packageNameSet.isEmpty() ? "none (monitoring disabled)" : packageNameSet);
        LOGGER.debug("  Batch size: {}", batchSize);
        LOGGER.debug("  Poll interval: {}", pollInterval);
        LOGGER.debug("  Start scan ID: {}", startScanId);
        LOGGER.debug("  Max scans per cycle: {}", maxScansPerCycle);
        LOGGER.debug("  Retry max attempts: {}", retryMaxAttempts == -1 ? "unlimited" : retryMaxAttempts);
        LOGGER.debug("  Retry batch size: {}", retryBatchSize);
        LOGGER.debug("  Retry backoff minutes: {}", retryBackoffDuration);
        LOGGER.debug("  Retry exponential backoff: {}", retryExponentialBackoff);
        LOGGER.debug("  Retry retention days: {}", retryRetentionDays);
        LOGGER.debug("  Retry cleanup interval: {}", retryCleanupInterval);
    }

    /**
     * Loads package list from the configured packages file.
     *
     * File format:
     * - One package name per line
     * - Blank lines are ignored
     * - Lines starting with # are treated as comments
     * - Leading/trailing whitespace is trimmed
     *
     * @return immutable set of package names to monitor
     */
    private Set<String> loadPackageListFromFile() {
        Path path = Path.of(packagesFilePath);

        try {
            LOGGER.info("Loading package list from file: {}", packagesFilePath);
            Set<String> packages = Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !line.startsWith("#"))
                    .collect(Collectors.toUnmodifiableSet());

            LOGGER.info("Successfully loaded {} packages from file: {}", packages.size(), packagesFilePath);
            return packages;
        } catch (IOException e) {
            LOGGER.error("Failed to read package list from file: {}", packagesFilePath, e);
            throw new IllegalStateException("Cannot load package list from " + packagesFilePath, e);
        }
    }

    /**
     * Checks if a package should be monitored based on configuration.
     *
     * @param packageName package name to check (must not be null)
     * @return true if package should be monitored, false otherwise
     */
    public boolean shouldMonitorPackage(@NonNull String packageName) {
        if (packageNameSet.isEmpty()) {
            return false;
        }

        return packageNameSet.contains(packageName);
    }

    /**
     * Reloads the package list from the packages file.
     *
     * @return number of packages loaded
     * @throws IllegalStateException if the file cannot be read
     */
    public synchronized int reloadPackageList() {
        if (packageNameSetOverridden) {
            LOGGER.warn("Package set was overridden for testing - reload skipped");
            return packageNameSet.size();
        }

        int previousCount = packageNameSet.size();
        LOGGER.debug("Reloading package list from file: {}", packagesFilePath);

        Set<String> newPackageSet = loadPackageListFromFile();
        packageNameSet = newPackageSet;

        LOGGER.debug(
                "Package list reloaded successfully. Previous count: {}, New count: {}",
                previousCount,
                packageNameSet.size());
        LOGGER.debug("Updated packages to monitor: {}", packageNameSet);

        return packageNameSet.size();
    }

    /**
     * Sets package list directly for testing purposes only.
     *
     * @param packages set of packages to monitor (null will be converted to empty set)
     */
    void setPackageNameSetForTesting(Set<String> packages) {
        this.packageNameSet = packages != null ? Set.copyOf(packages) : Set.of();
        this.packageNameSetOverridden = true;
    }

    /**
     * Returns effective batch size, constrained by max scans per cycle.
     *
     * @return batch size to use for polling
     */
    public int getEffectiveBatchSize() {
        return Math.min(batchSize, maxScansPerCycle);
    }

    // ==================== Retry Logic ====================

    /**
     * Calculates actual backoff time for a given attempt number.
     * Implements exponential backoff strategy if enabled.
     *
     * @param attemptNumber current attempt number
     * @return backoff time in minutes
     */
    public long calculateBackoffMinutes(int attemptNumber) {
        long retryInMinutes = Duration.parse(retryBackoffDuration).toMinutes();
        if (!retryExponentialBackoff || attemptNumber <= 1) {
            return retryInMinutes;
        }

        long calculatedBackoff = retryInMinutes * (long) Math.pow(2.0, attemptNumber - 1.0);

        return Math.min(calculatedBackoff, MAX_BACKOFF_MINUTES);
    }

    /**
     * Gets the cutoff time for retry eligibility based on backoff strategy.
     * Scans are only eligible for retry if their last attempt was before this time.
     *
     * @param attemptNumber attempt number for exponential backoff calculation
     * @return cutoff timestamp - only retry scans attempted before this time
     */
    public Instant getRetryCutoffTime(int attemptNumber) {
        long calculatedBackoffMinutes = calculateBackoffMinutes(attemptNumber);
        return Instant.now().minusSeconds(calculatedBackoffMinutes * 60);
    }

    /**
     * Gets the cutoff time for standard retry eligibility.
     * Uses base backoff time for scans with unknown attempt counts.
     *
     * @return cutoff timestamp for basic retry eligibility
     */
    public Instant getStandardRetryCutoffTime() {
        return Instant.now().minus(Duration.parse(retryBackoffDuration));
    }

    /**
     * Gets the cutoff time for retention cleanup.
     * Scans created before this time should be permanently removed.
     *
     * @return cutoff timestamp for retention policy
     */
    public Instant getRetentionCutoffTime() {
        return Instant.now().minusSeconds(retryRetentionDays * 86400L);
    }

    /**
     * Checks if retry attempts should be limited.
     * Returns false if maxAttempts is -1 (unlimited retries).
     *
     * @return true if retry attempts should be counted and limited
     */
    public boolean hasRetryLimit() {
        return retryMaxAttempts >= 0;
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
