package com.redhat.sast.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "mlops_job_token_usage",
        indexes = {@Index(name = "idx_mlops_job_token_usage_id", columnList = "id")})
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "mlOpsJob")
public class MlOpsJobTokenUsage {

    @Id
    private Long id; // Shared primary key with MlOpsJob

    @Column(name = "total_input_tokens", nullable = false)
    private Integer totalInputTokens;

    @Column(name = "total_output_tokens", nullable = false)
    private Integer totalOutputTokens;

    @Column(name = "total_tokens", nullable = false)
    private Integer totalTokens;

    @Column(name = "total_duration_seconds", precision = 10, scale = 3)
    private BigDecimal totalDurationSeconds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "node_breakdown", columnDefinition = "jsonb", nullable = false)
    private String nodeBreakdown;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Shared primary key relationship with MlOpsJob
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private MlOpsJob mlOpsJob;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
