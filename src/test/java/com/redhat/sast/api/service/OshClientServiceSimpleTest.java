package com.redhat.sast.api.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.sast.api.v1.dto.osh.OshScanResponse;

/**
 * Simple unit tests for OshClientService business logic.
 *
 * These tests focus on parts that can be tested without complex HTTP mocking:
 * - Component/version parsing logic
 * - Response filtering logic
 * - Edge cases and validation
 */
@DisplayName("OSH Client Service Simple Tests")
class OshClientServiceSimpleTest {

    @Test
    @DisplayName("Should identify completed scans correctly")
    void testCompletedScanIdentification() {
        assertTrue(createScanResponse("CLOSED").isCompleted());
        assertFalse(createScanResponse("OPEN").isCompleted());
        assertFalse(createScanResponse("FAILED").isCompleted());
        assertFalse(createScanResponse(null).isCompleted());
        assertFalse(createScanResponse("").isCompleted());
        assertFalse(createScanResponse("closed").isCompleted()); // Case sensitive
    }

    @Test
    @DisplayName("Should parse component names correctly")
    void testComponentNameParsing() {
        assertEquals("systemd", createScanWithComponent("systemd").getPackageName());
        assertEquals("kernel-headers", createScanWithComponent("kernel-headers").getPackageName());
        assertNull(createScanWithComponent(null).getPackageName());
        assertEquals("", createScanWithComponent("").getPackageName());
    }

    @Test
    @DisplayName("Should validate scan response construction")
    void testScanResponseConstruction() {
        OshScanResponse response = new OshScanResponse(12345, "CLOSED");
        assertEquals(12345, response.getScanId());
        assertEquals("CLOSED", response.getState());
        assertTrue(response.isCompleted());
    }

    @Test
    @DisplayName("Should handle edge cases gracefully")
    void testEdgeCases() {
        OshScanResponse extremeResponse = new OshScanResponse();
        extremeResponse.setScanId(Integer.MAX_VALUE);
        extremeResponse.setComponent("very-long-component-name-that-might-cause-issues");
        extremeResponse.setState("CLOSED");

        assertEquals(Integer.MAX_VALUE, extremeResponse.getScanId());
        assertTrue(extremeResponse.isCompleted());
        assertNotNull(extremeResponse.getPackageName());

        OshScanResponse minResponse = new OshScanResponse();
        minResponse.setScanId(0);
        minResponse.setComponent("a");
        minResponse.setState("CLOSED");

        assertEquals(0, minResponse.getScanId());
        assertEquals("a", minResponse.getComponent());
        assertTrue(minResponse.isCompleted());
    }

    @Test
    @DisplayName("Should handle complex package names correctly")
    void testComplexPackageNames() {
        assertEquals("zlib-ng", createScanWithComponent("zlib-ng").getPackageName());
        assertEquals("python3-pip", createScanWithComponent("python3-pip").getPackageName());
        assertEquals(
                "container-tools", createScanWithComponent("container-tools").getPackageName());
        assertEquals("libxml2", createScanWithComponent("libxml2").getPackageName());
        assertEquals("single", createScanWithComponent("single").getPackageName());
    }

    private OshScanResponse createScanResponse(String state) {
        return new OshScanResponse(1000, state);
    }

    private OshScanResponse createScanWithComponent(String component) {
        OshScanResponse response = new OshScanResponse();
        response.setComponent(component);
        return response;
    }
}
