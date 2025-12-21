package com.redhat.sast.api.repository.mlops;

import java.time.LocalDateTime;
import java.util.List;

import com.redhat.sast.api.model.MlOpsBatch;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MlOpsBatchRepository implements PanacheRepository<MlOpsBatch> {

    /**
     * Finds MLOps batches that are potentially stuck in PROCESSING status.
     *
     * @param threshold batches with lastUpdatedAt before this datetime are potentially stuck
     * @return list of potentially stuck batches with jobs fetched, ordered by
     *         lastUpdatedAt ascending (oldest first)
     */
    public List<MlOpsBatch> findStuckBatches(LocalDateTime threshold) {
        return getEntityManager()
                .createQuery(
                        """
                        SELECT DISTINCT b FROM MlOpsBatch b
                        LEFT JOIN FETCH b.jobs
                        WHERE b.status = 'PROCESSING'
                        AND b.lastUpdatedAt < :threshold
                        ORDER BY b.lastUpdatedAt ASC
                        """,
                        MlOpsBatch.class)
                .setParameter("threshold", threshold)
                .getResultList();
    }
}
