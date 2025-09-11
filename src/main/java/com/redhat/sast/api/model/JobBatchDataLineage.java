package com.redhat.sast.api.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "job_batch_data_lineage",
        indexes = {
            @Index(name = "idx_job_batch_data_lineage_lineage_id", columnList = "lineage_id"),
            @Index(name = "idx_job_batch_data_lineage_transformation_type", columnList = "transformation_type"),
            @Index(name = "idx_job_batch_data_lineage_created_at", columnList = "created_at")
        })
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "jobBatch")
public class JobBatchDataLineage {

    @Id
    private Long id; // Shared primary key with JobBatch

    @Column(name = "lineage_id", unique = true, nullable = false)
    private String lineageId;

    @Column(name = "input_artifacts", columnDefinition = "jsonb", nullable = false)
    private String inputArtifacts;

    @Column(name = "transformation_type", length = 100, nullable = false)
    private String transformationType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Shared primary key relationship with JobBatch (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private JobBatch jobBatch;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
