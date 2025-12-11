package com.redhat.sast.api.service.mlops;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.redhat.sast.api.common.constants.ApplicationConstants;
import com.redhat.sast.api.common.constants.DvcTaskResults;
import com.redhat.sast.api.v1.dto.request.DvcMetadata;
import com.redhat.sast.api.model.DataArtifact;

import com.redhat.sast.api.service.JobService;
import io.fabric8.tekton.v1.ParamValue;
import io.fabric8.tekton.v1.PipelineRun;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing DVC metadata and data versioning.
 * Provides functionality to track data version tags, pipeline stages, and commit hashes.
 */
@ApplicationScoped
@Slf4j
public class DvcMetadataService {

    private record CoreDvcMetadata(String dataVersion, String commitHash, String pipelineStage) {}

    private final JobService jobService;

    private final DataArtifactService dataArtifactService;
    private static final String DEFAULT_PIPELINE_STAGE = "sast_ai_analysis";

    private static final String DEFAULT_ARTIFACT_NAME = "default_artifact_name";

    public DvcMetadataService(JobService jobService, DataArtifactService dataArtifactService) {
        this.jobService = jobService;
        this.dataArtifactService = dataArtifactService;
    }

    /***
     * Extracts DVC metadata from completed Tekton PipelineRun and updates the job.
     * @param jobId
     * @param pipelineRun
     */
    public void updateDvcMetadata(Long jobId, PipelineRun pipelineRun) {
        LOGGER.debug(
                "Extracting DVC metadata from PipelineRun: {} for job ID: {}",
                pipelineRun.getMetadata().getName(),
                jobId);

        Map<String, String> pipelineResultsMap = extractPipelineResults(pipelineRun);
        CoreDvcMetadata coreMetadata = extractCoreMetadata(pipelineResultsMap, jobId);

        jobService.updateJobDvcMetadata(
                jobId, coreMetadata.dataVersion(), coreMetadata.commitHash(), coreMetadata.pipelineStage());

        persistDataArtifact(jobId, coreMetadata.dataVersion(), pipelineResultsMap);
        LOGGER.debug(
                "Successfully processed DVC metadata for job {}: version={}, commit={}, stage={}",
                jobId,
                coreMetadata.dataVersion(),
                coreMetadata.commitHash(),
                coreMetadata.pipelineStage());
    }

    private Map<String, String> extractPipelineResults(PipelineRun pipelineRun) {
        Map<String, String> results = new HashMap<>();

        if (pipelineRun.getStatus() != null && pipelineRun.getStatus().getResults() != null) {

            for (var result : pipelineRun.getStatus().getResults()) {
                String name = result.getName();
                ParamValue value = result.getValue();

                if (name != null && value != null) {
                    String stringVal = value.getStringVal();

                    if (ApplicationConstants.IS_NOT_NULL_AND_NOT_BLANK.test(stringVal)) {
                        results.put(name, stringVal.trim());
                    }
                }
            }
        }

        LOGGER.debug("Extracted {} task results from PipelineRun", results.size());
        return results;
    }

    /**
     * Extracts and validates core DVC metadata required for job processing
     */
    private CoreDvcMetadata extractCoreMetadata(Map<String, String> pipelineResults, Long jobId) {
        String dataVersion = pipelineResults.get(DvcTaskResults.DATA_VERSION);
        String commitHash = pipelineResults.get(DvcTaskResults.COMMIT_HASH);
        String pipelineStage = pipelineResults.get(DvcTaskResults.PIPELINE_STAGE);

        // Validate required fields (null defaultValue means required)
        try {
            validateField("DVC data version", dataVersion, null);
            validateField("Git commit hash", commitHash, null);
        } catch (IllegalStateException e) {
            LOGGER.error("Core DVC metadata validation failed for job {}: {}", jobId, e.getMessage());
            throw e;
        }

        // Apply default for pipeline stage if missing
        if (pipelineStage == null || pipelineStage.isBlank()) {
            pipelineStage = DEFAULT_PIPELINE_STAGE;
            LOGGER.warn("Pipeline stage not provided by workflow for job {}, using default: {}", jobId, pipelineStage);
        }

        return new CoreDvcMetadata(dataVersion, commitHash, pipelineStage);
    }

