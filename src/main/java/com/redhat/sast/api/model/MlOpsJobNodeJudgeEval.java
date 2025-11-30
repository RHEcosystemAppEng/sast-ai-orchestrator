package com.redhat.sast.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing judge LLM node evaluation results.
 * Stores quality metrics for LLM-generated justifications/analysis.
 */
@Entity
@Table(name = "mlops_job_node_judge_eval")
@Data
@NoArgsConstructor
public class MlOpsJobNodeJudgeEval {

    @Id
    private Long id;

    @Column(name = "overall_score", precision = 5, scale = 4)
    private BigDecimal overallScore;

    @Column(name = "clarity", precision = 5, scale = 4)
    private BigDecimal clarity;

    @Column(name = "completeness", precision = 5, scale = 4)
    private BigDecimal completeness;

    @Column(name = "technical_accuracy", precision = 5, scale = 4)
    private BigDecimal technicalAccuracy;

    @Column(name = "logical_flow", precision = 5, scale = 4)
    private BigDecimal logicalFlow;

    @Column(name = "total_tokens")
    private Integer totalTokens;

    @Column(name = "llm_call_count")
    private Integer llmCallCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private MlOpsJob mlOpsJob;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
