package com.redhat.sast.api.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "data_artifacts")
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class DataArtifact {

    @Id
    @Column(name = "artifact_id", nullable = false)
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
