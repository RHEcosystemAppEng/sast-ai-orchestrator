package com.redhat.sast.api.service;

import java.util.HashMap;
import java.util.Map;

import com.redhat.sast.api.model.DataArtifact;

import io.fabric8.tekton.v1.PipelineRun;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing DVC metadata and data versioning.
 * Provides functionality to track data version tags, pipeline stages, and commit hashes.
 */
@ApplicationScoped
@Slf4j
public class DvcMetadataService {

    private final JobService jobService;

    @Inject
    DataArtifactService dataArtifactService;

    public DvcMetadataService(JobService jobService) {
        this.jobService = jobService;
    }

    /**
     * Extracts DVC metadata from completed Tekton PipelineRun and updates the job.
     * Looks for task results from the execute-ai-analysis task.
     */
    public void extractAndUpdateDvcMetadata(Long jobId, PipelineRun pipelineRun) {
        log.debug(
                "Extracting DVC metadata from PipelineRun: {} for job ID: {}",
                pipelineRun.getMetadata().getName(),
                jobId);

        String dvcDataVersion = null;
        String dvcCommitHash = null;
        String dvcPipelineStage = null;

        String dvcHash = null;
        String dvcPath = null;
        String dvcArtifactType = null;
        String dvcAnalysisSummary = null;
        String dvcRepoUrl = null;
        String dvcRepoBranch = null;
        String dvcSplitType = null;
        String dvcSastReportPath = null;
        String dvcIssuesCount = null;

        try {
            log.debug("Extracting DVC metadata from PipelineRun task results");

            // Extract core DVC metadata (required)
            dvcDataVersion = extractTaskResult(pipelineRun, "dvc-data-version");
            dvcCommitHash = extractTaskResult(pipelineRun, "dvc-commit-hash");
            dvcPipelineStage = extractTaskResult(pipelineRun, "dvc-pipeline-stage");

            // Extract additional artifact metadata from Python workflow
            dvcHash = extractTaskResult(pipelineRun, "dvc-hash");
            dvcPath = extractTaskResult(pipelineRun, "dvc-path");
            dvcArtifactType = extractTaskResult(pipelineRun, "dvc-artifact-type");
            dvcAnalysisSummary = extractTaskResult(pipelineRun, "dvc-source-analysis-summary");
            dvcRepoUrl = extractTaskResult(pipelineRun, "dvc-repo-url");
            dvcRepoBranch = extractTaskResult(pipelineRun, "dvc-repo-branch");

            // Extract additional required metadata fields
            dvcSplitType = extractTaskResult(pipelineRun, "dvc-split-type");
            dvcSastReportPath = extractTaskResult(pipelineRun, "dvc-sast-report-path");
            dvcIssuesCount = extractTaskResult(pipelineRun, "dvc-issues-count");

            log.debug(
                    "Extracted additional DVC metadata - hash: {}, path: {}, type: {}",
                    dvcHash,
                    dvcPath,
                    dvcArtifactType);

            // Validate critical DVC metadata
            if (dvcDataVersion == null || dvcDataVersion.trim().isEmpty()) {
                throw new IllegalStateException(String.format(
                        "DVC data version not provided by workflow for job %d - indicates pipeline failure", jobId));
            }

            if (dvcCommitHash == null || dvcCommitHash.trim().isEmpty()) {
                throw new IllegalStateException(String.format(
                        "Git commit hash not provided by workflow for job %d - indicates pipeline failure", jobId));
            }

            if (dvcPipelineStage == null || dvcPipelineStage.trim().isEmpty()) {
                dvcPipelineStage = "sast_ai_analysis";
                log.warn(
                        "Pipeline stage not provided by workflow for job {}, using default: {}",
                        jobId,
                        dvcPipelineStage);
            }

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error extracting DVC metadata from PipelineRun task results: {}", e.getMessage(), e);
            throw new IllegalStateException(
                    String.format(
                            "Failed to extract DVC metadata from pipeline results for job %d: %s",
                            jobId, e.getMessage()),
                    e);
        }

        log.debug(
                "Extracted DVC metadata for job {}: version={}, commit={}, stage={}",
                jobId,
                dvcDataVersion,
                dvcCommitHash,
                dvcPipelineStage);

        jobService.updateJobDvcMetadata(jobId, dvcDataVersion, dvcCommitHash, dvcPipelineStage);

        if (dvcHash != null && dvcPath != null && dvcArtifactType != null) {
            createDataArtifactFromDvcMetadata(
                    jobId,
                    dvcDataVersion,
                    dvcHash,
                    dvcPath,
                    dvcArtifactType,
                    dvcAnalysisSummary,
                    dvcRepoUrl,
                    dvcRepoBranch,
                    dvcSplitType,
                    dvcSastReportPath,
                    dvcIssuesCount);
        } else {
            log.warn("Incomplete DVC metadata for job {}, skipping data artifact creation", jobId);
        }
    }

