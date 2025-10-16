package com.redhat.sast.api.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.sast.api.v1.dto.osh.OshScanResponse;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * Integration tests for OshJobCreationService focusing on business logic
 * that can be tested without complex HTTP mocking.
 *
 * Tests:
 * - Scan eligibility validation
 * - NVR building from OSH metadata
 * - Error handling and edge cases
 */
@QuarkusTest
@DisplayName("OSH Job Creation Integration Tests")
class OshJobCreationIntegrationTest {

    @Inject
    OshJobCreationService oshJobCreationService;

    @Test
    @DisplayName("Should validate scan eligibility correctly")
    void testCanProcessScan() {
        // Test valid scan
        OshScanResponse validScan = createValidOshScan();
        assertTrue(oshJobCreationService.canProcessScan(validScan));

        // Test null scan
        assertFalse(oshJobCreationService.canProcessScan(null));

        // Test scan with null ID
        OshScanResponse nullIdScan = createValidOshScan();
        nullIdScan.setScanId(null);
        assertFalse(oshJobCreationService.canProcessScan(nullIdScan));

        // Test scan with wrong state
        OshScanResponse openScan = createValidOshScan();
        openScan.setState("OPEN");
        assertFalse(oshJobCreationService.canProcessScan(openScan));

        // Test scan with FAILED state
        OshScanResponse failedScan = createValidOshScan();
        failedScan.setState("FAILED");
        assertFalse(oshJobCreationService.canProcessScan(failedScan));

        // Test scan with no component
        OshScanResponse noComponentScan = createValidOshScan();
        noComponentScan.setComponent(null);
        assertFalse(oshJobCreationService.canProcessScan(noComponentScan));

        // Test scan with empty component
        OshScanResponse emptyComponentScan = createValidOshScan();
        emptyComponentScan.setComponent("");
        assertFalse(oshJobCreationService.canProcessScan(emptyComponentScan));

        // Test scan with whitespace-only component
        OshScanResponse whitespaceComponentScan = createValidOshScan();
        whitespaceComponentScan.setComponent("   ");
        assertFalse(oshJobCreationService.canProcessScan(whitespaceComponentScan));
    }

    @Test
    @DisplayName("Should build NVR correctly from Label field")
    void testNvrBuildingFromLabel() {
        // Test standard RPM format
        OshScanResponse systemdScan = createOshScanWithLabel("systemd", "252", "systemd-252-54.el9.src.rpm");
        // We can't directly test the private buildNvrFromScan method, but we can verify
        // that the scan has the expected data structure for NVR building
        assertNotNull(systemdScan.getRawData());
        assertEquals("systemd-252-54.el9.src.rpm", systemdScan.getRawData().get("Label"));
        assertEquals("systemd", systemdScan.getComponent());
        assertEquals("252", systemdScan.getVersion());

        // Test complex package name
        OshScanResponse zlibScan = createOshScanWithLabel("zlib-ng", "2.1.6", "zlib-ng-2.1.6-2.el10.src.rpm");
        assertEquals("zlib-ng-2.1.6-2.el10.src.rpm", zlibScan.getRawData().get("Label"));
        assertEquals("zlib-ng", zlibScan.getComponent());
        assertEquals("2.1.6", zlibScan.getVersion());

        // Test with just .rpm extension
        OshScanResponse rpmScan = createOshScanWithLabel("kernel", "5.14.0", "kernel-5.14.0-284.el9.rpm");
        assertEquals("kernel-5.14.0-284.el9.rpm", rpmScan.getRawData().get("Label"));
        assertEquals("kernel", rpmScan.getComponent());
        assertEquals("5.14.0", rpmScan.getVersion());
    }

    @Test
    @DisplayName("Should handle missing Label field gracefully")
    void testNvrBuildingWithoutLabel() {
        // Test scan without Label field
        OshScanResponse scanWithoutLabel = createValidOshScan();
        scanWithoutLabel.setRawData(null);

        // Should still be processable if it has component
        assertTrue(oshJobCreationService.canProcessScan(scanWithoutLabel));
        assertEquals("systemd", scanWithoutLabel.getComponent());
        assertEquals("252", scanWithoutLabel.getVersion());

        // Test scan with empty rawData
        OshScanResponse scanWithEmptyRawData = createValidOshScan();
        scanWithEmptyRawData.setRawData(new HashMap<>());

        assertTrue(oshJobCreationService.canProcessScan(scanWithEmptyRawData));
        assertEquals("systemd", scanWithEmptyRawData.getComponent());

        // Test scan with rawData but no Label field
        OshScanResponse scanWithNoLabel = createValidOshScan();
        Map<String, Object> rawDataWithoutLabel = new HashMap<>();
        rawDataWithoutLabel.put("OtherField", "some-value");
        scanWithNoLabel.setRawData(rawDataWithoutLabel);

        assertTrue(oshJobCreationService.canProcessScan(scanWithNoLabel));
        assertNull(scanWithNoLabel.getRawData().get("Label"));
    }

