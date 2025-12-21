package com.redhat.sast.api.repository;

import java.time.Instant;
import java.util.List;

import com.redhat.sast.api.model.JobBatch;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JobBatchRepository implements PanacheRepository<JobBatch> {

    /**
     * Finds batches that are potentially stuck in PROCESSING status.
     *
     * @param threshold batches with lastUpdatedAt before this instant are potentially stuck
     * @return list of potentially stuck batches with jobs fetched, ordered by
     *         lastUpdatedAt ascending (oldest first)
     */
    public List<JobBatch> findStuckBatches(Instant threshold) {
        return getEntityManager()
                .createQuery(
                        """
                        SELECT DISTINCT b FROM JobBatch b
                        LEFT JOIN FETCH b.jobs
                        WHERE b.status = 'PROCESSING'
                        AND b.lastUpdatedAt < :threshold
                        ORDER BY b.lastUpdatedAt ASC
                        """,
                        JobBatch.class)
                .setParameter("threshold", threshold)
                .getResultList();
    }
}
