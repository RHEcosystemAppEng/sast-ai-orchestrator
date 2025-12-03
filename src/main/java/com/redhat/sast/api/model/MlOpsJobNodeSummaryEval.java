package com.redhat.sast.api.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity representing summary node evaluation results.
 * Stores quality metrics for LLM-generated summaries.
 */
@Entity
@Table(name = "mlops_job_node_summary_eval")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MlOpsJobNodeSummaryEval extends MlOpsJobNodeEvalBase {

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
}
