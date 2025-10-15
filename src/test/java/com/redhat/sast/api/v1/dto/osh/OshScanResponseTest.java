package com.redhat.sast.api.v1.dto.osh;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for OshScanResponse DTO.
 *
 * Tests cover:
 * - Constructor behavior
 * - Business logic methods (isCompleted, getPackageName)
 * - Field access and modification
 * - Edge cases and null handling
 */
@DisplayName("OSH Scan Response DTO Tests")
class OshScanResponseTest {

    @Test
    @DisplayName("Should create response with default constructor")
    void testDefaultConstructor() {
        // When
        OshScanResponse response = new OshScanResponse();

        // Then
        assertNull(response.getScanId());
        assertNull(response.getComponent());
        assertNull(response.getVersion());
        assertNull(response.getState());
        assertNull(response.getOwner());
        assertNull(response.getScanType());
        assertNull(response.getCreated());
        assertNull(response.getStarted());
        assertNull(response.getFinished());
        assertNull(response.getArch());
        assertNull(response.getChannel());
        assertNull(response.getRawData());
    }

    @Test
    @DisplayName("Should create response with parameterized constructor")
    void testParameterizedConstructor() {
        // When
        OshScanResponse response = new OshScanResponse(12345, "CLOSED");

        // Then
        assertEquals(12345, response.getScanId());
        assertEquals("CLOSED", response.getState());
        assertNull(response.getComponent()); // Other fields remain null
    }

    @Test
    @DisplayName("Should identify completed scans correctly")
    void testIsCompleted() {
        // Test CLOSED state (completed)
        OshScanResponse closedScan = new OshScanResponse(1001, "CLOSED");
        assertTrue(closedScan.isCompleted());

        // Test OPEN state (not completed)
        OshScanResponse openScan = new OshScanResponse(1002, "OPEN");
        assertFalse(openScan.isCompleted());

        // Test FAILED state (not completed)
        OshScanResponse failedScan = new OshScanResponse(1003, "FAILED");
        assertFalse(failedScan.isCompleted());

        // Test null state (not completed)
        OshScanResponse nullStateScan = new OshScanResponse();
        assertFalse(nullStateScan.isCompleted());

        // Test empty state (not completed)
        OshScanResponse emptyScan = new OshScanResponse(1004, "");
        assertFalse(emptyScan.isCompleted());

        // Test case sensitivity
        OshScanResponse lowercaseScan = new OshScanResponse(1005, "closed");
        assertFalse(lowercaseScan.isCompleted()); // Should be case-sensitive
    }

    @Test
    @DisplayName("Should return package name correctly")
    void testGetPackageName() {
        // Given
        OshScanResponse response = new OshScanResponse();
        response.setComponent("systemd");

        // When/Then
        assertEquals("systemd", response.getPackageName());
    }

    @Test
    @DisplayName("Should return null package name when component is null")
    void testGetPackageName_NullComponent() {
        // Given
        OshScanResponse response = new OshScanResponse();
        response.setComponent(null);

        // When/Then
        assertNull(response.getPackageName());
    }

    @Test
    @DisplayName("Should handle all field assignments correctly")
    void testFieldAssignments() {
        // Given
        OshScanResponse response = new OshScanResponse();
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("test_key", "test_value");

        // When
        response.setScanId(54321);
        response.setComponent("kernel");
        response.setVersion("5.14.0-284.el9");
        response.setState("OPEN");
        response.setOwner("test-owner");
        response.setScanType("differential");
        response.setCreated("2024-01-15T10:30:00Z");
        response.setStarted("2024-01-15T10:35:00Z");
        response.setFinished("2024-01-15T11:45:00Z");
        response.setArch("x86_64");
        response.setChannel("main");
        response.setRawData(rawData);

        // Then
        assertEquals(54321, response.getScanId());
        assertEquals("kernel", response.getComponent());
        assertEquals("5.14.0-284.el9", response.getVersion());
        assertEquals("OPEN", response.getState());
        assertEquals("test-owner", response.getOwner());
        assertEquals("differential", response.getScanType());
        assertEquals("2024-01-15T10:30:00Z", response.getCreated());
        assertEquals("2024-01-15T10:35:00Z", response.getStarted());
        assertEquals("2024-01-15T11:45:00Z", response.getFinished());
        assertEquals("x86_64", response.getArch());
        assertEquals("main", response.getChannel());
        assertEquals(rawData, response.getRawData());
        assertEquals("test_value", response.getRawData().get("test_key"));
    }

