package com.redhat.sast.api.v1.resource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.sast.api.config.OshConfiguration;
import com.redhat.sast.api.v1.dto.response.MonitoredPackagesResponseDto;

import jakarta.ws.rs.core.Response;

/**
 * Unit tests for ConfigurationResource.
 *
 * Tests cover:
 * - Successful retrieval of monitored packages
 * - Response structure and content
 * - OSH enabled/disabled states
 * - Empty package list handling
 */
@DisplayName("Configuration Resource Tests")
class ConfigurationResourceTest {

    private ConfigurationResource resource;
    private TestOshConfiguration oshConfiguration;

    @BeforeEach
    void setUp() {
        oshConfiguration = new TestOshConfiguration();
        resource = new ConfigurationResource(oshConfiguration);
    }

    @Test
    @DisplayName("Should return monitored packages when OSH is enabled")
    void testGetMonitoredPackages_OshEnabled() {
        // Given
        Set<String> packages = Set.of("systemd", "kernel", "glibc");
        oshConfiguration.setTestPackages(packages);
        oshConfiguration.setTestEnabled(true);
        oshConfiguration.setTestPackagesFilePath("/deployments/config/packages.txt");

        // When
        Response response = resource.getMonitoredPackages();

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof MonitoredPackagesResponseDto);

        MonitoredPackagesResponseDto dto = (MonitoredPackagesResponseDto) response.getEntity();
        assertEquals(packages, dto.getPackages());
        assertTrue(dto.isOshEnabled());
        assertEquals(3, dto.getTotalPackages());
        assertEquals("/deployments/config/packages.txt", dto.getPackagesFilePath());
    }

    @Test
    @DisplayName("Should return empty package list when OSH is disabled")
    void testGetMonitoredPackages_OshDisabled() {
        Set<String> emptyPackages = Set.of();
        oshConfiguration.setTestPackages(emptyPackages);
        oshConfiguration.setTestEnabled(false);
        oshConfiguration.setTestPackagesFilePath("/deployments/config/packages.txt");

        Response response = resource.getMonitoredPackages();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        MonitoredPackagesResponseDto dto = (MonitoredPackagesResponseDto) response.getEntity();
        assertTrue(dto.getPackages().isEmpty());
        assertFalse(dto.isOshEnabled());
        assertEquals(0, dto.getTotalPackages());
    }

    @Test
    @DisplayName("Should return large package list correctly")
    void testGetMonitoredPackages_LargeList() {
        Set<String> largePackageSet = Set.of(
                "systemd",
                "kernel",
                "glibc",
                "httpd",
                "nginx",
                "postgresql",
                "mysql",
                "redis",
                "mongodb",
                "elasticsearch",
                "kafka",
                "rabbitmq",
                "python",
                "java",
                "nodejs",
                "ruby",
                "php",
                "perl",
                "golang",
                "rust",
                "gcc",
                "make",
                "cmake",
                "automake",
                "autoconf",
                "libtool",
                "pkg-config",
                "binutils",
                "gdb",
                "valgrind",
                "openssl",
                "openssh",
                "curl",
                "wget",
                "rsync",
                "tar",
                "gzip",
                "bzip2",
                "xz",
                "zip",
                "git",
                "svn",
                "mercurial",
                "vim",
                "emacs",
                "nano",
                "bash",
                "zsh",
                "fish",
                "tcsh");

        oshConfiguration.setTestPackages(largePackageSet);
        oshConfiguration.setTestEnabled(true);
        oshConfiguration.setTestPackagesFilePath("/deployments/config/packages.txt");

        Response response = resource.getMonitoredPackages();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        MonitoredPackagesResponseDto dto = (MonitoredPackagesResponseDto) response.getEntity();
        assertEquals(50, dto.getTotalPackages());
        assertEquals(largePackageSet, dto.getPackages());
        assertTrue(dto.isOshEnabled());
    }

    @Test
    @DisplayName("Should return valid response structure with all fields")
    void testGetMonitoredPackages_ResponseStructure() {
        Set<String> packages = Set.of("adcli", "audit", "cpio");
        oshConfiguration.setTestPackages(packages);
        oshConfiguration.setTestEnabled(true);
        oshConfiguration.setTestPackagesFilePath("/custom/path/packages.txt");

        Response response = resource.getMonitoredPackages();
        MonitoredPackagesResponseDto dto = (MonitoredPackagesResponseDto) response.getEntity();

        assertNotNull(dto.getPackages(), "Packages field should not be null");
        assertNotNull(dto.getPackagesFilePath(), "PackagesFilePath field should not be null");
        assertEquals(packages.size(), dto.getTotalPackages(), "TotalPackages should match actual count");
        assertEquals("/custom/path/packages.txt", dto.getPackagesFilePath());
    }

    /**
     * Test implementation of OshConfiguration for testing purposes.
     * Allows setting package list without file I/O.
     */
    private static class TestOshConfiguration extends OshConfiguration {
        private Set<String> testPackages = Set.of();
        private boolean testEnabled = false;
        private String testPackagesFilePath = "";

        void setTestPackages(Set<String> packages) {
            this.testPackages = packages != null ? Set.copyOf(packages) : Set.of();
        }

        void setTestEnabled(boolean enabled) {
            this.testEnabled = enabled;
        }

        void setTestPackagesFilePath(String path) {
            this.testPackagesFilePath = path;
        }

        @Override
        public Set<String> getPackageNameSet() {
            return testPackages;
        }

        @Override
        public boolean isEnabled() {
            return testEnabled;
        }

        @Override
        public String getPackagesFilePath() {
            return testPackagesFilePath;
        }
    }
}
