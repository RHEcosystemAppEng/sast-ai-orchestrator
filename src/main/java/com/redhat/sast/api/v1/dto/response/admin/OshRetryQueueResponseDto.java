package com.redhat.sast.api.v1.dto.response.admin;

import java.time.LocalDateTime;
import java.util.List;

import com.redhat.sast.api.model.OshUncollectedScan;

import lombok.Data;

/**
 * Response DTO for retry queue contents inspection.
 *
 * Provides paginated view of retry queue with:
 * - List of retry records
 * - Pagination information
 * - Sorting and filtering metadata
 */
@Data
public class OshRetryQueueResponseDto {

    /**
     * Current retry queue status summary.
     */
    private String queueStatus;

    /**
     * List of retry records in the queue.
     */
    private List<OshUncollectedScan> retryRecords;

    /**
     * Number of records returned in this response.
     */
    private int returnedCount;

    /**
     * Maximum number of records requested.
     */
    private int requestedLimit;

    /**
     * Total number of records in the queue (for pagination).
     */
    private long totalInQueue;

    /**
     * Sort field used for this response.
     */
    private String sortBy;

    /**
     * Timestamp when this response was generated.
     */
    private LocalDateTime timestamp;

    /**
     * Indicates if there are more records available.
     */
    private boolean hasMore;

    public OshRetryQueueResponseDto(String queueStatus, int requestedLimit, String sortBy, LocalDateTime timestamp) {
        this.queueStatus = queueStatus;
        this.requestedLimit = requestedLimit;
        this.sortBy = sortBy;
        this.timestamp = timestamp;
        this.returnedCount = 0;
        this.totalInQueue = 0;
        this.hasMore = false;
    }

    public OshRetryQueueResponseDto() {
        this(null, 50, "created", LocalDateTime.now());
    }

    /**
     * Updates pagination metadata based on the returned records.
     */
    public void updatePaginationMetadata(List<OshUncollectedScan> records, long totalCount) {
        this.retryRecords = records;
        this.returnedCount = records != null ? records.size() : 0;
        this.totalInQueue = totalCount;
        this.hasMore = totalCount > this.returnedCount;
    }
}
