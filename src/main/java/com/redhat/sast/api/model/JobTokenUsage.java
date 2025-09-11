package com.redhat.sast.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "job_token_usage",
        indexes = {
            @Index(name = "idx_job_token_usage_created_at", columnList = "created_at"),
            @Index(name = "idx_job_token_usage_total_tokens", columnList = "total_tokens"),
            @Index(name = "idx_job_token_usage_estimated_cost", columnList = "estimated_cost")
        })
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "job")
public class JobTokenUsage {

    @Id
    private Long id; // Shared primary key with Job

    @Column(name = "total_input_tokens", nullable = false)
    private Integer totalInputTokens;

    @Column(name = "total_output_tokens", nullable = false)
    private Integer totalOutputTokens;

    @Column(name = "total_tokens", nullable = false)
    private Integer totalTokens;

    @Column(name = "node_breakdown", columnDefinition = "jsonb", nullable = false)
    private String nodeBreakdown;

    @Column(name = "estimated_cost", precision = 10, scale = 4, nullable = false)
    private BigDecimal estimatedCost;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Shared primary key relationship with Job
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Job job;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
