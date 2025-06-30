package com.redhat.sast.api.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.redhat.sast.api.enums.BatchStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "job_batch")
public class JobBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_google_sheet_url", nullable = false)
    private String batchGoogleSheetUrl;

    @Column(name = "submitted_by")
    private String submittedBy;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BatchStatus status;

    @Column(name = "total_jobs")
    private Integer totalJobs;

    @Column(name = "completed_jobs")
    private Integer completedJobs;

    @Column(name = "failed_jobs")
    private Integer failedJobs;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @OneToMany(mappedBy = "jobBatch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Job> jobs;

    @PrePersist
    public void prePersist() {
        this.submittedAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
        this.status = BatchStatus.PROCESSING;
        this.totalJobs = 0;
        this.completedJobs = 0;
        this.failedJobs = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchGoogleSheetUrl() {
        return batchGoogleSheetUrl;
    }

    public void setBatchGoogleSheetUrl(String batchGoogleSheetUrl) {
        this.batchGoogleSheetUrl = batchGoogleSheetUrl;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
    }

    public Integer getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(Integer totalJobs) {
        this.totalJobs = totalJobs;
    }

    public Integer getCompletedJobs() {
        return completedJobs;
    }

    public void setCompletedJobs(Integer completedJobs) {
        this.completedJobs = completedJobs;
    }

    public Integer getFailedJobs() {
        return failedJobs;
    }

    public void setFailedJobs(Integer failedJobs) {
        this.failedJobs = failedJobs;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public List<Job> getJobs() {
        return jobs != null ? new ArrayList<>(jobs) : new ArrayList<>();
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs != null ? new ArrayList<>(jobs) : new ArrayList<>();
    }
}
