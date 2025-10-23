package com.redhat.sast.api.enums;

/**
 * Classification of OSH scan processing failures.
 */
public enum OshFailureReason {

    /**
     * Network error downloading SAST report JSON from OSH logs.
     */
    JSON_DOWNLOAD_NETWORK_ERROR("Network error downloading SAST report JSON"),

    /**
     * HTTP error downloading SAST report JSON (timeouts, 4xx, 5xx errors).
     */
    JSON_DOWNLOAD_HTTP_ERROR("HTTP error downloading SAST report JSON"),

    /**
     * OSH API error when fetching scan details.
     */
    OSH_API_ERROR("OSH API error fetching scan details"),

    /**
     * Failed to parse or validate OSH scan data.
     */
    SCAN_DATA_ERROR("Failed to parse or validate OSH scan data"),

    /**
     * Database or persistence error.
     */
    DATABASE_ERROR("Database or persistence error"),

    /**
     * Failed to create job entity in JobService.
     */
    JOB_CREATION_ERROR("Failed to create job entity"),

    /**
     * Unclassified error during processing.
     */
    UNKNOWN_ERROR("Unclassified error during scan processing");

    private final String description;

    /**
     * Creates a failure reason with description.
     *
     * @param description human-readable description of the failure type
     */
    OshFailureReason(String description) {
        this.description = description;
    }

    /**
     * Gets human-readable description of this failure reason.
     *
     * @return description suitable for logging and debugging
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets failure reason name for use in logs and configuration.
     *
     * @return enum constant name
     */
    public String getReasonCode() {
        return this.name();
    }

    @Override
    public String toString() {
        return String.format("%s: %s", this.name(), description);
    }
}
