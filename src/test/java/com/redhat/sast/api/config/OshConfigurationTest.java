package com.redhat.sast.api.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Unit tests for OshConfiguration.
 *
 * Tests cover:
 * - Configuration validation at startup
 * - Package filtering logic
 * - Effective batch size calculation
 * - Edge cases and error conditions
 */
@QuarkusTest
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
        config.packages = Optional.of(Set.of("systemd", "kernel"));
        config.batchSize = 10;
        config.startScanId = 1000;
        config.maxScansPerCycle = 50;
        config.retryMaxAttempts = 3;
        config.retryBatchSize = 5;
        config.retryBackoffMinutes = 20;
        config.retryRetentionDays = 7;
        config.retryCleanupInterval = "24h";

        assertDoesNotThrow(() -> config.validateConfiguration());
    }

    @Test
    @DisplayName("Should fail validation with invalid batch size")
    void testValidateConfiguration_InvalidBatchSize() {
        config.enabled = true;
        config.baseUrl = "https://cov01.lab.eng.brq2.redhat.com";
        config.packages = Optional.empty();
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
        config.packages = Optional.empty();
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
        config.packages = Optional.empty();
        config.batchSize = 10;
        config.startScanId = 1000;
        config.maxScansPerCycle = 0; // Invalid

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> config.validateConfiguration());
        assertTrue(exception.getMessage().contains("osh.scan.max-per-cycle"));
    }

    @Test
    @DisplayName("Should accept zero start scan ID")
    void testValidateConfiguration_ZeroStartScanId() {
        config.enabled = true;
        config.baseUrl = "https://cov01.lab.eng.brq2.redhat.com";
        config.packages = Optional.empty();
        config.batchSize = 10;
        config.startScanId = 0; // Valid edge case
        config.maxScansPerCycle = 50;
        config.retryMaxAttempts = 3;
        config.retryBatchSize = 5;
        config.retryBackoffMinutes = 20;
        config.retryRetentionDays = 7;
        config.retryCleanupInterval = "24h";

        assertDoesNotThrow(() -> config.validateConfiguration());
    }

    @Test
    @DisplayName("Should monitor all packages when none configured")
    void testShouldMonitorPackage_NoPackagesConfigured() {
        config.enabled = true;
        config.packages = Optional.empty();

        assertTrue(config.shouldMonitorPackage("systemd"));
        assertTrue(config.shouldMonitorPackage("kernel"));
        assertTrue(config.shouldMonitorPackage("any-package"));
    }

    @Test
    @DisplayName("Should monitor all packages when empty set configured")
    void testShouldMonitorPackage_EmptyPackageSet() {
        config.enabled = true;
        config.packages = Optional.of(Set.of());

        assertTrue(config.shouldMonitorPackage("systemd"));
        assertTrue(config.shouldMonitorPackage("kernel"));
        assertTrue(config.shouldMonitorPackage("any-package"));
    }

    @Test
    @DisplayName("Should monitor only configured packages")
    void testShouldMonitorPackage_SpecificPackages() {
        config.enabled = true;
        config.packages = Optional.of(Set.of("systemd", "kernel", "glibc"));

        assertTrue(config.shouldMonitorPackage("systemd"));
        assertTrue(config.shouldMonitorPackage("kernel"));
        assertTrue(config.shouldMonitorPackage("glibc"));
        assertFalse(config.shouldMonitorPackage("httpd"));
        assertFalse(config.shouldMonitorPackage("unknown-package"));
    }

    @Test
    @DisplayName("Should not monitor any packages when disabled")
    void testShouldMonitorPackage_Disabled() {
        config.enabled = false;
        config.packages = Optional.of(Set.of("systemd", "kernel"));

        assertFalse(config.shouldMonitorPackage("systemd"));
        assertFalse(config.shouldMonitorPackage("kernel"));
        assertFalse(config.shouldMonitorPackage("any-package"));
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
    @DisplayName("Should handle null package name gracefully")
    void testShouldMonitorPackage_NullPackageName() {
        config.enabled = true;
        config.packages = Optional.of(Set.of("systemd", "kernel"));

        assertFalse(config.shouldMonitorPackage(null));
    }

    @Test
    @DisplayName("Should handle empty package name")
    void testShouldMonitorPackage_EmptyPackageName() {
        config.enabled = true;
        config.packages = Optional.of(Set.of("systemd", "kernel"));

        assertFalse(config.shouldMonitorPackage(""));
        assertFalse(config.shouldMonitorPackage("  ")); // Whitespace only
    }

    @Test
    @DisplayName("Should be case sensitive for package names")
    void testShouldMonitorPackage_CaseSensitive() {
        config.enabled = true;
        config.packages = Optional.of(Set.of("systemd", "kernel"));

        assertTrue(config.shouldMonitorPackage("systemd"));
        assertFalse(config.shouldMonitorPackage("Systemd"));
        assertFalse(config.shouldMonitorPackage("SYSTEMD"));
        assertFalse(config.shouldMonitorPackage("SystemD"));
    }
}
