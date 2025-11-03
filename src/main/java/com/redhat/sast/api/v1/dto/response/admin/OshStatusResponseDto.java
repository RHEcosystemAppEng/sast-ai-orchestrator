package com.redhat.sast.api.v1.dto.response.admin;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * Response DTO for overall OSH integration status.
 *
 * Provides comprehensive status information including:
 * - Retry queue status and statistics
 * - Scheduler cursor position
 * - Overall health indicators
 */
@Data
public class OshStatusResponseDto {

    /**
     * Current retry queue status summary.
     */
    private String retryQueueStatus;

    /**
     * Current scheduler cursor status.
     */
    private String cursorStatus;

    /**
     * Timestamp when this status was generated.
     */
    private LocalDateTime timestamp;

    /**
     * Overall health status of OSH integration.
     */
    private String overallStatus;

    /**
     * Any additional notes or warnings.
     */
    private String notes;

    public OshStatusResponseDto() {
        this.timestamp = LocalDateTime.now();
        this.overallStatus = "UNKNOWN";
    }
}
