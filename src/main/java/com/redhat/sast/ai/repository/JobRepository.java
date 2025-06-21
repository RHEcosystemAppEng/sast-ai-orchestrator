package com.redhat.sast.ai.repository;

import com.redhat.sast.ai.enums.JobStatus;
import com.redhat.sast.ai.model.Job;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

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
                    .page(page).list();
        } else if (packageName != null) {
            return find("packageName = ?1", packageName)
                    .page(page).list();
        } else if (status != null) {
            return find("status = ?1", status)
                    .page(page).list();
        } else {
            return findAll().page(page).list();
        }
    }

    public List<String> findDistinctPackageNames() {
        return getEntityManager()
            .createQuery("SELECT DISTINCT j.packageName FROM Job j WHERE j.packageName IS NOT NULL", String.class)
            .getResultList();
    }
} 