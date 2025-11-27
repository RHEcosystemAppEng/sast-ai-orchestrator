package com.redhat.sast.api.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "job_metrics",
        indexes = {@Index(name = "idx_job_metrics_id", columnList = "id")})
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "job")
public class JobMetrics {

    @Id
    private Long id; // Shared primary key with Job

    @Column(name = "package_name", nullable = false)
    private String packageName;

    @Column(name = "total_issues", nullable = false)
    private Integer totalIssues;

    @Column(name = "predicted_issues_count", nullable = false)
    private Integer predictedIssuesCount;

    @Column(name = "predicted_non_issues_count", nullable = false)
    private Integer predictedNonIssuesCount;

    @Column(name = "actual_issues_count")
    private Integer actualIssuesCount;

    @Column(name = "actual_non_issues_count")
    private Integer actualNonIssuesCount;

    @Column(name = "has_ground_truth", nullable = false)
    private Boolean hasGroundTruth = false;

    @Column(name = "precision", precision = 5, scale = 4)
    private BigDecimal precision;

    @Column(name = "recall", precision = 5, scale = 4)
    private BigDecimal recall;

    @Column(name = "f1_score", precision = 5, scale = 4)
    private BigDecimal f1Score;

    @Column(name = "accuracy", precision = 5, scale = 4)
    private BigDecimal accuracy;

    @Column(name = "confusion_matrix", columnDefinition = "jsonb")
    private String confusionMatrix;

    @Column(name = "node_metrics", columnDefinition = "jsonb")
    private String nodeMetrics;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Shared primary key relationship with Job (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Job job;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
