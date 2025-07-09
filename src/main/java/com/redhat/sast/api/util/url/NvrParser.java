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
    private static final Pattern NVR_PATTERN = Pattern.compile("^([a-zA-Z0-9._+-]+?)-([0-9].*)$");

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
     * Validates if a string follows the NVR format.
     *
     * @param nvr the string to validate
     * @return true if it's a valid NVR format, false otherwise
     */
    public boolean isValidNvr(String nvr) {
        return extractPackageName(nvr) != null;
    }
}
