package com.redhat.sast.api.repository;

import java.time.Instant;

import com.redhat.sast.api.enums.BatchStatus;
import com.redhat.sast.api.model.JobBatch;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JobBatchRepository implements PanacheRepository<JobBatch> {

    /**
     * Counts batches with a specific status within a time range.
     * Used for time-filtered dashboard summaries.
     * Filters by submittedAt timestamp.
     *
     * @param status the batch status to count
     * @param startTime the start of the time range (inclusive)
     * @param endTime the end of the time range (inclusive)
     * @return count of batches with the given status in the time range
     */
    public long countByStatusInTimeRange(BatchStatus status, Instant startTime, Instant endTime) {
        return count("status = ?1 AND submittedAt >= ?2 AND submittedAt <= ?3", status, startTime, endTime);
    }

    /**
     * Counts all batches within a time range.
     * Used for time-filtered dashboard summaries.
     * Filters by submittedAt timestamp.
     *
     * @param startTime the start of the time range (inclusive)
     * @param endTime the end of the time range (inclusive)
     * @return count of all batches in the time range
     */
    public long countInTimeRange(Instant startTime, Instant endTime) {
        return count("submittedAt >= ?1 AND submittedAt <= ?2", startTime, endTime);
    }
}
