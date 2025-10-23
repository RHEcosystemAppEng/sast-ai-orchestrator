package com.redhat.sast.api.repository;

import java.util.List;
import java.util.Optional;

import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.model.Job;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
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
        if (packageName != null && status != null) {
            return find("packageName = ?1 and status = ?2", packageName, status)
                    .page(page)
                    .list();
        } else if (packageName != null) {
            return find("packageName = ?1", packageName).page(page).list();
        } else if (status != null) {
            return find("status = ?1", status).page(page).list();
        } else {
            return findAll().page(page).list();
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
        if (oshScanId == null || oshScanId.trim().isEmpty()) {
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
     * Finds all jobs that have an associated OSH scan ID (collected scans).
     * Used by the dashboard to display jobs created from OSH scans.
     *
     * @return List of jobs with non-null oshScanId, ordered by creation date descending
     */
    public List<Job> findByOshScanIdNotNull() {
        return find("SELECT j FROM Job j WHERE j.oshScanId IS NOT NULL ORDER BY j.createdAt DESC")
                .list();
    }
}
