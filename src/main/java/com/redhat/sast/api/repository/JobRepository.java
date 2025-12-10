package com.redhat.sast.api.repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.model.Job;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JobRepository implements PanacheRepository<Job> {

    public List<Job> findByPackageName(String packageName) {
        return list("packageName", packageName);
    }

    public List<Job> findByStatus(JobStatus status) {
        return list("status", status);
    }

    public List<Job> findByPackageNameAndStatus(String packageName, JobStatus status) {
        return list("packageName = ?1 and status = ?2", packageName, status);
    }

    public List<Job> findJobsWithPagination(String packageName, JobStatus status, Page page) {
        Sort sort = Sort.by("createdAt").descending();
        if (packageName != null && status != null) {
            return find("packageName = ?1 and status = ?2", sort, packageName, status)
                    .page(page)
                    .list();
        } else if (packageName != null) {
            return find("packageName = ?1", sort, packageName).page(page).list();
        } else if (status != null) {
            return find("status = ?1", sort, status).page(page).list();
        } else {
            return findAll(sort).page(page).list();
        }
    }

    /**
     * Finds a job by OSH scan ID for idempotency checking.
     * Used to prevent duplicate job creation from the same OSH scan.
     *
     * @param oshScanId the OSH scan ID to search for
     * @return Optional containing the Job if found, empty otherwise
     */
    public Optional<Job> findByOshScanId(String oshScanId) {
        if (oshScanId == null || oshScanId.isBlank()) {
            return Optional.empty();
        }
        return find("oshScanId", oshScanId).firstResultOptional();
    }

    public List<String> findDistinctPackageNames() {
        return getEntityManager()
                .createQuery("SELECT DISTINCT j.packageName FROM Job j WHERE j.packageName IS NOT NULL", String.class)
                .getResultList();
    }

    /**
     * Finds jobs that have an associated OSH scan ID with pagination.
     * Results are ordered by creation time descending (newest first).
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @return list of jobs with OSH scan IDs
     */
    public List<Job> findJobsWithOshScanId(int page, int size) {
        return find("oshScanId IS NOT NULL ORDER BY createdAt DESC")
                .page(Page.of(page, size))
                .list();
    }

    /**
     * Counts jobs that have an associated OSH scan ID (collected scans).
     *
     * @return number of jobs with non-null oshScanId
     */
    public long countJobsWithOshScanId() {
        return getEntityManager()
                .createQuery("SELECT COUNT(j) FROM Job j WHERE j.oshScanId IS NOT NULL", Long.class)
                .getSingleResult();
    }

    /**
     * @param status the job status to count
     * @param startTime the start of the time window
     * @param endTime the end of the time window
     * @return count of jobs with the given status in the time window
     */
    public long countByStatusInTimeWindow(JobStatus status, Instant startTime, Instant endTime) {
        return count("status = ?1 AND createdAt >= ?2 AND createdAt < ?3", status, startTime, endTime);
    }

    /**
     * Gets OSH scan statistics grouped by package name.
     * Returns a map with package names as keys and OshScanStats objects containing:
     * - Total OSH scan count
     * - Last OSH scan date
     * - Completed OSH scans count
     * - Failed OSH scans count
     *
     * @return Map of package name to OshScanStats
     */
    public Map<String, OshScanStats> getOshScanStatsByPackage() {
        List<Object[]> results = getEntityManager()
                .createQuery(
                        """
                        SELECT
                            j.packageName,
                            COUNT(j),
                            MAX(j.createdAt),
                            SUM(CASE WHEN j.status = 'COMPLETED' THEN 1 ELSE 0 END),
                            SUM(CASE WHEN j.status = 'FAILED' THEN 1 ELSE 0 END)
                        FROM Job j
                        WHERE j.oshScanId IS NOT NULL AND j.packageName IS NOT NULL
                        GROUP BY j.packageName
                        """,
                        Object[].class)
                .getResultList();

        Map<String, OshScanStats> statsMap = new HashMap<>();
        for (Object[] row : results) {
            String packageName = (String) row[0];
            Long scanCount = (Long) row[1];
            Instant lastScanDate = (Instant) row[2];
            Long completedScans = (Long) row[3];
            Long failedScans = (Long) row[4];

            statsMap.put(
                    packageName,
                    new OshScanStats(
                            scanCount.intValue(), lastScanDate, completedScans.intValue(), failedScans.intValue()));
        }

        return statsMap;
    }

    /**
     * Data class to hold OSH scan statistics for a package.
     */
    public static class OshScanStats {
        private final int scanCount;
        private final Instant lastScanDate;
        private final int completedScans;
        private final int failedScans;

        public OshScanStats(int scanCount, Instant lastScanDate, int completedScans, int failedScans) {
            this.scanCount = scanCount;
            this.lastScanDate = lastScanDate;
            this.completedScans = completedScans;
            this.failedScans = failedScans;
        }

        public int getScanCount() {
            return scanCount;
        }

        public Instant getLastScanDate() {
            return lastScanDate;
        }

        public int getCompletedScans() {
            return completedScans;
        }

        public int getFailedScans() {
            return failedScans;
        }
    }
}
