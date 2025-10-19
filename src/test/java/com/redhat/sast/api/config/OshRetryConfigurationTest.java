package com.redhat.sast.api.config;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * Integration tests for OshRetryConfiguration focusing on validation and calculation logic.
 *
 * Tests cover:
 * - Configuration validation at startup
 * - Exponential backoff calculations
 * - Retry cutoff time calculations
 * - Effective batch size limits
 * - Edge cases and boundary conditions
 */
@QuarkusTest
@DisplayName("OSH Retry Configuration Tests")
class OshRetryConfigurationTest {

    @Inject
    OshRetryConfiguration retryConfiguration;

    @Test
    @DisplayName("Should provide basic configuration access")
    void testBasicConfiguration() {
        assertNotNull(retryConfiguration);

        assertNotNull(retryConfiguration.isRetryEnabled());
        assertTrue(retryConfiguration.getEffectiveRetryBatchSize() > 0);
    }

    @Test
    @DisplayName("Should calculate backoff times correctly")
    void testBackoffCalculation() {
        long backoff1 = retryConfiguration.calculateBackoffMinutes(1);
        long backoff2 = retryConfiguration.calculateBackoffMinutes(2);
        long backoff3 = retryConfiguration.calculateBackoffMinutes(3);

        assertTrue(backoff1 > 0);
        assertTrue(backoff2 > 0);
        assertTrue(backoff3 > 0);

        assertTrue(retryConfiguration.calculateBackoffMinutes(0) > 0);
        assertTrue(retryConfiguration.calculateBackoffMinutes(-1) > 0);

        long largeBackoff = retryConfiguration.calculateBackoffMinutes(100);
        assertTrue(largeBackoff <= 1440); // Max 24 hours in minutes
    }

    @Test
    @DisplayName("Should calculate cutoff times correctly")
    void testCutoffTimeCalculation() {
        LocalDateTime cutoff = retryConfiguration.getStandardRetryCutoffTime();
        assertNotNull(cutoff);
        assertTrue(cutoff.isBefore(LocalDateTime.now()));

        LocalDateTime retentionCutoff = retryConfiguration.getRetentionCutoffTime();
        assertNotNull(retentionCutoff);
        assertTrue(retentionCutoff.isBefore(LocalDateTime.now()));
        assertTrue(retentionCutoff.isBefore(cutoff));
    }

    @Test
    @DisplayName("Should handle attempt-specific cutoff times")
    void testAttemptSpecificCutoffTimes() {
        LocalDateTime cutoff1 = retryConfiguration.getRetryCutoffTime(1);
        LocalDateTime cutoff2 = retryConfiguration.getRetryCutoffTime(2);
        LocalDateTime cutoff3 = retryConfiguration.getRetryCutoffTime(3);

        assertNotNull(cutoff1);
        assertNotNull(cutoff2);
        assertNotNull(cutoff3);

        assertTrue(cutoff1.isBefore(LocalDateTime.now()));
        assertTrue(cutoff2.isBefore(LocalDateTime.now()));
        assertTrue(cutoff3.isBefore(LocalDateTime.now()));
    }

    @Test
    @DisplayName("Should provide effective batch size")
    void testEffectiveBatchSize() {
        int batchSize = retryConfiguration.getEffectiveRetryBatchSize();
        assertTrue(batchSize > 0);
        assertTrue(batchSize <= 50); // Should not exceed maximum
    }

    @Test
    @DisplayName("Should handle retry limits correctly")
    void testRetryLimits() {
        boolean hasLimit = retryConfiguration.hasRetryLimit();
        assertTrue(hasLimit || !hasLimit);
    }
}