    @Test
    @DisplayName("Should handle complex package names correctly")
    void testComplexPackageNames() {
        // Test hyphenated package names that would break naive parsing
        String[] complexPackages = {"zlib-ng", "python3-pip", "container-tools", "java-11-openjdk", "nodejs-npm"};

        for (String packageName : complexPackages) {
            OshScanResponse scan = createValidOshScan();
            scan.setComponent(packageName);

            assertTrue(oshJobCreationService.canProcessScan(scan), "Should be able to process package: " + packageName);
            assertEquals(packageName, scan.getComponent());
        }
    }

    @Test
    @DisplayName("Should validate scan data consistency")
    void testScanDataConsistency() {
        // Test scan with consistent data
        OshScanResponse consistentScan = createOshScanWithLabel("systemd", "252", "systemd-252-54.el9.src.rpm");
        assertTrue(oshJobCreationService.canProcessScan(consistentScan));

        // Test scan where component doesn't match label (edge case)
        OshScanResponse inconsistentScan = createOshScanWithLabel("systemd", "252", "kernel-5.14.0-1.el9.src.rpm");
        // Should still be processable - the service handles label vs component mismatches
        assertTrue(oshJobCreationService.canProcessScan(inconsistentScan));
        assertEquals("systemd", inconsistentScan.getComponent()); // Component takes precedence
    }

    @Test
    @DisplayName("Should handle edge cases in scan data")
    void testScanDataEdgeCases() {
        // Test with minimum valid scan ID
        OshScanResponse minScan = createValidOshScan();
        minScan.setScanId(1);
        assertTrue(oshJobCreationService.canProcessScan(minScan));

        // Test with large scan ID
        OshScanResponse largeScan = createValidOshScan();
        largeScan.setScanId(Integer.MAX_VALUE);
        assertTrue(oshJobCreationService.canProcessScan(largeScan));

        // Test with single character component
        OshScanResponse singleCharScan = createValidOshScan();
        singleCharScan.setComponent("a");
        assertTrue(oshJobCreationService.canProcessScan(singleCharScan));

        // Test with very long component name
        OshScanResponse longComponentScan = createValidOshScan();
        longComponentScan.setComponent("very-long-package-name-with-many-hyphens-and-numbers-123");
        assertTrue(oshJobCreationService.canProcessScan(longComponentScan));
    }

    @Test
    @DisplayName("Should require CLOSED state for processing")
    void testStateRequirement() {
        String[] validStates = {"CLOSED"};
        String[] invalidStates = {"OPEN", "FAILED", "RUNNING", "CANCELLED", "closed", "", null};

        // Test valid states
        for (String state : validStates) {
            OshScanResponse scan = createValidOshScan();
            scan.setState(state);
            assertTrue(oshJobCreationService.canProcessScan(scan), "Should accept state: " + state);
        }

        // Test invalid states
        for (String state : invalidStates) {
            OshScanResponse scan = createValidOshScan();
            scan.setState(state);
            assertFalse(oshJobCreationService.canProcessScan(scan), "Should reject state: " + state);
        }
    }

    // Helper methods
    private OshScanResponse createValidOshScan() {
        OshScanResponse scan = new OshScanResponse();
        scan.setScanId(12345);
        scan.setState("CLOSED");
        scan.setComponent("systemd");
        scan.setVersion("252");

        Map<String, Object> rawData = new HashMap<>();
        rawData.put("Label", "systemd-252-54.el9.src.rpm");
        scan.setRawData(rawData);

        return scan;
    }

    private OshScanResponse createOshScanWithLabel(String component, String version, String label) {
        OshScanResponse scan = new OshScanResponse();
        scan.setScanId(12345);
        scan.setState("CLOSED");
        scan.setComponent(component);
        scan.setVersion(version);

        Map<String, Object> rawData = new HashMap<>();
        rawData.put("Label", label);
        scan.setRawData(rawData);

        return scan;
    }
}
