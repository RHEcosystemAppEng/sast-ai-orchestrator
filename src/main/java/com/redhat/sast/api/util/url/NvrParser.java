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

    // Pattern to match NVR: package name followed by version (starts with digit)
    private static final Pattern NVR_PATTERN = Pattern.compile("^([a-zA-Z0-9._+]+?)-([0-9][a-zA-Z0-9.-]+)$");

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
            return matcher.group(1);
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
            String versionRelease = matcher.group(2); // e.g., "257-9.el10"
            // Split on first dash to separate version from release
            int firstDash = versionRelease.indexOf('-');
            if (firstDash > 0) {
                return versionRelease.substring(0, firstDash); // e.g., "257"
            }
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
            String versionRelease = matcher.group(2); // e.g., "257-9.el10"
            // Split on first dash to separate version from release
            int firstDash = versionRelease.indexOf('-');
            if (firstDash > 0 && firstDash < versionRelease.length() - 1) {
                return versionRelease.substring(firstDash + 1); // e.g., "9.el10"
            }
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
