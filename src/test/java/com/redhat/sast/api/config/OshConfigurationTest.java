package com.redhat.sast.api.config;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for OshConfiguration.
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
        config.enabled = false; // Disable OSH to skip file loading
        config.baseUrl = "https://cov01.lab.eng.brq2.redhat.com";
        config.setPackageNameSetForTesting(Set.of("systemd", "kernel"));
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
        config.setPackageNameSetForTesting(Set.of("systemd")); // Set packages to skip file loading
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
        config.setPackageNameSetForTesting(Set.of("systemd")); // Set packages to skip file loading
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
        config.setPackageNameSetForTesting(Set.of("systemd")); // Set packages to skip file loading
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
        config.setPackageNameSetForTesting(Set.of()); // Empty package set
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
        config.setPackageNameSetForTesting(Set.of("systemd")); // Set packages to skip file loading
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
        config.setPackageNameSetForTesting(Set.of());

        assertFalse(config.shouldMonitorPackage("systemd"));
        assertFalse(config.shouldMonitorPackage("kernel"));
        assertFalse(config.shouldMonitorPackage("any-package"));
    }

    @Test
    @DisplayName("Should monitor only configured packages")
    void testShouldMonitorPackage_SpecificPackages() {
        config.enabled = true;
        config.setPackageNameSetForTesting(Set.of("systemd", "kernel", "glibc"));

        assertTrue(config.shouldMonitorPackage("systemd"));
        assertTrue(config.shouldMonitorPackage("kernel"));
        assertTrue(config.shouldMonitorPackage("glibc"));
        assertFalse(config.shouldMonitorPackage("httpd"));
        assertFalse(config.shouldMonitorPackage("unknown-package"));
    }

    @ParameterizedTest
    @MethodSource("effectiveBatchSizeTestCases")
    @DisplayName("Should return correct effective batch size based on configured values")
    void testGetEffectiveBatchSize(
            int batchSize, int maxScansPerCycle, int expectedEffectiveBatchSize, String description) {
        config.batchSize = batchSize;
        config.maxScansPerCycle = maxScansPerCycle;

        int actualEffectiveBatchSize = config.getEffectiveBatchSize();

        assertEquals(expectedEffectiveBatchSize, actualEffectiveBatchSize, description);
    }

    static Stream<Arguments> effectiveBatchSizeTestCases() {
        return Stream.of(
                Arguments.of(10, 50, 10, "Should return batch size when smaller than max per cycle"),
                Arguments.of(100, 25, 25, "Should return max per cycle when smaller than batch size"),
                Arguments.of(30, 30, 30, "Should return same value when batch size equals max per cycle"));
    }

    @Test
    @DisplayName("Should handle empty package name")
    void testShouldMonitorPackage_EmptyPackageName() {
        config.enabled = true;
        config.setPackageNameSetForTesting(Set.of("systemd", "kernel"));

        assertFalse(config.shouldMonitorPackage(""));
        assertFalse(config.shouldMonitorPackage("  ")); // Whitespace only
    }

    @Test
    @DisplayName("Should be case sensitive for package names")
    void testShouldMonitorPackage_CaseSensitive() {
        config.enabled = true;
        config.setPackageNameSetForTesting(Set.of("systemd", "kernel"));

        assertTrue(config.shouldMonitorPackage("systemd"));
        assertFalse(config.shouldMonitorPackage("Systemd"));
        assertFalse(config.shouldMonitorPackage("SYSTEMD"));
        assertFalse(config.shouldMonitorPackage("SystemD"));
    }

    // ==================== Retry Configuration Tests ====================

    @Test
    @DisplayName("Should calculate exponential backoff times correctly")
    void testBackoffCalculation() {
        // Set up retry configuration
        config.retryBackoffDuration = "PT20M"; // 20 minutes base
        config.retryExponentialBackoff = true;

        // Exponential backoff: 20 * 2^(n-1)
        // Attempt 1: 20 * 2^0 = 20 minutes
        // Attempt 2: 20 * 2^1 = 40 minutes
        // Attempt 3: 20 * 2^2 = 80 minutes
        assertEquals(20, config.calculateBackoffMinutes(1), "First attempt should be base backoff");
        assertEquals(40, config.calculateBackoffMinutes(2), "Second attempt should double");
        assertEquals(80, config.calculateBackoffMinutes(3), "Third attempt should quadruple");

        // Attempt 0 or negative should return base backoff
        assertEquals(20, config.calculateBackoffMinutes(0), "Attempt 0 should return base backoff");
        assertEquals(20, config.calculateBackoffMinutes(-1), "Negative attempt should return base backoff");

        // Test maximum cap (24 hours = 1440 minutes)
        long attempt7 = config.calculateBackoffMinutes(7);
        assertEquals(1280, attempt7, "Attempt 7 should be 1280 minutes");

        long attempt8 = config.calculateBackoffMinutes(8);
        assertEquals(1440, attempt8, "Attempt 8 should be capped at 1440 minutes");

        long attempt10 = config.calculateBackoffMinutes(10);
        assertEquals(1440, attempt10, "Attempt 10 should be capped at 1440 minutes");
    }

    @Test
    @DisplayName("Should calculate constant backoff when exponential is disabled")
    void testConstantBackoffCalculation() {
        config.retryBackoffDuration = "PT30M";
        config.retryExponentialBackoff = false;

        assertEquals(30, config.calculateBackoffMinutes(1), "Should use constant backoff");
        assertEquals(30, config.calculateBackoffMinutes(2), "Should use constant backoff");
        assertEquals(30, config.calculateBackoffMinutes(3), "Should use constant backoff");
        assertEquals(30, config.calculateBackoffMinutes(10), "Should use constant backoff");
    }

    @Test
    @DisplayName("Should calculate cutoff times with correct time differences")
    void testCutoffTimeCalculation() {
        config.retryBackoffDuration = "PT20M";
        config.retryRetentionDays = 7;

        Instant now = Instant.now();
        Instant cutoff = config.getStandardRetryCutoffTime();

        assertNotNull(cutoff);
        assertTrue(cutoff.isBefore(now), "Cutoff should be in the past");

        // Verify cutoff is approximately 20 minutes ago (allow 1 minute tolerance)
        long minutesDiff = Duration.between(cutoff, now).toMinutes();
        assertTrue(
                minutesDiff >= 19 && minutesDiff <= 21,
                "Cutoff should be ~20 minutes ago, was: " + minutesDiff + " minutes");

        Instant retentionCutoff = config.getRetentionCutoffTime();
        assertNotNull(retentionCutoff);
        assertTrue(retentionCutoff.isBefore(now), "Retention cutoff should be in the past");

        // Verify retention cutoff is approximately 7 days ago
        long daysDiff = Duration.between(retentionCutoff, now).toDays();
        assertTrue(
                daysDiff >= 6 && daysDiff <= 7, "Retention cutoff should be ~7 days ago, was: " + daysDiff + " days");
    }

    @Test
    @DisplayName("Should handle attempt-specific cutoff times with exponential backoff")
    void testAttemptSpecificCutoffTimes() {
        config.retryBackoffDuration = "PT20M";
        config.retryExponentialBackoff = true;

        Instant now = Instant.now();
        Instant cutoff1 = config.getRetryCutoffTime(1); // 20 minutes ago
        Instant cutoff2 = config.getRetryCutoffTime(2); // 40 minutes ago
        Instant cutoff3 = config.getRetryCutoffTime(3); // 80 minutes ago

        assertNotNull(cutoff1);
        assertNotNull(cutoff2);
        assertNotNull(cutoff3);

        assertTrue(cutoff1.isBefore(now));
        assertTrue(cutoff2.isBefore(now));
        assertTrue(cutoff3.isBefore(now));

        // Exponential backoff means cutoff3 < cutoff2 < cutoff1 < now
        assertTrue(cutoff3.isBefore(cutoff2), "Attempt 3 cutoff should be earlier than attempt 2");
        assertTrue(cutoff2.isBefore(cutoff1), "Attempt 2 cutoff should be earlier than attempt 1");

        // Verify approximate time differences (with tolerance)
        long diff1 = java.time.Duration.between(cutoff1, now).toMinutes();
        long diff2 = java.time.Duration.between(cutoff2, now).toMinutes();
        long diff3 = java.time.Duration.between(cutoff3, now).toMinutes();

        assertTrue(diff1 >= 19 && diff1 <= 21, "Attempt 1 cutoff should be ~20 min ago");
        assertTrue(diff2 >= 38 && diff2 <= 42, "Attempt 2 cutoff should be ~40 min ago");
        assertTrue(diff3 >= 78 && diff3 <= 82, "Attempt 3 cutoff should be ~80 min ago");
    }

    @Test
    @DisplayName("Should return configured retry batch size when under limit")
    void testEffectiveRetryBatchSize() {
        config.retryBatchSize = 5;

        int batchSize = config.getEffectiveRetryBatchSize();
        assertEquals(5, batchSize, "Should return configured batch size when under limit");

        // Test with another value under the limit
        config.retryBatchSize = 30;
        batchSize = config.getEffectiveRetryBatchSize();
        assertEquals(30, batchSize, "Should return configured batch size");

        // Test with exactly the maximum
        config.retryBatchSize = 50;
        batchSize = config.getEffectiveRetryBatchSize();
        assertEquals(50, batchSize, "Should return max when configured at max");
    }

    @Test
    @DisplayName("Should cap retry batch size at maximum (50)")
    void testEffectiveRetryBatchSize_capped() {
        config.retryBatchSize = 100;
        int cappedBatchSize = config.getEffectiveRetryBatchSize();
        assertEquals(50, cappedBatchSize, "Should cap at maximum of 50");

        config.retryBatchSize = 1000;
        cappedBatchSize = config.getEffectiveRetryBatchSize();
        assertEquals(50, cappedBatchSize, "Should cap large values at 50");
    }

    @Test
    @DisplayName("Should handle retry limits correctly")
    void testRetryLimits() {
        // With retryMaxAttempts = 3, should have a limit
        config.setRetryMaxAttempts(3);
        assertTrue(config.hasRetryLimit());

        // Test when retryMaxAttempts = -1 (unlimited)
        config.setRetryMaxAttempts(-1);
        assertFalse(config.hasRetryLimit());

        // Test when retryMaxAttempts = 0 (no retries)
        config.setRetryMaxAttempts(0);
        assertTrue(config.hasRetryLimit());
        assertEquals(0, config.getRetryMaxAttempts());
    }
}
