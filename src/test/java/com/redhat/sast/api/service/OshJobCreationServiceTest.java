package com.redhat.sast.api.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.sast.api.service.osh.OshJobCreationService;
import com.redhat.sast.api.v1.dto.osh.OshScanDto;

@DisplayName("OSH Job Creation Service Tests")
class OshJobCreationServiceTest {

    private OshJobCreationService oshJobCreationService;

    @BeforeEach
    void setUp() {
        oshJobCreationService = new OshJobCreationService(null, null, null, null, null);
    }

    @Test
    @DisplayName("Should validate scan eligibility correctly")
    void canProcessScan_handleValidations() {
        OshScanDto validScan = createValidOshScan();
        assertTrue(oshJobCreationService.canProcessScan(validScan));

        assertFalse(oshJobCreationService.canProcessScan(null));

        OshScanDto nullIdScan = createValidOshScan();
        nullIdScan.setScanId(null);
        assertFalse(oshJobCreationService.canProcessScan(nullIdScan));

        OshScanDto openScan = createValidOshScan();
        openScan.setState("OPEN");
        assertFalse(oshJobCreationService.canProcessScan(openScan));

        OshScanDto failedScan = createValidOshScan();
        failedScan.setState("FAILED");
        assertFalse(oshJobCreationService.canProcessScan(failedScan));

        OshScanDto noComponentScan = createValidOshScan();
        noComponentScan.setComponent(null);
        assertFalse(oshJobCreationService.canProcessScan(noComponentScan));

        OshScanDto emptyComponentScan = createValidOshScan();
        emptyComponentScan.setComponent("");
        assertFalse(oshJobCreationService.canProcessScan(emptyComponentScan));

        OshScanDto whitespaceComponentScan = createValidOshScan();
        whitespaceComponentScan.setComponent("   ");
        assertFalse(oshJobCreationService.canProcessScan(whitespaceComponentScan));
    }

    @Test
    @DisplayName("Should extract package NVR from Label field")
    void extractPackageNvr_fromLabelField() throws Exception {
        var method = OshJobCreationService.class.getDeclaredMethod("extractPackageNvr", OshScanDto.class);
        method.setAccessible(true);

        OshScanDto systemdScan = createValidOshScan("systemd", "252", "systemd-252-54.el9.src.rpm");
        String nvr = (String) method.invoke(oshJobCreationService, systemdScan);
        assertEquals("systemd-252-54.el9", nvr, "Should extract NVR from Label");

        OshScanDto zlibScan = createValidOshScan("zlib-ng", "2.1.6", "zlib-ng-2.1.6-2.el10.src.rpm");
        nvr = (String) method.invoke(oshJobCreationService, zlibScan);
        assertEquals("zlib-ng-2.1.6-2.el10", nvr, "Should handle multi-part package names");

        OshScanDto rpmScan = createValidOshScan("kernel", "5.14.0", "kernel-5.14.0-284.el9.rpm");
        nvr = (String) method.invoke(oshJobCreationService, rpmScan);
        assertEquals("kernel-5.14.0-284.el9", nvr, "Should handle .rpm extension");
    }

    @Test
    @DisplayName("Should handle missing Label field gracefully")
    void canProcessScan_handleMissingLabelField() {
        OshScanDto scanWithoutLabel = createValidOshScan();
        scanWithoutLabel.setRawData(null);

        assertTrue(oshJobCreationService.canProcessScan(scanWithoutLabel));
        assertEquals("systemd", scanWithoutLabel.getComponent());
        assertEquals("252", scanWithoutLabel.getVersion());

        OshScanDto scanWithEmptyRawData = createValidOshScan();
        scanWithEmptyRawData.setRawData(new HashMap<>());

        assertTrue(oshJobCreationService.canProcessScan(scanWithEmptyRawData));
        assertEquals("systemd", scanWithEmptyRawData.getComponent());

        OshScanDto scanWithNoLabel = createValidOshScan();
        Map<String, Object> rawDataWithoutLabel = new HashMap<>();
        rawDataWithoutLabel.put("OtherField", "some-value");
        scanWithNoLabel.setRawData(rawDataWithoutLabel);

        assertTrue(oshJobCreationService.canProcessScan(scanWithNoLabel));
        assertNull(scanWithNoLabel.getRawData().get("Label"));
    }

    @Test
    @DisplayName("Should extract NVR correctly for packages with hyphens and numbers")
    void extractPackageNvr_handleComplexPackageNames() throws Exception {
        var method = OshJobCreationService.class.getDeclaredMethod("extractPackageNvr", OshScanDto.class);
        method.setAccessible(true);

        OshScanDto zlibScan = createValidOshScan("zlib-ng", "2.1.6", "zlib-ng-2.1.6-2.el10.src.rpm");
        String nvr = (String) method.invoke(oshJobCreationService, zlibScan);
        assertEquals("zlib-ng-2.1.6-2.el10", nvr, "Should handle package with hyphen");

        OshScanDto pythonScan = createValidOshScan("python3-pip", "21.2.3", "python3-pip-21.2.3-1.el9.src.rpm");
        nvr = (String) method.invoke(oshJobCreationService, pythonScan);
        assertEquals("python3-pip-21.2.3-1.el9", nvr, "Should handle package with numbers");

        OshScanDto javaScan =
                createValidOshScan("java-11-openjdk", "11.0.19.0.7", "java-11-openjdk-11.0.19.0.7-1.el9.src.rpm");
        nvr = (String) method.invoke(oshJobCreationService, javaScan);
        assertEquals("java-11-openjdk-11.0.19.0.7-1.el9", nvr, "Should handle multi-hyphen package names");
    }

    @Test
    @DisplayName("Should require CLOSED state for processing")
    void canProcessScan_requireClosedState() {
        String validState = "CLOSED";
        String[] invalidStates = {"OPEN", "FAILED", "RUNNING", "CANCELLED", "closed", "", null};

        OshScanDto scan = createValidOshScan();
        scan.setState(validState);
        assertTrue(oshJobCreationService.canProcessScan(scan), "Should accept state: " + validState);

        for (String state : invalidStates) {
            OshScanDto s = createValidOshScan();
            s.setState(state);
            assertFalse(oshJobCreationService.canProcessScan(s), "Should reject state: " + state);
        }
    }

    // Helper methods
    private OshScanDto createValidOshScan() {
        return createValidOshScan("systemd", "252", "systemd-252-54.el9.src.rpm");
    }

    private OshScanDto createValidOshScan(String component, String version, String label) {
        OshScanDto scan = new OshScanDto();
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
