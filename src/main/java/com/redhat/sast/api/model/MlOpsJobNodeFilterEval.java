package com.redhat.sast.api.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing filter node evaluation results.
 * Stores FAISS vector search performance metrics for finding similar known issues.
 */
@Entity
@Table(name = "mlops_job_node_filter_eval")
@Data
@NoArgsConstructor
public class MlOpsJobNodeFilterEval {

    @Id
    private Long id;

    @Column(name = "total_tokens")
    private Integer totalTokens;

    @Column(name = "llm_call_count")
    private Integer llmCallCount;

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
