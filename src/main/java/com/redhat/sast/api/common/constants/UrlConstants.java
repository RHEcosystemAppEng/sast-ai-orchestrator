package com.redhat.sast.api.common.constants;

/**
 * Constants for URL templates and patterns used in URL inference.
 */
public final class UrlConstants {

    private UrlConstants() {
        // Utility class - prevent instantiation
    }

    // Known false positives URL components
    public static final String KNOWN_FALSE_POSITIVES_BASE_URL =
            "https://gitlab.cee.redhat.com/osh/known-false-positives/-/raw/master/";
    public static final String KNOWN_FALSE_POSITIVES_FILE = "ignore.err";

    // Source code URL components
    public static final String SOURCE_CODE_BASE_URL_TEMPLATE =
            "https://download.devel.redhat.com/brewroot/vol/rhel-%s/packages/";
    public static final String SOURCE_CODE_SUFFIX = ".src.rpm";
}
