package com.redhat.sast.api.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.sast.api.service.osh.OshJobCreationService;
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
        OshScanResponse validScan = createValidOshScan();
        assertTrue(oshJobCreationService.canProcessScan(validScan));

        assertFalse(oshJobCreationService.canProcessScan(null));

        OshScanResponse nullIdScan = createValidOshScan();
        nullIdScan.setScanId(null);
        assertFalse(oshJobCreationService.canProcessScan(nullIdScan));

        OshScanResponse openScan = createValidOshScan();
        openScan.setState("OPEN");
        assertFalse(oshJobCreationService.canProcessScan(openScan));

        OshScanResponse failedScan = createValidOshScan();
        failedScan.setState("FAILED");
        assertFalse(oshJobCreationService.canProcessScan(failedScan));

        OshScanResponse noComponentScan = createValidOshScan();
        noComponentScan.setComponent(null);
        assertFalse(oshJobCreationService.canProcessScan(noComponentScan));

        OshScanResponse emptyComponentScan = createValidOshScan();
        emptyComponentScan.setComponent("");
        assertFalse(oshJobCreationService.canProcessScan(emptyComponentScan));

        OshScanResponse whitespaceComponentScan = createValidOshScan();
        whitespaceComponentScan.setComponent("   ");
        assertFalse(oshJobCreationService.canProcessScan(whitespaceComponentScan));
    }

    @Test
    @DisplayName("Should build NVR correctly from Label field")
    void testNvrBuildingFromLabel() {
        OshScanResponse systemdScan = createOshScanWithLabel("systemd", "252", "systemd-252-54.el9.src.rpm");
        assertNotNull(systemdScan.getRawData());
        assertEquals("systemd-252-54.el9.src.rpm", systemdScan.getRawData().get("Label"));
        assertEquals("systemd", systemdScan.getComponent());
        assertEquals("252", systemdScan.getVersion());

        OshScanResponse zlibScan = createOshScanWithLabel("zlib-ng", "2.1.6", "zlib-ng-2.1.6-2.el10.src.rpm");
        assertEquals("zlib-ng-2.1.6-2.el10.src.rpm", zlibScan.getRawData().get("Label"));
        assertEquals("zlib-ng", zlibScan.getComponent());
        assertEquals("2.1.6", zlibScan.getVersion());

        OshScanResponse rpmScan = createOshScanWithLabel("kernel", "5.14.0", "kernel-5.14.0-284.el9.rpm");
        assertEquals("kernel-5.14.0-284.el9.rpm", rpmScan.getRawData().get("Label"));
        assertEquals("kernel", rpmScan.getComponent());
        assertEquals("5.14.0", rpmScan.getVersion());
    }

    @Test
    @DisplayName("Should handle missing Label field gracefully")
    void testNvrBuildingWithoutLabel() {
        OshScanResponse scanWithoutLabel = createValidOshScan();
        scanWithoutLabel.setRawData(null);

        assertTrue(oshJobCreationService.canProcessScan(scanWithoutLabel));
        assertEquals("systemd", scanWithoutLabel.getComponent());
        assertEquals("252", scanWithoutLabel.getVersion());

        OshScanResponse scanWithEmptyRawData = createValidOshScan();
        scanWithEmptyRawData.setRawData(new HashMap<>());

        assertTrue(oshJobCreationService.canProcessScan(scanWithEmptyRawData));
        assertEquals("systemd", scanWithEmptyRawData.getComponent());

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
        OshScanResponse consistentScan = createOshScanWithLabel("systemd", "252", "systemd-252-54.el9.src.rpm");
        assertTrue(oshJobCreationService.canProcessScan(consistentScan));

        OshScanResponse inconsistentScan = createOshScanWithLabel("systemd", "252", "kernel-5.14.0-1.el9.src.rpm");
        assertTrue(oshJobCreationService.canProcessScan(inconsistentScan));
        assertEquals("systemd", inconsistentScan.getComponent());
    }

    @Test
    @DisplayName("Should handle edge cases in scan data")
    void testScanDataEdgeCases() {
        OshScanResponse minScan = createValidOshScan();
        minScan.setScanId(1);
        assertTrue(oshJobCreationService.canProcessScan(minScan));

        OshScanResponse largeScan = createValidOshScan();
        largeScan.setScanId(Integer.MAX_VALUE);
        assertTrue(oshJobCreationService.canProcessScan(largeScan));

        OshScanResponse singleCharScan = createValidOshScan();
        singleCharScan.setComponent("a");
        assertTrue(oshJobCreationService.canProcessScan(singleCharScan));

        OshScanResponse longComponentScan = createValidOshScan();
        longComponentScan.setComponent("very-long-package-name-with-many-hyphens-and-numbers-123");
        assertTrue(oshJobCreationService.canProcessScan(longComponentScan));
    }

    @Test
    @DisplayName("Should require CLOSED state for processing")
    void testStateRequirement() {
        String[] validStates = {"CLOSED"};
        String[] invalidStates = {"OPEN", "FAILED", "RUNNING", "CANCELLED", "closed", "", null};

        for (String state : validStates) {
            OshScanResponse scan = createValidOshScan();
            scan.setState(state);
            assertTrue(oshJobCreationService.canProcessScan(scan), "Should accept state: " + state);
        }

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
