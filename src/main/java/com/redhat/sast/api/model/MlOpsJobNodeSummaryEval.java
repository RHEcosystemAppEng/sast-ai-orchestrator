package com.redhat.sast.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing summary node evaluation results.
 * Stores quality metrics for LLM-generated summaries.
 */
@Entity
@Table(name = "mlops_job_node_summary_eval")
@Data
@NoArgsConstructor
public class MlOpsJobNodeSummaryEval {

    @Id
    private Long id;

    @Column(name = "overall_score", precision = 5, scale = 4)
    private BigDecimal overallScore;

    @Column(name = "semantic_similarity", precision = 5, scale = 4)
    private BigDecimal semanticSimilarity;

    @Column(name = "factual_accuracy", precision = 5, scale = 4)
    private BigDecimal factualAccuracy;

    @Column(name = "conciseness", precision = 5, scale = 4)
    private BigDecimal conciseness;

    @Column(name = "professional_tone", precision = 5, scale = 4)
    private BigDecimal professionalTone;

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
