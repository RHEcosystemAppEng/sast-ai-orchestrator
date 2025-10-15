package com.redhat.sast.api.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.sast.api.util.url.NvrParser;
import com.redhat.sast.api.v1.dto.osh.OshScanResponse;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/**
 * Integration test to verify that OSH label parsing works correctly
 * with the NvrParser for complex package names.
 *
 * This test verifies the fix for the issue where package names with dashes
 * (like zlib-ng-2.1.6-2.el10) were incorrectly parsed.
 */
@QuarkusTest
@DisplayName("OSH NVR Parsing Integration Tests")
class OshNvrParsingIntegrationTest {

    @Inject
    NvrParser nvrParser;

    @Test
    @DisplayName("Should parse complex NVR strings correctly")
    void testComplexNvrParsing() {
        // Test cases based on real OSH scan labels
        testNvrParsing("zlib-ng-2.1.6-2.el10", "zlib-ng", "2.1.6");
        testNvrParsing("systemd-257-9.el10", "systemd", "257");
        testNvrParsing("kernel-5.14.0-284.el9", "kernel", "5.14.0");
        testNvrParsing("python3-pip-21.2.3-7.el9", "python3-pip", "21.2.3");
        testNvrParsing("libxml2-2.9.13-6.el9_4", "libxml2", "2.9.13");
    }

    @Test
    @DisplayName("Should handle edge cases in NVR parsing")
    void testNvrParsingEdgeCases() {
        // Single word (no version/release)
        assertNull(nvrParser.extractPackageName("singleword"));

        // Invalid format
        assertNull(nvrParser.extractPackageName("invalid-format-no-dot-el"));

        // Empty/null
        assertNull(nvrParser.extractPackageName(""));
        assertNull(nvrParser.extractPackageName(null));

        // Very short valid NVR
        assertEquals("a", nvrParser.extractPackageName("a-1-1.el9"));
    }

    @Test
    @DisplayName("Should demonstrate the fix for dash-containing package names")
    void testDashContainingPackageNamesFix() {
        // This test demonstrates why the old naive parsing failed
        String complexLabel = "zlib-ng-2.1.6-2.el10";

        // Old naive approach (splitting on first dash) would give wrong result:
        String naiveComponent = complexLabel.split("-")[0]; // Would be "zlib" (WRONG!)
        assertEquals("zlib", naiveComponent); // This is the BUG we fixed

        // Correct NVR parsing gives right result:
        String correctComponent = nvrParser.extractPackageName(complexLabel);
        assertEquals("zlib-ng", correctComponent); // This is CORRECT!

        // Verify other complex cases
        assertEquals("python3-pip", nvrParser.extractPackageName("python3-pip-21.2.3-7.el9"));
    }

    @Test
    @DisplayName("Should handle version extraction correctly")
    void testVersionExtraction() {
        assertEquals("2.1.6", nvrParser.extractVersion("zlib-ng-2.1.6-2.el10"));
        assertEquals("257", nvrParser.extractVersion("systemd-257-9.el10"));
        assertEquals("5.14.0", nvrParser.extractVersion("kernel-5.14.0-284.el9"));
        assertEquals("21.2.3", nvrParser.extractVersion("python3-pip-21.2.3-7.el9"));

        // Edge cases
        assertNull(nvrParser.extractVersion("invalid-format"));
        assertNull(nvrParser.extractVersion(""));
        assertNull(nvrParser.extractVersion(null));
    }

    @Test
    @DisplayName("Should simulate OshClientService parsing behavior")
    void testOshClientServiceParsingSimulation() {
        // Simulate what happens in OshClientService.parseComponentFromLabel()
        String oshLabel = "zlib-ng-2.1.6-2.el10";

        OshScanResponse response = new OshScanResponse();

        // Simulate the logic from parseComponentFromLabel method
        String component = nvrParser.extractPackageName(oshLabel);
        String version = nvrParser.extractVersion(oshLabel);

        if (component != null) {
            response.setComponent(component);
            response.setVersion(version);
        } else {
            response.setComponent(oshLabel); // Fallback
        }

        // Verify correct parsing
        assertEquals("zlib-ng", response.getComponent());
        assertEquals("2.1.6", response.getVersion());
        assertEquals("zlib-ng", response.getPackageName()); // This uses getComponent()
    }

    // Helper method for testing NVR parsing
    private void testNvrParsing(String nvr, String expectedComponent, String expectedVersion) {
        assertEquals(expectedComponent, nvrParser.extractPackageName(nvr), "Component parsing failed for: " + nvr);
        assertEquals(expectedVersion, nvrParser.extractVersion(nvr), "Version parsing failed for: " + nvr);
        assertTrue(nvrParser.isValidNvr(nvr), "NVR validation failed for: " + nvr);
    }
}
