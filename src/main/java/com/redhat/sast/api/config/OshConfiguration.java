package com.redhat.sast.api.config;

import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for OSH (Open Scan Hub) integration.
 *
 * Provides validated configuration properties for:
 * - OSH API connection settings
 * - Package filtering options
 * - Polling behavior configuration
 * - Feature toggle support
 */
@ApplicationScoped
@Getter
@Slf4j
public class OshConfiguration {

    /**
     * Master toggle for OSH integration.
     * When false, all OSH operations are disabled.
     */
    @ConfigProperty(name = "osh.integration.enabled", defaultValue = "false")
    boolean enabled;

    /**
     * OSH API base URL.
     * Required when OSH integration is enabled.
     * Example: https://cov01.lab.eng.brq2.redhat.com
     */
    @ConfigProperty(name = "osh.api.base-url")
    Optional<String> baseUrl = Optional.empty();

    /**
     * Comma-separated list of package names to monitor.
     * Only scans for these packages will be processed.
     * Example: systemd,kernel,glibc
     */
    @ConfigProperty(name = "osh.packages")
    Optional<List<String>> packages = Optional.empty();

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

        // Validate required properties
        if (baseUrl.isEmpty()) {
            throw new IllegalStateException("OSH integration enabled but 'osh.api.base-url' is not configured");
        }

        if (packages.isEmpty() || packages.map(List::isEmpty).orElse(true)) {
            LOGGER.warn("No packages configured for OSH monitoring - will process all scans");
        }

        // Validate numeric properties
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

        LOGGER.debug("OSH configuration validated successfully:");
        LOGGER.debug("  Base URL: {}", baseUrl.get());
        LOGGER.debug("  Packages: {}", packages.orElse(List.of("all")));
        LOGGER.debug("  Batch size: {}", batchSize);
        LOGGER.debug("  Poll interval: {}", pollInterval);
        LOGGER.debug("  Start scan ID: {}", startScanId);
        LOGGER.debug("  Max scans per cycle: {}", maxScansPerCycle);
    }

    /**
     * Checks if a package should be monitored based on configuration.
     *
     * @param packageName package name to check
     * @return true if package should be monitored, false otherwise
     */
    public boolean shouldMonitorPackage(String packageName) {
        if (!enabled) {
            return false;
        }

        // If no packages configured, monitor all
        if (packages.isEmpty() || packages.map(List::isEmpty).orElse(true)) {
            return true;
        }

        if (packageName == null) {
            return false;
        }

        // Check if package is in the configured list
        return packages.get().contains(packageName);
    }

    /**
     * Returns effective batch size, constrained by max scans per cycle.
     *
     * @return batch size to use for polling
     */
    public int getEffectiveBatchSize() {
        return Math.min(batchSize, maxScansPerCycle);
    }
}