    /**
     * Extracts DVC metadata from PipelineRun for data artifact creation.
     * This method provides all available metadata without failing if optional fields are missing.
     *
     * @param pipelineRun The completed Tekton PipelineRun
     * @return Map containing all available DVC metadata fields
     */
    public Map<String, String> extractDvcMetadata(PipelineRun pipelineRun) {
        log.debug(
                "Extracting comprehensive DVC metadata from PipelineRun: {}",
                pipelineRun.getMetadata().getName());

        Map<String, String> metadata = new HashMap<>();

        // Core DVC metadata
        addIfNotEmpty(metadata, "dataVersion", extractTaskResult(pipelineRun, "dvc-data-version"));
        addIfNotEmpty(metadata, "commitHash", extractTaskResult(pipelineRun, "dvc-commit-hash"));
        addIfNotEmpty(metadata, "pipelineStage", extractTaskResult(pipelineRun, "dvc-pipeline-stage"));

        // Artifact metadata from Python workflow
        addIfNotEmpty(metadata, "dvcHash", extractTaskResult(pipelineRun, "dvc-hash"));
        addIfNotEmpty(metadata, "dvcPath", extractTaskResult(pipelineRun, "dvc-path"));
        addIfNotEmpty(metadata, "artifactType", extractTaskResult(pipelineRun, "dvc-artifact-type"));

        // Execution context
        addIfNotEmpty(metadata, "executionTimestamp", extractTaskResult(pipelineRun, "dvc-execution-timestamp"));
        addIfNotEmpty(metadata, "analysisSummary", extractTaskResult(pipelineRun, "dvc-source-analysis-summary"));
        addIfNotEmpty(metadata, "repoUrl", extractTaskResult(pipelineRun, "dvc-repo-url"));
        addIfNotEmpty(metadata, "repoBranch", extractTaskResult(pipelineRun, "dvc-repo-branch"));

        log.debug("Extracted {} DVC metadata fields", metadata.size());
        return metadata;
    }

    /**
     * Creates a data artifact using DVC metadata from Python workflow combined with business metadata from Java
     */
    private void createDataArtifactFromDvcMetadata(
            Long jobId,
            String version,
            String dvcHash,
            String dvcPath,
            String artifactType,
            String analysisSummary,
            String repoUrl,
            String repoBranch,
            String splitType,
            String sastReportPath,
            String issuesCount) {

        log.debug("Creating data artifact for job {} with DVC metadata", jobId);

        try {
            String artifactName = generateArtifactName(jobId, artifactType);

            Map<String, Object> metadata = new HashMap<>();

            if (analysisSummary != null) metadata.put("analysis_summary", analysisSummary);
            if (repoUrl != null) metadata.put("source_code_repo", repoUrl);
            if (repoBranch != null) metadata.put("repo_branch", repoBranch);

            if (splitType != null) metadata.put("split_type", splitType);
            if (sastReportPath != null) metadata.put("sast_report_path", sastReportPath);
            if (issuesCount != null) {
                try {
                    metadata.put("issues_count", Integer.parseInt(issuesCount));
                } catch (NumberFormatException e) {
                    log.warn("Invalid issues count format: {}, setting to 0", issuesCount);
                    metadata.put("issues_count", 0);
                }
            }

            DataArtifact createdArtifact = dataArtifactService.createDataArtifact(
                    artifactType, artifactName, version, dvcPath, dvcHash, metadata);

            log.debug(
                    "Created data artifact for job {}: {} (ID: {}, type: {}, version: {})",
                    jobId,
                    artifactName,
                    createdArtifact.getArtifactId(),
                    artifactType,
                    version);

        } catch (Exception e) {
            log.error("Failed to create data artifact for job {}: {}", jobId, e.getMessage(), e);
        }
    }

    /**
     * Generate artifact name
     */
    private String generateArtifactName(Long jobId, String artifactType) {
        return "default_artifact_name";
    }

    /**
     * Helper method to add non-empty values to metadata map
     */
    private void addIfNotEmpty(Map<String, String> map, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            map.put(key, value.trim());
        }
    }

    /**
     * Extracts a specific task result value from the PipelineRun
     */
    private String extractTaskResult(PipelineRun pipelineRun, String resultName) {
        try {
            if (pipelineRun.getStatus() != null && pipelineRun.getStatus().getResults() != null) {
                var results = pipelineRun.getStatus().getResults();
                for (var result : results) {
                    if (resultName.equals(result.getName())) {
                        var paramValue = result.getValue();
                        String value = paramValue != null ? paramValue.getStringVal() : null;
                        log.debug("Extracted task result {}: {}", resultName, value);
                        return value;
                    }
                }
            }
            log.warn("Task result '{}' not found in PipelineRun", resultName);
            return null;
        } catch (Exception e) {
            log.error("Error extracting task result '{}': {}", resultName, e.getMessage(), e);
            return null;
        }
    }
}
