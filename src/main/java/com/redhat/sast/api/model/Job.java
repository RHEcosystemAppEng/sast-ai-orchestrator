package com.redhat.sast.api.model;

import java.time.LocalDateTime;

import com.redhat.sast.api.enums.InputSourceType;
import com.redhat.sast.api.enums.JobStatus;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"jobBatch", "jobSettings"})
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_name")
    protected String projectName;

    @Column(name = "project_version")
    protected String projectVersion;

    @Column(name = "package_name")
    protected String packageName;

    @Column(name = "package_nvr")
    protected String packageNvr;

    @Column(name = "osh_scan_id")
    protected String oshScanId;

    @Column(name = "package_source_code_url")
    private String packageSourceCodeUrl;

    @Column(name = "jira_link")
    protected String jiraLink;

    @Column(name = "hostname")
    protected String hostname;

    @Column(name = "known_false_positives_url")
    private String knownFalsePositivesUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_source_type")
    private InputSourceType inputSourceType;

    @Column(name = "google_sheet_url")
    private String gSheetUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    protected JobStatus status;

    @Column(name = "created_at", nullable = false)
    protected LocalDateTime createdAt;

    @Column(name = "started_at")
    protected LocalDateTime startedAt;

    @Column(name = "completed_at")
    protected LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    protected LocalDateTime cancelledAt;

    @Column(name = "tekton_url")
    protected String tektonUrl;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "submitted_by")
    private String submittedBy;

    @Column(name = "dvc_data_version")
    private String dvcDataVersion;

    @Column(name = "dvc_pipeline_stage")
    private String dvcPipelineStage;

    @Column(name = "dvc_commit_hash")
    private String dvcCommitHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_batch_id")
    private JobBatch jobBatch;

    @OneToOne(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private JobSettings jobSettings;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = JobStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
