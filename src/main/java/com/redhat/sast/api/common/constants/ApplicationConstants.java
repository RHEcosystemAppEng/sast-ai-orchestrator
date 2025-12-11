package com.redhat.sast.api.common.constants;

import java.util.function.Predicate;

/**
 * Application-wide constants and utility predicates.
 */
public final class ApplicationConstants {

    private ApplicationConstants() {
        // Utility class - prevent instantiation
    }

    public static final String DEFAULT_SECRET_NAME = "sast-ai-default-llm-creds";

    /**
     * Predicate to check if a string is not null and not blank.
     * Handles null, empty, and whitespace-only strings.
     */
    public static final Predicate<String> IS_NOT_NULL_AND_NOT_BLANK = value -> value != null && !value.isBlank();
}