    @Test
    @DisplayName("Should handle empty strings appropriately")
    void testEmptyStringHandling() {
        // Given
        OshScanResponse response = new OshScanResponse();

        // When
        response.setComponent("");
        response.setVersion("");
        response.setState("");
        response.setOwner("");

        // Then
        assertEquals("", response.getComponent());
        assertEquals("", response.getVersion());
        assertEquals("", response.getState());
        assertEquals("", response.getOwner());
        assertEquals("", response.getPackageName());
        assertFalse(response.isCompleted()); // Empty state is not CLOSED
    }

    @Test
    @DisplayName("Should handle whitespace strings")
    void testWhitespaceHandling() {
        // Given
        OshScanResponse response = new OshScanResponse();

        // When
        response.setComponent("  systemd  ");
        response.setState("  CLOSED  ");

        // Then
        assertEquals("  systemd  ", response.getComponent());
        assertEquals("  systemd  ", response.getPackageName());
        assertEquals("  CLOSED  ", response.getState());
        assertFalse(response.isCompleted()); // Whitespace around CLOSED should not match
    }

    @Test
    @DisplayName("Should support null scan ID")
    void testNullScanId() {
        // Given
        OshScanResponse response = new OshScanResponse();
        response.setScanId(null);

        // When/Then
        assertNull(response.getScanId());
    }

    @Test
    @DisplayName("Should support negative scan ID")
    void testNegativeScanId() {
        // Given
        OshScanResponse response = new OshScanResponse();
        response.setScanId(-1);

        // When/Then
        assertEquals(-1, response.getScanId());
    }

    @Test
    @DisplayName("Should support zero scan ID")
    void testZeroScanId() {
        // Given
        OshScanResponse response = new OshScanResponse();
        response.setScanId(0);

        // When/Then
        assertEquals(0, response.getScanId());
    }

    @Test
    @DisplayName("Should handle raw data manipulation")
    void testRawDataManipulation() {
        // Given
        OshScanResponse response = new OshScanResponse();
        Map<String, Object> rawData = new HashMap<>();

        // When
        response.setRawData(rawData);
        rawData.put("state", "CLOSED");
        rawData.put("scan_id", 12345);
        rawData.put("nested_data", Map.of("key", "value"));

        // Then
        assertEquals(rawData, response.getRawData());
        assertEquals("CLOSED", response.getRawData().get("state"));
        assertEquals(12345, response.getRawData().get("scan_id"));
        assertTrue(response.getRawData().get("nested_data") instanceof Map);
    }

    @Test
    @DisplayName("Should handle null raw data")
    void testNullRawData() {
        // Given
        OshScanResponse response = new OshScanResponse();
        response.setRawData(null);

        // When/Then
        assertNull(response.getRawData());
    }

    @Test
    @DisplayName("Should create response using builder pattern simulation")
    void testBuilderPatternSimulation() {
        // Given/When - Simulating a builder pattern with method chaining
        OshScanResponse response = new OshScanResponse();
        response.setScanId(99999);
        response.setComponent("test-component");
        response.setState("CLOSED");

        // Then
        assertEquals(99999, response.getScanId());
        assertEquals("test-component", response.getComponent());
        assertEquals("CLOSED", response.getState());
        assertTrue(response.isCompleted());
        assertEquals("test-component", response.getPackageName());
    }
}
