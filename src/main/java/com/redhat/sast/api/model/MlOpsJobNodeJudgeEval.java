package com.redhat.sast.api.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity representing judge LLM node evaluation results.
 * Stores quality metrics for LLM-generated justifications/analysis.
 */
@Entity
@Table(name = "mlops_job_node_judge_eval")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MlOpsJobNodeJudgeEval extends MlOpsJobNodeEvalBase {

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
}
