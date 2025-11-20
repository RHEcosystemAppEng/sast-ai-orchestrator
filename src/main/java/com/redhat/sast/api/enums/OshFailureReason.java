package com.redhat.sast.api.enums;

public enum OshFailureReason {
    OSH_METADATA_NETWORK_ERROR("Network error fetching OSH scan metadata"),
    OSH_METADATA_PARSE_ERROR("Error parsing OSH API response"),
    OSH_API_ERROR("OSH API error fetching scan details"),
    SCAN_DATA_ERROR("Failed to parse or validate OSH scan data"),
    DATABASE_ERROR("Database or persistence error"),
    JOB_CREATION_ERROR("Failed to create job entity"),
    UNKNOWN_ERROR("Unclassified error during scan processing");

    private final String description;

    OshFailureReason(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", this.name(), description);
    }
}
