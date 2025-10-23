package com.redhat.sast.api.v1.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing an OSH scan with its associated job information.
 * Used by the dashboard to display unified view of collected and uncollected scans.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OshScanWithJobDto {
    private String oshScanId;
    private String packageName;
    private String packageNvr;
    private String status; // "COLLECTED" or "UNCOLLECTED"
    private JobResponseDto associatedJob;
    private RetryInfo retryInfo;
    private String processedAt;

    /**
     * Retry information for uncollected scans.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryInfo {
        private Integer retryAttempts;
        private Integer maxRetries;
        private String failureReason;
        private String lastAttemptAt;
    }
}
