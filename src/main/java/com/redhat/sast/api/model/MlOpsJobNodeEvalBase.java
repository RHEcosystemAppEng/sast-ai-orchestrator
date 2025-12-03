package com.redhat.sast.api.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Abstract base class for MLOps job node evaluation entities.
 * Provides common fields and behavior shared across all node evaluation types
 * (filter, judge, summary).
 */
@MappedSuperclass
@Data
@EqualsAndHashCode(exclude = "mlOpsJob")
@ToString(exclude = "mlOpsJob")
public abstract class MlOpsJobNodeEvalBase {

    @Id
    private Long id;

    @Column(name = "total_tokens")
    private Integer totalTokens;

    @Column(name = "llm_call_count")
    private Integer llmCallCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private MlOpsJob mlOpsJob;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
