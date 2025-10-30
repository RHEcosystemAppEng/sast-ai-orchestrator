package com.redhat.sast.api.config;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for OshConfiguration.
 *
 * Tests cover:
 * - Configuration validation at startup
 * - Package filtering logic
 * - Effective batch size calculation
 * - Retry mechanism configuration and calculations
 * - Edge cases and error conditions
 */
@DisplayName("OSH Configuration Tests")
class OshConfigurationTest {

    private OshConfiguration config;

    @BeforeEach
    void setUp() {
        config = new OshConfiguration();
    }

    @Test
    @DisplayName("Should validate when OSH is disabled")
    void testValidateConfiguration_Disabled() {
        config.enabled = false;

        assertDoesNotThrow(() -> config.validateConfiguration());
    }

    @Test
    @DisplayName("Should validate when OSH is enabled with valid config")
    void testValidateConfiguration_EnabledValid() {
        config.enabled = true;
        config.baseUrl = "https://cov01.lab.eng.brq2.redhat.com";
        config.packageNameSet = Optional.of(Set.of("systemd", "kernel"));
        config.batchSize = 10;
        config.startScanId = 1000;
        config.maxScansPerCycle = 50;
        config.retryMaxAttempts = 3;
        config.retryBatchSize = 5;
        config.retryBackoffDuration = "PT20M";
        config.retryRetentionDays = 7;
        config.retryCleanupInterval = "24h";

        assertDoesNotThrow(() -> config.validateConfiguration());
    }

