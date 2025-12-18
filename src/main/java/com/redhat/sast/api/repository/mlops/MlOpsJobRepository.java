package com.redhat.sast.api.repository.mlops;

import java.time.LocalDateTime;
import java.util.List;

import com.redhat.sast.api.model.MlOpsJob;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MlOpsJobRepository implements PanacheRepository<MlOpsJob> {

    /**
     * Finds potentially orphaned MLOps jobs that may need recovery.
     * A (potentially) orphaned MLOps job will be in a non-terminal state (PENDING, SCHEDULED, RUNNING)
     * but hasn't been updated recently, suggesting its watcher may have been lost.
     *
     * @param threshold jobs with lastUpdatedAt before this datetime are potentially orphaned
     * @param maxResults maximum number of jobs to return
     * @return list of potentially orphaned jobs, ordered by lastUpdatedAt ascending (oldest
     *         first)
     */
    public List<MlOpsJob> findOrphanedJobs(LocalDateTime threshold, int maxResults) {
        return getEntityManager()
                .createQuery(
                        """
                        SELECT j FROM MlOpsJob j
                        WHERE j.status IN ('PENDING', 'SCHEDULED', 'RUNNING')
                        AND j.lastUpdatedAt < :threshold
                        AND j.tektonUrl IS NOT NULL
                        ORDER BY j.lastUpdatedAt ASC
                        """,
                        MlOpsJob.class)
                .setParameter("threshold", threshold)
                .setMaxResults(maxResults)
                .getResultList();
    }
}
