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
@Table(name = "mlops_batch")
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"jobs"})
public class MlOpsBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "testing_data_nvrs_version", nullable = false, length = 100)
    private String testingDataNvrsVersion;

    @Column(name = "prompts_version", nullable = false, length = 100)
    private String promptsVersion;

    @Column(name = "known_non_issues_version", nullable = false, length = 100)
    private String knownNonIssuesVersion;

    @Column(name = "container_image", nullable = false, length = 500)
    private String containerImage;

    @Column(name = "submitted_by")
    private String submittedBy;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private BatchStatus status;

    @Column(name = "total_jobs")
    private Integer totalJobs;

    @Column(name = "completed_jobs")
    private Integer completedJobs;

    @Column(name = "failed_jobs")
    private Integer failedJobs;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @OneToMany(mappedBy = "mlOpsBatch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MlOpsJob> jobs;

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

    public List<MlOpsJob> getJobs() {
        if (jobs == null) {
            jobs = new ArrayList<>();
        }
        return jobs;
    }

    public void setJobs(List<MlOpsJob> jobs) {
        this.jobs = jobs != null ? new ArrayList<>(jobs) : new ArrayList<>();
    }
}
