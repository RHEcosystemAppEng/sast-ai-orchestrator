package com.redhat.sast.api.model;

import java.time.LocalDateTime;

import com.redhat.sast.api.enums.JobStatus;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mlops_job")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"mlOpsBatch"})
public class MlOpsJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "project_version")
    private String projectVersion;

    @Column(name = "package_name")
    private String packageName;

    @Column(name = "package_nvr", nullable = false)
    private String packageNvr;

    @Column(name = "osh_scan_id")
    private String oshScanId;

    @Column(name = "package_source_code_url")
    private String packageSourceCodeUrl;

    @Column(name = "jira_link")
    private String jiraLink;

    @Column(name = "hostname")
    private String hostname;

    @Column(name = "known_false_positives_url")
    private String knownFalsePositivesUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private JobStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "tekton_url")
    private String tektonUrl;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "submitted_by")
    private String submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mlops_batch_id", nullable = false)
    private MlOpsBatch mlOpsBatch;

    @OneToOne(mappedBy = "mlOpsJob", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MlOpsJobMetrics mlOpsJobMetrics;

    @OneToOne(mappedBy = "mlOpsJob", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MlOpsJobSettings mlOpsJobSettings;

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
