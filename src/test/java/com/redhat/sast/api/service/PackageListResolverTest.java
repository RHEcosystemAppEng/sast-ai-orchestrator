package com.redhat.sast.api.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@DisplayName("PackageListResolver Tests")
class PackageListResolverTest {

    @Inject
    PackageListResolver packageListResolver;

    @Test
    @DisplayName("Should return hardcoded package list")
    void shouldReturnHardcodedPackageList() {
        String testNvr = "v1.0.0-nvr";
        List<String> packages = packageListResolver.getPackageListForNvr(testNvr);

        assertNotNull(packages, "Package list should not be null");
        assertEquals(2, packages.size(), "Should return 2 hardcoded packages");
        assertTrue(packages.contains("libconfig-1.7.3-8.el10"), "Should contain libconfig package");
        assertTrue(packages.contains("tpm2-tools-5.6-2.el10"), "Should contain tpm2-tools package");
    }

    @Test
    @DisplayName("Should return same list for any NVR")
    void shouldReturnSameListForAnyNvr() {
        List<String> packages1 = packageListResolver.getPackageListForNvr("v1.0.0");
        List<String> packages2 = packageListResolver.getPackageListForNvr("v2.0.0");

        assertEquals(packages1, packages2, "Should return same hardcoded list for different NVRs");
    }

    @Test
    @DisplayName("Should return non-empty list")
    void shouldReturnNonEmptyList() {
        List<String> packages = packageListResolver.getPackageListForNvr("any-nvr");

        assertFalse(packages.isEmpty(), "Package list should not be empty");
    }
}

