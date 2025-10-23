package com.redhat.sast.api.v1.dto.response.admin;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * Response DTO for detailed OSH retry statistics and performance metrics.
 *
 * Provides operational insights into:
 * - Retry queue performance
 * - Success/failure rates
 * - Processing metrics
 * - Performance indicators
 */
@Data
public class OshRetryStatisticsResponseDto {

    /**
     * Current retry queue status summary.
     */
    private String queueStatus;

    /**
     * Total number of scans currently in retry queue.
     */
    private long totalInQueue;

    /**
     * Number of scans currently eligible for retry.
     */
    private long eligibleForRetry;

    /**
     * Number of scans awaiting backoff period.
     */
    private long awaitingBackoff;

    /**
     * Number of scans that have exceeded maximum attempts.
     */
    private long exceededMaxAttempts;

    /**
     * Average retry attempts per scan.
     */
    private double averageAttempts;

    /**
     * Oldest scan in retry queue.
     */
    private LocalDateTime oldestScan;

    /**
     * Most recent scan added to retry queue.
     */
    private LocalDateTime newestScan;

    /**
     * Timestamp when these statistics were generated.
     */
    private LocalDateTime timestamp;

    /**
     * Configuration summary for context.
     */
    private String configurationSummary;

    public OshRetryStatisticsResponseDto(String queueStatus, LocalDateTime timestamp) {
        this.queueStatus = queueStatus;
        this.timestamp = timestamp;
        this.totalInQueue = 0;
        this.eligibleForRetry = 0;
        this.awaitingBackoff = 0;
        this.exceededMaxAttempts = 0;
        this.averageAttempts = 0.0;
    }

    public OshRetryStatisticsResponseDto() {
        this(null, LocalDateTime.now());
    }
}