    @Test
    @DisplayName("Should fail validation with invalid batch size")
    void testValidateConfiguration_InvalidBatchSize() {
        config.enabled = true;
        config.baseUrl = "https://cov01.lab.eng.brq2.redhat.com";
        config.packageNameSet = Optional.of(Set.of("systemd"));
        config.batchSize = 0; // Invalid

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> config.validateConfiguration());
        assertTrue(exception.getMessage().contains("osh.batch.size"));
    }

    @Test
    @DisplayName("Should fail validation with negative start scan ID")
    void testValidateConfiguration_NegativeStartScanId() {
        config.enabled = true;
        config.baseUrl = "https://cov01.lab.eng.brq2.redhat.com";
        config.packageNameSet = Optional.of(Set.of("systemd"));
        config.batchSize = 10;
        config.startScanId = -1; // Invalid

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> config.validateConfiguration());
        assertTrue(exception.getMessage().contains("osh.scan.start-id"));
    }

    @Test
    @DisplayName("Should fail validation with invalid max scans per cycle")
    void testValidateConfiguration_InvalidMaxScansPerCycle() {
        config.enabled = true;
        config.baseUrl = "https://cov01.lab.eng.brq2.redhat.com";
        config.packageNameSet = Optional.of(Set.of("systemd"));
        config.batchSize = 10;
        config.startScanId = 1000;
        config.maxScansPerCycle = 0; // Invalid

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> config.validateConfiguration());
        assertTrue(exception.getMessage().contains("osh.scan.max-per-cycle"));
    }

    @Test
    @DisplayName("Should allow validation with no packages configured (with warning)")
    void testValidateConfiguration_NoPackagesConfigured() {
        config.enabled = true;
        config.baseUrl = "https://cov01.lab.eng.brq2.redhat.com";
        config.packageNameSet = Optional.empty();
        config.batchSize = 10;
        config.startScanId = 1000;
        config.maxScansPerCycle = 50;
        config.retryMaxAttempts = 3;
        config.retryBatchSize = 5;
        config.retryBackoffDuration = "PT20M";
        config.retryRetentionDays = 7;
        config.retryCleanupInterval = "24h";

        assertDoesNotThrow(() -> config.validateConfiguration());

        assertFalse(config.shouldMonitorPackage("systemd"));
        assertFalse(config.shouldMonitorPackage("any-package"));
    }

    @Test
    @DisplayName("Should accept zero start scan ID")
    void testValidateConfiguration_ZeroStartScanId() {
        config.enabled = true;
        config.baseUrl = "https://cov01.lab.eng.brq2.redhat.com";
        config.packageNameSet = Optional.of(Set.of("systemd"));
        config.batchSize = 10;
        config.startScanId = 0; // Valid edge case
        config.maxScansPerCycle = 50;
        config.retryMaxAttempts = 3;
        config.retryBatchSize = 5;
        config.retryBackoffDuration = "PT20M";
        config.retryRetentionDays = 7;
        config.retryCleanupInterval = "24h";

        assertDoesNotThrow(() -> config.validateConfiguration());
    }

    @Test
    @DisplayName("Should monitor nothing when no packages configured (fail-safe)")
    void testShouldMonitorPackage_NoPackagesConfigured() {
        config.enabled = true;
        config.packageNameSet = Optional.empty();

        assertFalse(config.shouldMonitorPackage("systemd"));
        assertFalse(config.shouldMonitorPackage("kernel"));
        assertFalse(config.shouldMonitorPackage("any-package"));
    }

    @Test
    @DisplayName("Should monitor nothing when empty set configured (fail-safe)")
    void testShouldMonitorPackage_EmptyPackageSet() {
        config.enabled = true;
        config.packageNameSet = Optional.of(Set.of());

        assertFalse(config.shouldMonitorPackage("systemd"));
        assertFalse(config.shouldMonitorPackage("kernel"));
        assertFalse(config.shouldMonitorPackage("any-package"));
    }

    @Test
    @DisplayName("Should monitor only configured packages")
    void testShouldMonitorPackage_SpecificPackages() {
        config.enabled = true;
        config.packageNameSet = Optional.of(Set.of("systemd", "kernel", "glibc"));

        assertTrue(config.shouldMonitorPackage("systemd"));
        assertTrue(config.shouldMonitorPackage("kernel"));
        assertTrue(config.shouldMonitorPackage("glibc"));
        assertFalse(config.shouldMonitorPackage("httpd"));
        assertFalse(config.shouldMonitorPackage("unknown-package"));
    }

    @Test
    @DisplayName("Should return batch size when smaller than max per cycle")
    void testGetEffectiveBatchSize_BatchSizeSmaller() {
        config.batchSize = 10;
        config.maxScansPerCycle = 50;

        int effectiveBatchSize = config.getEffectiveBatchSize();

        assertEquals(10, effectiveBatchSize);
    }

    @Test
    @DisplayName("Should return max per cycle when smaller than batch size")
    void testGetEffectiveBatchSize_MaxPerCycleSmaller() {
        config.batchSize = 100;
        config.maxScansPerCycle = 25;

        int effectiveBatchSize = config.getEffectiveBatchSize();

        assertEquals(25, effectiveBatchSize);
    }

    @Test
    @DisplayName("Should return same value when batch size equals max per cycle")
    void testGetEffectiveBatchSize_Equal() {
        config.batchSize = 30;
        config.maxScansPerCycle = 30;

        int effectiveBatchSize = config.getEffectiveBatchSize();

        assertEquals(30, effectiveBatchSize);
    }

    @Test
    @DisplayName("Should throw NullPointerException for null package name")
    void testShouldMonitorPackage_NullPackageName() {
        config.enabled = true;
        config.packageNameSet = Optional.of(Set.of("systemd", "kernel"));

        assertThrows(NullPointerException.class, () -> config.shouldMonitorPackage(null));
    }

    @Test
    @DisplayName("Should handle empty package name")
    void testShouldMonitorPackage_EmptyPackageName() {
        config.enabled = true;
        config.packageNameSet = Optional.of(Set.of("systemd", "kernel"));

        assertFalse(config.shouldMonitorPackage(""));
        assertFalse(config.shouldMonitorPackage("  ")); // Whitespace only
    }

    @Test
    @DisplayName("Should be case sensitive for package names")
    void testShouldMonitorPackage_CaseSensitive() {
        config.enabled = true;
        config.packageNameSet = Optional.of(Set.of("systemd", "kernel"));

        assertTrue(config.shouldMonitorPackage("systemd"));
        assertFalse(config.shouldMonitorPackage("Systemd"));
        assertFalse(config.shouldMonitorPackage("SYSTEMD"));
        assertFalse(config.shouldMonitorPackage("SystemD"));
    }

    // ==================== Retry Configuration Tests ====================

    @Test
    @DisplayName("Should calculate backoff times correctly")
    void testBackoffCalculation() {
        // Set up retry configuration
        config.retryBackoffDuration = "PT20M";
        config.retryExponentialBackoff = true;

        long backoff1 = config.calculateBackoffMinutes(1);
        long backoff2 = config.calculateBackoffMinutes(2);
        long backoff3 = config.calculateBackoffMinutes(3);

        assertTrue(backoff1 > 0);
        assertTrue(backoff2 > 0);
        assertTrue(backoff3 > 0);

        // Test edge cases
        assertTrue(config.calculateBackoffMinutes(0) > 0);
        assertTrue(config.calculateBackoffMinutes(-1) > 0);

        // Test maximum cap (24 hours = 1440 minutes)
        long largeBackoff = config.calculateBackoffMinutes(100);
        assertTrue(largeBackoff <= 1440); // Max 24 hours in minutes
    }

    @Test
    @DisplayName("Should calculate cutoff times correctly")
    void testCutoffTimeCalculation() {
        // Set up retry configuration
        config.retryBackoffDuration = "PT20M";
        config.retryRetentionDays = 7;

        LocalDateTime cutoff = config.getStandardRetryCutoffTime();
        assertNotNull(cutoff);
        assertTrue(cutoff.isBefore(LocalDateTime.now()));

        LocalDateTime retentionCutoff = config.getRetentionCutoffTime();
        assertNotNull(retentionCutoff);
        assertTrue(retentionCutoff.isBefore(LocalDateTime.now()));
        assertTrue(retentionCutoff.isBefore(cutoff));
    }

    @Test
    @DisplayName("Should handle attempt-specific cutoff times")
    void testAttemptSpecificCutoffTimes() {
        // Set up retry configuration
        config.retryBackoffDuration = "PT20M";

        LocalDateTime cutoff1 = config.getRetryCutoffTime(1);
        LocalDateTime cutoff2 = config.getRetryCutoffTime(2);
        LocalDateTime cutoff3 = config.getRetryCutoffTime(3);

        assertNotNull(cutoff1);
        assertNotNull(cutoff2);
        assertNotNull(cutoff3);

        assertTrue(cutoff1.isBefore(LocalDateTime.now()));
        assertTrue(cutoff2.isBefore(LocalDateTime.now()));
        assertTrue(cutoff3.isBefore(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Should provide effective retry batch size")
    void testEffectiveRetryBatchSize() {
        config.retryBatchSize = 5;

        int batchSize = config.getEffectiveRetryBatchSize();
        assertTrue(batchSize > 0);
        assertTrue(batchSize <= 50); // Should not exceed maximum

        // Test with larger retry batch size
        config.retryBatchSize = 100;
        int cappedBatchSize = config.getEffectiveRetryBatchSize();
        assertEquals(50, cappedBatchSize); // Should be capped at maximum
    }

    @Test
    @DisplayName("Should handle retry limits correctly")
    void testRetryLimits() {
        // With retryMaxAttempts = 3, should have a limit
        config.retryMaxAttempts = 3;
        assertTrue(config.hasRetryLimit());

        // Test when retryMaxAttempts = 0 (unlimited)
        config.retryMaxAttempts = 0;
        assertFalse(config.hasRetryLimit());
    }
}
