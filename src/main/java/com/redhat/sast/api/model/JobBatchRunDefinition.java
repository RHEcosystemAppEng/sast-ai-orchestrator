package com.redhat.sast.api.model;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "job_batch_run_definitions",
        indexes = {
            @Index(name = "idx_job_batch_run_def_definition_id", columnList = "definition_id"),
            @Index(name = "idx_job_batch_run_def_name", columnList = "name"),
            @Index(name = "idx_job_batch_run_def_version", columnList = "version"),
            @Index(name = "idx_job_batch_run_def_git_hash", columnList = "git_hash"),
            @Index(name = "idx_job_batch_run_def_created_at", columnList = "created_at")
        })
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JobBatchRunDefinition extends PanacheEntity {

    @Column(name = "definition_id", unique = true, nullable = false)
    private String definitionId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "version", length = 50, nullable = false)
    private String version;

    @Column(name = "git_hash", length = 40, nullable = false)
    private String gitHash;

    @Column(name = "description")
    private String description;

    @Column(name = "config_version", length = 50, nullable = false)
    private String configVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_graph_topology_id")
    private WorkflowGraphTopology workflowGraphTopology;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
