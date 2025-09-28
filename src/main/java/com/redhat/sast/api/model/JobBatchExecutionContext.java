package com.redhat.sast.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "job_batch_execution_context",
        indexes = {@Index(name = "idx_job_batch_execution_context_id", columnList = "id")})
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"jobBatch"})
public class JobBatchExecutionContext {

    @Id
    private Long id; // Shared primary key with JobBatch

    @Column(name = "environment", length = 100, nullable = false)
    private String environment;

    @Column(name = "config_version", length = 50, nullable = false)
    private String configVersion;

    @Column(name = "hw_spec", columnDefinition = "jsonb", nullable = false)
    private String hwSpec;

    // Shared primary key relationship with JobBatch
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private JobBatch jobBatch;
}
