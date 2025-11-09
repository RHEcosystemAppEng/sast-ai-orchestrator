package com.redhat.sast.api.v1.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing an OSH scan with its associated job and retry information.
 *
 * This aggregates data from:
 * - Job table (for collected scans with oshScanId)
 * - OshUncollectedScan table (for uncollected scans with retry info)
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OshScanStatusDto {

    @JsonProperty("oshScanId")
    private String oshScanId;

    @JsonProperty("packageName")
    private String packageName;

    @JsonProperty("packageNvr")
    private String packageNvr;

    /**
     * Status of the OSH scan:
     * - "COLLECTED": Successfully processed and has an associated job
     * - "UNCOLLECTED": Failed processing and is in retry queue
     */
    @JsonProperty("status")
    private String status;

    /**
     * Associated job if scan was successfully collected.
     * Null for uncollected scans.
     */
    @JsonProperty("associatedJob")
    private JobResponseDto associatedJob;

    /**
     * Retry information if scan is uncollected.
     * Null for successfully collected scans.
     */
    @JsonProperty("retryInfo")
    private OshRetryInfoDto retryInfo;

    /**
     * Timestamp when the scan was processed or added to retry queue.
     * For collected scans: job creation time
     * For uncollected scans: time added to retry queue
     */
    @JsonProperty("processedAt")
    private LocalDateTime processedAt;

    /**
     * DTO representing retry information for an OSH scan.
     *
     * Contains details about retry attempts, failure reasons, and timing.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OshRetryInfoDto {

        @JsonProperty("retryAttempts")
        private Integer retryAttempts;

        @JsonProperty("maxRetries")
        private Integer maxRetries;

        @JsonProperty("failureReason")
        private String failureReason;

        @JsonProperty("lastAttemptAt")
        private LocalDateTime lastAttemptAt;
    }
}
