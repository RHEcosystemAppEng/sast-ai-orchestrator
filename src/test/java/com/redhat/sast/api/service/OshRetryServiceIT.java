package com.redhat.sast.api.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.sast.api.config.OshConfiguration;
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
class OshRetryServiceIT {

    @Inject
    OshRetryService oshRetryService;

    @Inject
    OshUncollectedScanRepository uncollectedScanRepository;

    @Inject
    OshConfiguration oshConfiguration;

    @Inject
    jakarta.persistence.EntityManager entityManager;

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
        assertEquals(0, uncollected.getAttemptCount());
        assertNotNull(uncollected.getCreatedAt());
        assertNotNull(uncollected.getLastAttemptAt());
    }

    @Test
    @Transactional
    @DisplayName("Should record failed scan with 3 retry attempts")
    void recordFailedScan_capturesRetryInfoCorrectly() {
        OshScan scan = createTestScan(1001, "test-package");

        oshRetryService.recordFailedScan(
                scan, OshFailureReason.JSON_DOWNLOAD_NETWORK_ERROR, "Network timeout occurred");

        Optional<OshUncollectedScan> optionalOshUncollectedScan = oshRetryService.findRetryInfo(1001);
        assertTrue(optionalOshUncollectedScan.isPresent());
        var uncollectedScan = optionalOshUncollectedScan.get();
        assertEquals(OshFailureReason.JSON_DOWNLOAD_NETWORK_ERROR, uncollectedScan.getFailureReason());
        assertEquals(0, uncollectedScan.getAttemptCount());

        oshRetryService.recordRetryAttempt(
                uncollectedScan.getId(), OshFailureReason.OSH_API_ERROR, "My exception message");
        entityManager.clear();
        uncollectedScan = oshRetryService.findRetryInfo(1001).get();
        assertEquals(OshFailureReason.OSH_API_ERROR, uncollectedScan.getFailureReason());
        assertEquals(1, uncollectedScan.getAttemptCount());

        oshRetryService.recordRetryAttempt(
                uncollectedScan.getId(), OshFailureReason.DATABASE_ERROR, "My exception message");
        entityManager.clear();
        uncollectedScan = oshRetryService.findRetryInfo(1001).get();
        assertEquals(OshFailureReason.DATABASE_ERROR, uncollectedScan.getFailureReason());
        assertEquals(2, uncollectedScan.getAttemptCount());

        oshRetryService.recordRetryAttempt(
                uncollectedScan.getId(), OshFailureReason.JOB_CREATION_ERROR, "My exception message");
        entityManager.clear();
        uncollectedScan = oshRetryService.findRetryInfo(1001).get();
        assertEquals(OshFailureReason.JOB_CREATION_ERROR, uncollectedScan.getFailureReason());
        assertEquals(3, uncollectedScan.getAttemptCount());
    }

    @Test
    @DisplayName("Should handle null scan gracefully")
    void recordFailedScan_handleNullScanGracefully() {
        assertDoesNotThrow(
                () -> oshRetryService.recordFailedScan(null, OshFailureReason.JSON_DOWNLOAD_NETWORK_ERROR, "Error"));

        assertEquals(0, uncollectedScanRepository.count());
    }

    @Test
    @DisplayName("Should handle scan with null ID gracefully")
    void recordFailedScan_handleNullIdGracefully() {
        OshScan scan = createTestScan(null, "test-package");

        assertDoesNotThrow(
                () -> oshRetryService.recordFailedScan(scan, OshFailureReason.JSON_DOWNLOAD_NETWORK_ERROR, "Error"));

        assertEquals(0, uncollectedScanRepository.count());
    }

    @Test
    @DisplayName("Should fetch retryable scans and include recorded failures")
    void fetchRetryableScans_includesRecordedFailures() {
        List<OshUncollectedScan> initialResult = oshRetryService.fetchRetryableScans();
        assertTrue(initialResult.isEmpty());

        OshScan scan = createTestScan(2001, "failed-package");
        oshRetryService.recordFailedScan(scan, OshFailureReason.OSH_API_ERROR, "API error");

        oshConfiguration.setRetryBackoffDuration("PT0.001S");
        List<OshUncollectedScan> result = oshRetryService.fetchRetryableScans();
        assertEquals(1, result.size());
        assertEquals(2001, result.getFirst().getOshScanId());
    }

    @Test
    @DisplayName("Should mark retry successful and remove from queue")
    void markRetrySuccessful_removesFromRetryQueue() {
        OshScan scan = createTestScan(3001, "success-package");
        oshRetryService.recordFailedScan(scan, OshFailureReason.JSON_DOWNLOAD_NETWORK_ERROR, "Download error");

        assertTrue(oshRetryService.findRetryInfo(3001).isPresent());

        oshRetryService.markRetrySuccessful(3001);

        assertTrue(oshRetryService.findRetryInfo(3001).isEmpty());
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
    @DisplayName("Should find retry info for existing scan ID")
    void testFindRetryInfo_existing() {
        OshScan scan = createTestScan(12345, "test-package");
        oshRetryService.recordFailedScan(scan, OshFailureReason.OSH_API_ERROR, "Error");

        Optional<OshUncollectedScan> result = oshRetryService.findRetryInfo(12345);

        assertTrue(result.isPresent());
        assertEquals(12345, result.get().getOshScanId());
    }

    @Test
    @DisplayName("Should return empty for null scan ID in find retry info")
    void testFindRetryInfoNullId() {
        Optional<OshUncollectedScan> result = oshRetryService.findRetryInfo(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("Should provide accurate detailed statistics")
    void testGetDetailedRetryStatistics() throws InterruptedException {
        // Set very short backoff FIRST
        oshConfiguration.setRetryBackoffDuration("PT0.001S");

        OshScan scan1 = createTestScan(1001, "package1");
        OshScan scan2 = createTestScan(1002, "package2");
        OshScan scan3 = createTestScan(1003, "package3");

        oshRetryService.recordFailedScan(scan1, OshFailureReason.OSH_API_ERROR, "Error 1");
        oshRetryService.recordFailedScan(scan2, OshFailureReason.JSON_DOWNLOAD_NETWORK_ERROR, "Error 2");
        oshRetryService.recordFailedScan(scan3, OshFailureReason.DATABASE_ERROR, "Error 3");

        entityManager.clear();

        RetryQueueStatistics stats = oshRetryService.getDetailedRetryStatistics();

        assertEquals(3, stats.totalInQueue, "Should have 3 scans in queue");
        assertEquals(3, stats.eligibleForRetry, "All scans should be eligible after backoff period");
        assertNotNull(stats.configurationSummary);

        OshUncollectedScan uncollected = oshRetryService.findRetryInfo(1001).get();
        oshRetryService.recordRetryAttempt(uncollected.getId(), OshFailureReason.OSH_API_ERROR, "Retry 1");
        entityManager.clear();

        uncollected = oshRetryService.findRetryInfo(1001).get();
        oshRetryService.recordRetryAttempt(uncollected.getId(), OshFailureReason.OSH_API_ERROR, "Retry 2");
        entityManager.clear();

        uncollected = oshRetryService.findRetryInfo(1001).get();
        oshRetryService.recordRetryAttempt(uncollected.getId(), OshFailureReason.OSH_API_ERROR, "Retry 3");
        entityManager.clear();

        RetryQueueStatistics statsAfter = oshRetryService.getDetailedRetryStatistics();

        assertEquals(3, statsAfter.totalInQueue, "Should still have 3 scans in queue");
        assertEquals(2, statsAfter.eligibleForRetry, "Only 2 scans eligible (1001 exceeded max attempts)");
        assertEquals(1, statsAfter.exceededMaxAttempts, "1 scan should have exceeded max attempts");
    }

    @Test
    @Transactional
    @DisplayName("Should get retry queue snapshot")
    void testGetRetryQueueSnapshot() {
        OshScan scan1 = createTestScan(1001, "package1");
        OshScan scan2 = createTestScan(1002, "package2");
        OshScan scan3 = createTestScan(1003, "package3");

        oshRetryService.recordFailedScan(scan1, OshFailureReason.OSH_API_ERROR, "Error 1");
        oshRetryService.recordFailedScan(scan2, OshFailureReason.JSON_DOWNLOAD_NETWORK_ERROR, "Error 2");
        oshRetryService.recordFailedScan(scan3, OshFailureReason.DATABASE_ERROR, "Error 3");

        List<OshUncollectedScan> result = oshRetryService.getRetryQueueSnapshot(10, "created");

        assertNotNull(result);
        assertEquals(3, result.size(), "Should return 3 scans");

        assertTrue(result.stream().anyMatch(s -> s.getOshScanId() == 1001));
        assertTrue(result.stream().anyMatch(s -> s.getOshScanId() == 1002));
        assertTrue(result.stream().anyMatch(s -> s.getOshScanId() == 1003));

        List<OshUncollectedScan> limitedResult = oshRetryService.getRetryQueueSnapshot(2, "created");
        assertEquals(2, limitedResult.size(), "Should respect limit parameter");
    }

    @Test
    @DisplayName("Verify graceful handling of cleanup operations on empty queue")
    void testCleanupOperations() {
        assertDoesNotThrow(() -> {
            oshRetryService.cleanupExpiredRetries();
        });

        assertDoesNotThrow(() -> {
            int cleaned = oshRetryService.cleanupExceededRetries();
            assertTrue(cleaned == 0);
        });
    }

    @Test
    @Transactional
    @DisplayName("Should cleanup scans that exceeded max retry attempts")
    void testCleanupExceededRetries() throws InterruptedException {
        oshConfiguration.setRetryBackoffDuration("PT0.001S");

        OshScan scan1 = createTestScan(1001, "package1");
        OshScan scan2 = createTestScan(1002, "package2");
        OshScan scan3 = createTestScan(1003, "package3");

        oshRetryService.recordFailedScan(scan1, OshFailureReason.OSH_API_ERROR, "Error 1");
        oshRetryService.recordFailedScan(scan2, OshFailureReason.JSON_DOWNLOAD_NETWORK_ERROR, "Error 2");
        oshRetryService.recordFailedScan(scan3, OshFailureReason.DATABASE_ERROR, "Error 3");

        entityManager.clear();

        assertEquals(3, uncollectedScanRepository.count(), "Should have 3 scans before cleanup");

        OshUncollectedScan uncollected = oshRetryService.findRetryInfo(1001).get();
        oshRetryService.recordRetryAttempt(uncollected.getId(), OshFailureReason.OSH_API_ERROR, "Retry 1");
        entityManager.clear();

        uncollected = oshRetryService.findRetryInfo(1001).get();
        oshRetryService.recordRetryAttempt(uncollected.getId(), OshFailureReason.OSH_API_ERROR, "Retry 2");
        entityManager.clear();

        uncollected = oshRetryService.findRetryInfo(1001).get();
        oshRetryService.recordRetryAttempt(uncollected.getId(), OshFailureReason.OSH_API_ERROR, "Retry 3");
        entityManager.clear();

        uncollected = oshRetryService.findRetryInfo(1001).get();
        assertEquals(3, uncollected.getAttemptCount(), "Scan 1001 should have 3 attempts");

        int cleaned = oshRetryService.cleanupExceededRetries();

        assertEquals(1, cleaned, "Should have cleaned 1 scan");
        assertEquals(2, uncollectedScanRepository.count(), "Should have 2 scans remaining");

        assertTrue(oshRetryService.findRetryInfo(1001).isEmpty(), "Scan 1001 should be removed");

        assertTrue(oshRetryService.findRetryInfo(1002).isPresent(), "Scan 1002 should still exist");
        assertTrue(oshRetryService.findRetryInfo(1003).isPresent(), "Scan 1003 should still exist");
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
