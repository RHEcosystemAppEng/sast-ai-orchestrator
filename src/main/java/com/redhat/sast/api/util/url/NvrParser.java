package com.redhat.sast.api.util.url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Utility class for parsing NVR (Name-Version-Release) strings.
 *
 * NVR format: name-version-release
 * Example:
 * - systemd-257-9.el10 → name: systemd
 */
@ApplicationScoped
public class NvrParser {

    // Pattern to match NVR: package name, version (starts with digit), and release (contains .el)
    private static final Pattern NVR_PATTERN =
            Pattern.compile("^(.++)-([0-9][a-zA-Z0-9.]*+)-([a-zA-Z0-9.]*+\\.el\\d++.*)$");

    /**
     * Extracts the package name from an NVR string.
     *
     * @param nvr the NVR string (e.g., "systemd-257-9.el10")
     * @return the package name (e.g., "systemd") or null if parsing fails
     */
    public String extractPackageName(String nvr) {
        if (nvr == null || nvr.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = NVR_PATTERN.matcher(nvr.trim());
        if (matcher.matches()) {
            return matcher.group(1); // Package name
        }

        return null;
    }

    /**
     * Extracts the version from an NVR string.
     *
     * @param nvr the NVR string (e.g., "systemd-257-9.el10")
     * @return the version (e.g., "257") or null if parsing fails
     */
    public String extractVersion(String nvr) {
        if (nvr == null || nvr.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = NVR_PATTERN.matcher(nvr.trim());
        if (matcher.matches()) {
            return matcher.group(2); // Version
        }
        return null;
    }

    /**
     * Extracts the release from an NVR string.
     *
     * @param nvr the NVR string (e.g., "systemd-257-9.el10")
     * @return the release (e.g., "9.el10") or null if parsing fails
     */
    public String extractRelease(String nvr) {
        if (nvr == null || nvr.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = NVR_PATTERN.matcher(nvr.trim());
        if (matcher.matches()) {
            return matcher.group(3); // Release
        }
        return null;
    }

    /**
     * Extracts the RHEL version from an NVR release string.
     *
     * @param nvr the NVR string (e.g., "systemd-257-9.el10")
     * @return the RHEL version (e.g., "10") or null if parsing fails
     */
    public String extractRhelVersion(String nvr) {
        String release = extractRelease(nvr);
        if (release == null) {
            return null;
        }

        // Look for pattern .elX where X is the version number
        Pattern elPattern = Pattern.compile("\\.el(\\d+)(?:\\.|$)");
        Matcher matcher = elPattern.matcher(release);
        if (matcher.find()) {
            return matcher.group(1); // Return just the number part
        }

        return null;
    }

    /**
     * Validates if a string follows the NVR format.
     *
     * @param nvr the string to validate
     * @return true if it's a valid NVR format, false otherwise
     */
    public boolean isValidNvr(String nvr) {
        return extractPackageName(nvr) != null;
    }
}
