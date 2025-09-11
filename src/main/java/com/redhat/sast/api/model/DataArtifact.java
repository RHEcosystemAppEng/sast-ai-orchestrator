package com.redhat.sast.api.model;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "data_artifacts")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DataArtifact extends PanacheEntity {

    @Column(name = "artifact_id", unique = true, nullable = false)
    private String artifactId;

    @Column(name = "artifact_type", length = 100, nullable = false)
    private String artifactType;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "version", length = 100, nullable = false)
    private String version;

    @Column(name = "dvc_path", length = 500, nullable = false)
    private String dvcPath;

    @Column(name = "dvc_hash", nullable = false)
    private String dvcHash;

    @Column(name = "metadata", columnDefinition = "jsonb", nullable = false)
    private String metadata;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
