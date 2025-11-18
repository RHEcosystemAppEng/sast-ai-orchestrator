package com.redhat.sast.api.model;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "job_batch_data_flow",
        indexes = {@Index(name = "idx_job_batch_data_flow_id", columnList = "id")})
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "jobBatch")
public class JobBatchDataFlow {

    @Id
    private Long id; // Shared primary key with JobBatch

    @Column(name = "flow_id", unique = true, nullable = false)
    private String flowId;

    @Column(name = "input_artifacts", columnDefinition = "jsonb", nullable = false)
    private List<String> inputArtifacts;

    @Column(name = "transformation_type", length = 100, nullable = false)
    private String transformationType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Shared primary key relationship with JobBatch (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private JobBatch jobBatch;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
