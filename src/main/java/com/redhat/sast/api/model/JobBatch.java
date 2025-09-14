package com.redhat.sast.api.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.redhat.sast.api.enums.BatchStatus;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "job_batch",
        indexes = {@Index(name = "idx_job_batch_id", columnList = "id")})
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"jobs", "jobBatchExecutionContext", "jobBatchRunDefinition"})
public class JobBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
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

    @Column(name = "use_known_false_positive_file")
    private Boolean useKnownFalsePositiveFile;

    @OneToMany(mappedBy = "jobBatch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Job> jobs;

    @OneToOne(mappedBy = "jobBatch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private JobBatchExecutionContext jobBatchExecutionContext;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_batch_run_definition_id")
    private JobBatchRunDefinition jobBatchRunDefinition;

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

    public List<Job> getJobs() {
        if (jobs == null) {
            jobs = new ArrayList<>();
        }
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs != null ? new ArrayList<>(jobs) : new ArrayList<>();
    }
}
