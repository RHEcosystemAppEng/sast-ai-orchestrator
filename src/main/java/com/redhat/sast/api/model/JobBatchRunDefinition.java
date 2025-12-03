package com.redhat.sast.api.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "job_batch_run_definitions",
        indexes = {@Index(name = "idx_job_batch_run_def_id", columnList = "id")})
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class JobBatchRunDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

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
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
