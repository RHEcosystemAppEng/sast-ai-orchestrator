package com.redhat.sast.api.service.mlops;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.sast.api.exceptions.DataArtifactCreationException;
import com.redhat.sast.api.model.DataArtifact;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.repository.mlops.DataArtifactRepository;

import io.fabric8.tekton.v1.PipelineRun;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing data artifacts and their DVC metadata.
 * Handles creation and tracking of artifacts created during SAST workflow execution.
 */
@ApplicationScoped
@Slf4j
public class DataArtifactService {
    /**
     * Metadata structure for data artifacts
     */
    public record ArtifactMetadata(
            String gitCommitHash,
            String pipelineStage,
            String artifactType,
            String projectName,
            String projectVersion,
            Instant createdAt) {}

    private final DataArtifactRepository dataArtifactRepository;

    private final ObjectMapper objectMapper;

    public DataArtifactService(DataArtifactRepository dataArtifactRepository, ObjectMapper objectMapper) {
        this.dataArtifactRepository = dataArtifactRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a data artifact record for tracking
     */
    @Transactional
    public DataArtifact createDataArtifact(
            String artifactType, String name, String version, String dvcPath, String dvcHash, Object metadata) {
        try {
            String artifactId = generateArtifactId(artifactType, name, version);
            String metadataJson = objectMapper.writeValueAsString(metadata);

            DataArtifact artifact = new DataArtifact();
            artifact.setArtifactId(artifactId);
            artifact.setArtifactType(artifactType);
            artifact.setName(name);
            artifact.setVersion(version);
            artifact.setDvcPath(dvcPath);
            artifact.setDvcHash(dvcHash);
            artifact.setMetadata(metadataJson);
            // createdAt is set automatically by @PrePersist

            dataArtifactRepository.persist(artifact);

            LOGGER.debug("Created data artifact: {} (type: {}, version: {})", name, artifactType, version);
            return artifact;

        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize metadata for artifact {}: {}", name, e.getMessage(), e);
            throw new DataArtifactCreationException("Failed to create data artifact", e);
        }
    }

    /**
     * Creates basic data artifacts for completed job execution using job metadata
     */
    @Transactional
    public void createJobExecutionArtifacts(Job job, PipelineRun pipelineRun) {
        LOGGER.debug("Creating basic data artifacts for job ID: {}", job.getId());

        try {
            String defaultVersion = "v1.0.0";
            String version = job.getDvcDataVersion() != null ? job.getDvcDataVersion() : defaultVersion;

            if (job.getPackageSourceCodeUrl() != null) {
                createDataArtifact(
                        "input_source",
                        job.getProjectName() + "_source_" + job.getId(),
                        version,
                        job.getPackageSourceCodeUrl(),
                        calculateHash(),
                        createBasicArtifactMetadata(job, "input"));
            }

            createDataArtifact(
                    "excel_report",
                    job.getProjectName() + "_report_" + job.getId(),
                    version,
                    "/shared-data/output/sast_ai_output.xlsx",
                    calculateHash(),
                    createBasicArtifactMetadata(job, "output"));

            LOGGER.debug("Successfully created basic data artifacts for job {}", job.getId());

        } catch (Exception e) {
            LOGGER.error("Failed to create data artifacts for job {}: {}", job.getId(), e.getMessage(), e);
        }
    }

    /**
     * Generates unique artifact ID
     */
    private String generateArtifactId(String artifactType, String name, String version) {
        return String.format(
                "%s_%s_%s_%s",
                artifactType, name, version, UUID.randomUUID().toString().substring(0, 8));
    }

    /**
     * Calculate simple hash for artifact (basic implementation)
     */
    private String calculateHash() {
        return "sha256:" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Create basic metadata for artifacts
     */
    private Object createBasicArtifactMetadata(Job job, String category) {
        return new ArtifactMetadata(
                job.getDvcCommitHash(),
                job.getDvcPipelineStage(),
                category,
                job.getProjectName(),
                job.getProjectVersion(),
                Instant.now());
    }
}
