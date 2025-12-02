package com.redhat.sast.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity representing filter node evaluation results.
 * Stores FAISS vector search performance metrics for finding similar known issues.
 */
@Entity
@Table(name = "mlops_job_node_filter_eval")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MlOpsJobNodeFilterEval extends MlOpsJobNodeEvalBase {

    // FAISS stratified stats - with expected matches
    @Column(name = "with_expected_total")
    private Integer withExpectedTotal;

    @Column(name = "with_expected_faiss_found")
    private Integer withExpectedFaissFound;

    @Column(name = "with_expected_perc_correct")
    private java.math.BigDecimal withExpectedPercCorrect;

    // FAISS stratified stats - without expected matches
    @Column(name = "without_expected_total")
    private Integer withoutExpectedTotal;

    @Column(name = "without_expected_faiss_found")
    private Integer withoutExpectedFaissFound;

    @Column(name = "without_expected_perc_correct")
    private java.math.BigDecimal withoutExpectedPercCorrect;
}
