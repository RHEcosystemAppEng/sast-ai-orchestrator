package com.redhat.sast.api.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "mlops_job_issue",
        indexes = {
            @Index(name = "idx_mlops_job_issue_mlops_job_id", columnList = "mlops_job_id"),
            @Index(name = "idx_mlops_job_issue_result", columnList = "investigation_result")
        })
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "mlOpsJob")
public class MlOpsJobIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mlops_job_id", nullable = false)
    private MlOpsJob mlOpsJob;

    @Column(name = "issue_id", length = 50, nullable = false)
    private String issueId;

    @Column(name = "issue_name", length = 100)
    private String issueName;

    @Column(name = "investigation_result", length = 50)
    private String investigationResult;

    @Column(name = "hint", columnDefinition = "TEXT")
    private String hint;

    @Column(name = "answer_relevancy", length = 20)
    private String answerRelevancy;

    @Column(name = "s3_file_url", columnDefinition = "TEXT")
    private String s3FileUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