    private void persistDataArtifact(Long jobId, String dataVersion, Map<String, String> pipelineResults) {
        String dvcHash = pipelineResults.get(DvcTaskResults.HASH);
        String dvcPath = pipelineResults.get(DvcTaskResults.PATH);
        String artifactType = pipelineResults.get(DvcTaskResults.ARTIFACT_TYPE);

        if (dvcHash != null && dvcPath != null && artifactType != null) {
            String repoUrl = validateField("Repository URL", pipelineResults.get(DvcTaskResults.REPO_URL), null);
            String issuesCount = validateField("Issues count", pipelineResults.get(DvcTaskResults.ISSUES_COUNT), "0");

            DvcMetadata metadata = DvcMetadata.builder()
                    .jobId(jobId)
                    .version(dataVersion)
                    .dvcHash(dvcHash)
                    .dvcPath(dvcPath)
                    .artifactType(artifactType)
                    .analysisSummary(pipelineResults.get(DvcTaskResults.ANALYSIS_SUMMARY))
                    .repoUrl(repoUrl)
                    .repoBranch(pipelineResults.get(DvcTaskResults.REPO_BRANCH))
                    .splitType(pipelineResults.get(DvcTaskResults.SPLIT_TYPE))
                    .sastReportPath(pipelineResults.get(DvcTaskResults.SAST_REPORT_PATH))
                    .issuesCount(issuesCount)
                    .build();
            // save data in DataArtifact table
            try {
                saveDataArtifact(metadata);
            } catch (Exception e) {
                throw new IllegalStateException(
                        String.format(
                                "Failed to save data artifact for job %d (version: %s, type: %s): %s",
                                jobId, dataVersion, artifactType, e.getMessage()),
                        e);
            }
        } else {
            LOGGER.warn("Incomplete DVC metadata for job {}, skipping data artifact creation", jobId);
        }
    }

    /**
     * For required fields (defaultValue = null), throws exception on validation failure.
     * For optional fields, returns defaultValue on validation failure.
     */
    private String validateField(String fieldName, String value, String defaultValue) {
        boolean isRequired = (defaultValue == null);

        if (value == null || value.isBlank()) {
            if (isRequired) {
                throw new IllegalStateException(
                        String.format("%s not provided by workflow - indicates pipeline failure", fieldName));
            }
            return defaultValue;
        }

        String trimmedValue = value.trim();

        // Validate based on field type
        return switch (fieldName) {
            case "Git commit hash" -> validateCommitHash(trimmedValue, isRequired);
            case "DVC data version" -> validateDataVersion(trimmedValue, isRequired);
            case "Repository URL" -> validateUrl(trimmedValue, defaultValue, isRequired);
            case "Issues count" -> validateIssuesCount(trimmedValue, defaultValue, isRequired);
            default -> {
                if (!isRequired && trimmedValue.length() > 1000) yield trimmedValue.substring(0, 1000);
                yield trimmedValue;
            }
        };
    }

    /**
     * Validates git commit hash format (7-40 hexadecimal characters)
     */
    private String validateCommitHash(String commitHash, boolean isRequired) {
        if (!commitHash.matches("^[a-f0-9]{7,40}$")) {
            String errorMsg = String.format(
                    "Invalid git commit hash format: '%s' - expected 7-40 hexadecimal characters", commitHash);
            if (isRequired) {
                throw new IllegalStateException(errorMsg);
            }
            return null;
        }
        return commitHash;
    }

