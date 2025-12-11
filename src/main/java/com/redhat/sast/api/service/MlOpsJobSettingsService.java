package com.redhat.sast.api.service;

import java.util.List;
import java.util.Optional;

import com.redhat.sast.api.common.constants.ApplicationConstants;
import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.model.MlOpsJobSettings;
import com.redhat.sast.api.repository.MlOpsJobRepository;

import io.fabric8.tekton.v1.Param;
import io.fabric8.tekton.v1.PipelineRun;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for extracting and saving MLOps job settings from PipelineRun parameters.
 * Populates the mlops_job_settings table with actual values used in the pipeline execution.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MlOpsJobSettingsService {

    private final MlOpsJobRepository mlOpsJobRepository;

    /**
     * Extracts job settings from PipelineRun parameters and saves them to the database.
     * This captures the actual configuration that was used during pipeline execution.
     *
     * @param jobId the MLOps job ID
     * @param pipelineRun the completed PipelineRun
     */
    @Transactional
    public void extractAndSaveJobSettings(Long jobId, PipelineRun pipelineRun) {
        try {
            MlOpsJob job = mlOpsJobRepository.findById(jobId);
            if (job == null) {
                LOGGER.warn("MLOps job {} not found, cannot save job settings", jobId);
                return;
            }

            List<Param> params = pipelineRun.getSpec().getParams();
            if (params == null || params.isEmpty()) {
                LOGGER.warn("No parameters found in PipelineRun for job {}", jobId);
                return;
            }

            // Create or update job settings
            MlOpsJobSettings settings = job.getMlOpsJobSettings();
            if (settings == null) {
                settings = new MlOpsJobSettings();
                settings.setMlOpsJob(job);
                job.setMlOpsJobSettings(settings);
            }

            // Extract values from pipeline parameters
            String llmUrl = getParamValue(params, "LLM_URL");
            String llmModelName = getParamValue(params, "LLM_MODEL_NAME");
            String embeddingsUrl = getParamValue(params, "EMBEDDINGS_LLM_URL");
            String embeddingsModelName = getParamValue(params, "EMBEDDINGS_LLM_MODEL_NAME");
            String useKnownFalsePositiveFileStr = getParamValue(params, "USE_KNOWN_FALSE_POSITIVE_FILE");
            String evaluateSpecificNode = getParamValue(params, "EVALUATE_SPECIFIC_NODE");

            // Set values in settings entity
            settings.setLlmUrl(llmUrl);
            settings.setLlmModelName(llmModelName);
            settings.setEmbeddingLlmUrl(embeddingsUrl);
            settings.setEmbeddingLlmModelName(embeddingsModelName);

            // Parse boolean value
            if (useKnownFalsePositiveFileStr != null) {
                settings.setUseKnownFalsePositiveFile(Boolean.parseBoolean(useKnownFalsePositiveFileStr));
            }

            settings.setEvaluateSpecificNode(evaluateSpecificNode);

            // Note: We don't save secret_name as it's not relevant for actual execution values
            settings.setSecretName(null);

            mlOpsJobRepository.persist(job);

            LOGGER.info(
                    "Extracted and saved job settings for MLOps job {} from PipelineRun (LLM: {}, Model: {}, EvaluateNode: {})",
                    jobId,
                    llmUrl,
                    llmModelName,
                    evaluateSpecificNode);

        } catch (Exception e) {
            LOGGER.error("Failed to extract job settings from PipelineRun for job {}: {}", jobId, e.getMessage(), e);
        }
    }

    /**
     * Helper method to extract parameter value by name from pipeline parameters.
     */
    private String getParamValue(List<Param> params, String paramName) {
        Optional<Param> param =
                params.stream().filter(p -> paramName.equals(p.getName())).findFirst();

        if (param.isPresent() && param.get().getValue() != null) {
            String value = param.get().getValue().getStringVal();
            return ApplicationConstants.IS_NOT_NULL_AND_NOT_BLANK.test(value) ? value : null;
        }

        return null;
    }
}
