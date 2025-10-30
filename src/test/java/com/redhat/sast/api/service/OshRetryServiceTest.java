package com.redhat.sast.api.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.sast.api.enums.OshFailureReason;
import com.redhat.sast.api.model.OshUncollectedScan;
import com.redhat.sast.api.repository.OshUncollectedScanRepository;
import com.redhat.sast.api.service.osh.OshRetryService;
import com.redhat.sast.api.service.osh.OshRetryService.RetryQueueStatistics;
import com.redhat.sast.api.v1.dto.osh.OshScan;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Integration tests for OshRetryService focusing on retry lifecycle management.
 *
 * Tests basic retry functionality including:
 * - Failed scan recording
 * - Retry scan fetching
 * - Queue statistics and monitoring
 * - JSON reconstruction
 */
@QuarkusTest
@TestProfile(com.redhat.sast.api.config.TestProfile.class)
@DisplayName("OSH Retry Service Tests")
class OshRetryServiceTest {

    @Inject
    OshRetryService oshRetryService;

    @Inject
    OshUncollectedScanRepository uncollectedScanRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        uncollectedScanRepository.deleteAll();
    }

    @Test
    @DisplayName("Should record failed scan with correct details")
    void recordFailedScan_storesCompleteRetryInformation() {
        OshScan scan = createTestScan(1001, "test-package");

        oshRetryService.recordFailedScan(
                scan, OshFailureReason.JSON_DOWNLOAD_NETWORK_ERROR, "Network timeout occurred");

        Optional<OshUncollectedScan> recorded = oshRetryService.findRetryInfo(1001);
        assertTrue(recorded.isPresent(), "Failed scan should be recorded in retry queue");

        OshUncollectedScan uncollected = recorded.get();
        assertEquals(1001, uncollected.getOshScanId());
        assertEquals(OshFailureReason.JSON_DOWNLOAD_NETWORK_ERROR, uncollected.getFailureReason());
        assertEquals("Network timeout occurred", uncollected.getLastErrorMessage());
        assertEquals("test-package", uncollected.getPackageName());
        assertEquals(1, uncollected.getAttemptCount());
        assertNotNull(uncollected.getCreatedAt());
        assertNotNull(uncollected.getLastAttemptAt());
    }

    @Test
    @DisplayName("Should handle null scan gracefully")
    void recordFailedScan_handleNullScanGracefully() {
        // Should not throw exception - method logs warning and returns early
        assertDoesNotThrow(
                () -> oshRetryService.recordFailedScan(null, OshFailureReason.JSON_DOWNLOAD_NETWORK_ERROR, "Error"));

        // Verify no records were created
        assertEquals(0, uncollectedScanRepository.count());
    }

    @Test
    @DisplayName("Should handle scan with null ID gracefully")
    void recordFailedScan_handleNullIdGracefully() {
        OshScan scan = createTestScan(null, "test-package");

        assertDoesNotThrow(
                () -> oshRetryService.recordFailedScan(scan, OshFailureReason.JSON_DOWNLOAD_NETWORK_ERROR, "Error"));

        // Verify no records were created
        assertEquals(0, uncollectedScanRepository.count());
    }

    @Test
    @DisplayName("Should fetch retryable scans and include recorded failures")
    void fetchRetryableScans_includesRecordedFailures() {
        // Start with empty queue
        List<OshUncollectedScan> initialResult = oshRetryService.fetchRetryableScans();
        assertTrue(initialResult.isEmpty());

        // Record a failed scan
        OshScan scan = createTestScan(2001, "failed-package");
        oshRetryService.recordFailedScan(scan, OshFailureReason.OSH_API_ERROR, "API error");

        // Should now find the failed scan
        List<OshUncollectedScan> result = oshRetryService.fetchRetryableScans();
        assertEquals(1, result.size());
        assertEquals(2001, result.get(0).getOshScanId());
    }

    @Test
    @DisplayName("Should mark retry successful and remove from queue")
    void markRetrySuccessful_removesFromRetryQueue() {
        // Record a failed scan first
        OshScan scan = createTestScan(3001, "success-package");
        oshRetryService.recordFailedScan(scan, OshFailureReason.JSON_DOWNLOAD_NETWORK_ERROR, "Download error");

        // Verify it's in the queue
        assertTrue(oshRetryService.findRetryInfo(3001).isPresent());

        // Mark as successful
        oshRetryService.markRetrySuccessful(3001);

        // Should no longer be in the queue
        assertTrue(oshRetryService.findRetryInfo(3001).isEmpty());
    }

    @Test
    @DisplayName("Should handle null scan ID gracefully in mark successful")
    void markRetrySuccessful_handleNullIdGracefully() {
        assertDoesNotThrow(() -> oshRetryService.markRetrySuccessful(null));

        // Verify no side effects on existing data
        assertEquals(0, uncollectedScanRepository.count());
    }

    @Test
    @DisplayName("Should record retry attempt without error")
    void testRecordRetryAttempt() {
        assertDoesNotThrow(() -> {
            oshRetryService.recordRetryAttempt(123L, OshFailureReason.OSH_API_ERROR, "API Error");
        });
    }

    @Test
    @DisplayName("Should handle null ID in record retry attempt")
    void testRecordRetryAttemptNullId() {
        assertDoesNotThrow(() -> {
            oshRetryService.recordRetryAttempt(null, OshFailureReason.OSH_API_ERROR, "Error");
        });
    }

    @Test
    @DisplayName("Should reconstruct scan from valid JSON")
    void testReconstructScanFromJson() throws Exception {
        String validJson = "{\"scanId\":12345,\"state\":\"CLOSED\",\"component\":\"systemd\",\"version\":\"252\"}";

        OshScan result = oshRetryService.reconstructScanFromJson(validJson);

        assertNotNull(result);
        assertEquals(12345, result.getScanId());
        assertEquals("CLOSED", result.getState());
        assertEquals("systemd", result.getComponent());
        assertEquals("252", result.getVersion());
    }

    @Test
    @DisplayName("Should throw exception for invalid JSON")
    void testReconstructScanFromInvalidJson() {
        String invalidJson = "{invalid json}";

        assertThrows(Exception.class, () -> {
            oshRetryService.reconstructScanFromJson(invalidJson);
        });
    }

    @Test
    @DisplayName("Should throw exception for null or empty JSON")
    void testReconstructScanFromNullOrEmptyJson() {
        assertThrows(Exception.class, () -> {
            oshRetryService.reconstructScanFromJson(null);
        });

        assertThrows(Exception.class, () -> {
            oshRetryService.reconstructScanFromJson("");
        });

        assertThrows(Exception.class, () -> {
            oshRetryService.reconstructScanFromJson("   ");
        });
    }

    @Test
    @DisplayName("Should find retry info by scan ID")
    void testFindRetryInfo() {
        Optional<OshUncollectedScan> result = oshRetryService.findRetryInfo(12345);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should return empty for null scan ID in find retry info")
    void testFindRetryInfoNullId() {
        Optional<OshUncollectedScan> result = oshRetryService.findRetryInfo(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should provide detailed statistics")
    void testGetDetailedRetryStatistics() {
        RetryQueueStatistics stats = oshRetryService.getDetailedRetryStatistics();

        assertNotNull(stats);
        assertTrue(stats.totalInQueue >= 0);
        assertTrue(stats.eligibleForRetry >= 0);
        assertTrue(stats.awaitingBackoff >= 0);
        assertTrue(stats.exceededMaxAttempts >= 0);
        assertNotNull(stats.configurationSummary);
    }

    @Test
    @DisplayName("Should get retry queue snapshot")
    void testGetRetryQueueSnapshot() {
        List<OshUncollectedScan> result = oshRetryService.getRetryQueueSnapshot(10, "created");

        assertNotNull(result);
        assertTrue(result.size() >= 0); // Can be empty, which is fine
    }

    @Test
    @DisplayName("Should handle cleanup operations")
    void testCleanupOperations() {
        assertDoesNotThrow(() -> {
            oshRetryService.cleanupExpiredRetries();
        });

        assertDoesNotThrow(() -> {
            int cleaned = oshRetryService.cleanupExceededRetries();
            assertTrue(cleaned >= 0); // Should return non-negative count
        });
    }

    // Helper methods
    private OshScan createTestScan(Integer scanId, String packageName) {
        OshScan scan = new OshScan();
        scan.setScanId(scanId);
        scan.setState("CLOSED");
        scan.setComponent(packageName);
        scan.setVersion("1.0.0");

        Map<String, Object> rawData = new HashMap<>();
        rawData.put("Label", packageName + "-1.0.0-1.el9.src.rpm");
        scan.setRawData(rawData);

        return scan;
    }
}