    /**
     * Validates DVC data version format (semantic versioning or custom format)
     */
    private String validateDataVersion(String version, boolean isRequired) {
        // Prevent ReDoS by limiting input length
        if (version.length() > 100) {
            String displayVersion = version.substring(0, 50) + "...";
            String errorMsg = String.format(
                    "Invalid DVC data version format: '%s' - version string too long (max 100 characters)",
                    displayVersion);
            if (isRequired) {
                throw new IllegalStateException(errorMsg);
            }
            return null;
        }

        // Accept semantic versioning (v1.0.0, 1.0.0) or custom formats (dev-123, feature-abc)
        if (!version.matches(
                "^(v?\\d+\\.\\d+\\.\\d+(?:-[a-zA-Z0-9]+)?(?:\\+[a-zA-Z0-9]+)?|[a-zA-Z][a-zA-Z0-9_-]{0,49}|\\d{4}-\\d{2}-\\d{2})$")) {
            String errorMsg = String.format(
                    "Invalid DVC data version format: '%s' - expected semantic version (v1.0.0) or valid identifier",
                    version);
            if (isRequired) {
                throw new IllegalStateException(errorMsg);
            }
            return null;
        }
        return version;
    }

    private String validateUrl(String url, String defaultValue, boolean isRequired) {
        if (isValidUrl(url)) {
            return url;
        }
        if (isRequired) {
            throw new IllegalStateException(
                    String.format("Invalid Repository URL format: '%s' - expected valid URL format", url));
        }
        return defaultValue;
    }

    private String validateIssuesCount(String issuesCount, String defaultValue, boolean isRequired) {
        try {
            int count = Integer.parseInt(issuesCount);
            if (count < 0) {
                if (isRequired) {
                    throw new IllegalStateException(String.format("Invalid Issues count: negative value %d", count));
                }
                return defaultValue;
            }
            return issuesCount;
        } catch (NumberFormatException e) {
            if (isRequired) {
                throw new IllegalStateException(
                        String.format("Invalid Issues count format: '%s' - expected integer", issuesCount));
            }
            return defaultValue;
        }
    }

    private boolean isValidUrl(@Nonnull String url) {
        return url.matches("^(https?://|git@)[\\w.-]+[/:][\\w./_-]*$");
    }

    private void saveDataArtifact(@Nonnull DvcMetadata dvcMetadata) {
        LOGGER.debug("Creating data artifact for job {} with DVC metadata", dvcMetadata.getJobId());

        Map<String, Object> metadata = buildMetadataMap(dvcMetadata);

        DataArtifact createdArtifact = dataArtifactService.createDataArtifact(
                dvcMetadata.getArtifactType(),
                DEFAULT_ARTIFACT_NAME,
                dvcMetadata.getVersion(),
                dvcMetadata.getDvcPath(),
                dvcMetadata.getDvcHash(),
                metadata);

        LOGGER.debug(
                "Created data artifact for job {}: {} (ID: {}, type: {}, version: {})",
                dvcMetadata.getJobId(),
                DEFAULT_ARTIFACT_NAME,
                createdArtifact.getArtifactId(),
                dvcMetadata.getArtifactType(),
                dvcMetadata.getVersion());
    }

    /**
     * Builds metadata map from DVC artifact metadata using stream-based approach
     */
    private Map<String, Object> buildMetadataMap(DvcMetadata dvcMetadata) {
        Map<String, Object> result = Stream.of(
                        entry("analysis_summary", dvcMetadata.getAnalysisSummary()),
                        entry("source_code_repo", dvcMetadata.getRepoUrl()),
                        entry("repo_branch", dvcMetadata.getRepoBranch()),
                        entry("split_type", dvcMetadata.getSplitType()),
                        entry("sast_report_path", dvcMetadata.getSastReportPath()))
                .filter(e -> e.getValue() != null)
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);

        // Always add issues count
        result.put("issues_count", parseIssuesCount(dvcMetadata.getIssuesCount()));
        return result;
    }

    /**
     * Helper method to create map entries
     */
    private static SimpleEntry<String, Object> entry(String key, Object value) {
        return new SimpleEntry<>(key, value);
    }

    /**
     * Parses issues count with error handling
     */
    private int parseIssuesCount(String issuesCount) {
        if (issuesCount != null) {
            try {
                return Integer.parseInt(issuesCount);
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid issues count format: {}, setting to 0", issuesCount);
            }
        }
        return 0;
    }
}
