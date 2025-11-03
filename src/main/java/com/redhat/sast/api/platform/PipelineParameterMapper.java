package com.redhat.sast.api.platform;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.redhat.sast.api.enums.InputSourceType;
import com.redhat.sast.api.model.Job;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.v1.Param;
import io.fabric8.tekton.v1.ParamBuilder;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Maps Job domain objects to Tekton pipeline parameters.
 * Handles the transformation of job data and settings into pipeline-specific parameter format.
 */
@ApplicationScoped
@Slf4j
public class PipelineParameterMapper {

    // Pipeline parameter names
    private static final String PARAM_REPO_REMOTE_URL = "REPO_REMOTE_URL";
    private static final String PARAM_FALSE_POSITIVES_URL = "FALSE_POSITIVES_URL";
    private static final String PARAM_INPUT_REPORT_FILE_PATH = "INPUT_REPORT_FILE_PATH";
    private static final String PARAM_INPUT_REPORT_CONTENT = "INPUT_REPORT_CONTENT";
    private static final String PARAM_INPUT_SOURCE_TYPE = "INPUT_SOURCE_TYPE";
    private static final String PARAM_PROJECT_NAME = "PROJECT_NAME";
    private static final String PARAM_PROJECT_VERSION = "PROJECT_VERSION";
    private static final String PARAM_LLM_URL = "LLM_URL";
    private static final String PARAM_LLM_MODEL_NAME = "LLM_MODEL_NAME";
    private static final String PARAM_LLM_API_KEY = "LLM_API_KEY";
    private static final String PARAM_EMBEDDINGS_LLM_URL = "EMBEDDINGS_LLM_URL";
    private static final String PARAM_EMBEDDINGS_LLM_MODEL_NAME = "EMBEDDINGS_LLM_MODEL_NAME";
    private static final String PARAM_EMBEDDINGS_LLM_API_KEY = "EMBEDDINGS_LLM_API_KEY";
    private static final String PARAM_USE_KNOWN_FALSE_POSITIVE_FILE = "USE_KNOWN_FALSE_POSITIVE_FILE";

    @Inject
    TektonClient tektonClient;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    @ConfigProperty(name = "quarkus.profile", defaultValue = "prod")
    String profile;

    /**
     * Extracts and converts Job data to pipeline parameters.
     *
     * @param job the job containing data to be converted
     * @return list of pipeline parameters
     */
    public List<Param> extractPipelineParams(@Nonnull Job job) {
        List<Param> params = new ArrayList<>();

        addBasicJobParameters(params, job);
        addLlmParameters(params, job);

        return params;
    }

    /**
     * Adds basic job-related pipeline parameters.
     */
    private void addBasicJobParameters(List<Param> params, Job job) {
        params.add(createParam(PARAM_REPO_REMOTE_URL, job.getPackageSourceCodeUrl()));

        Boolean useKnownFalsePositiveFile = getUseKnownFalsePositiveFileValue(job);
        String falsePositivesUrl =
                Boolean.TRUE.equals(useKnownFalsePositiveFile) ? job.getKnownFalsePositivesUrl() : "";
        params.add(createParam(PARAM_FALSE_POSITIVES_URL, falsePositivesUrl));

        addInputSourceParameters(params, job);

        params.add(createParam(PARAM_PROJECT_NAME, job.getProjectName()));
        params.add(createParam(PARAM_PROJECT_VERSION, job.getProjectVersion()));

        params.add(createParam(PARAM_USE_KNOWN_FALSE_POSITIVE_FILE, useKnownFalsePositiveFile.toString()));
    }

    /**
     * Adds input source parameters based on the job's input source type.
     * For OSH scans: passes OSH URL
     * For Google Sheets: passes Google Sheet URL
     */
    private void addInputSourceParameters(List<Param> params, Job job) {
        InputSourceType inputSourceType = job.getInputSourceType();

        if (inputSourceType == null) {
            LOGGER.warn("Job {} has null input source type, defaulting to GOOGLE_SHEET", job.getId());
            inputSourceType = InputSourceType.GOOGLE_SHEET;
        }

        params.add(createParam(PARAM_INPUT_SOURCE_TYPE, inputSourceType.toString()));

        switch (inputSourceType) {
            case OSH_SCAN, GOOGLE_SHEET, SARIF -> {
                params.add(createParam(PARAM_INPUT_REPORT_FILE_PATH, job.getGSheetUrl()));
                params.add(createParam(PARAM_INPUT_REPORT_CONTENT, ""));
                LOGGER.debug("Job {} using {} input with URL: {}", job.getId(), inputSourceType, job.getGSheetUrl());
            }
            default -> {
                LOGGER.warn(
                        "Unknown input source type {} for job {}, using GOOGLE_SHEET fallback",
                        inputSourceType,
                        job.getId());
                params.add(createParam(PARAM_INPUT_REPORT_FILE_PATH, job.getGSheetUrl()));
                params.add(createParam(PARAM_INPUT_REPORT_CONTENT, ""));
            }
        }
    }

    /**
     * Adds LLM-related pipeline parameters based on job settings.
     */
    private void addLlmParameters(List<Param> params, Job job) {
        if (job.getJobSettings() != null) {
            addLlmParametersWithSettings(params, job);
        } else {
            addDefaultLlmParameters(params, job);
        }
    }

    /**
     * Adds LLM parameters when JobSettings are available.
     */
    private void addLlmParametersWithSettings(List<Param> params, Job job) {
        String secretName = job.getJobSettings().getSecretName();
        LOGGER.info("Job {} has JobSettings with secretName: '{}'", job.getId(), secretName);

        LlmSecretValues llmSecretValues = getLlmSecretValues(secretName);

        // Main LLM parameters
        params.add(createParam(PARAM_LLM_URL, llmSecretValues.llmUrl()));
        params.add(createParam(
                PARAM_LLM_MODEL_NAME,
                getModelNameWithFallback(job.getJobSettings().getLlmModelName(), llmSecretValues.llmModelName())));
        params.add(createParam(PARAM_LLM_API_KEY, llmSecretValues.llmApiKey()));

        // Embeddings LLM parameters
        params.add(createParam(PARAM_EMBEDDINGS_LLM_URL, llmSecretValues.embeddingsUrl()));
        params.add(createParam(
                PARAM_EMBEDDINGS_LLM_MODEL_NAME,
                getModelNameWithFallback(
                        job.getJobSettings().getEmbeddingLlmModelName(), llmSecretValues.embeddingsModelName())));
        params.add(createParam(PARAM_EMBEDDINGS_LLM_API_KEY, llmSecretValues.embeddingsApiKey()));
    }

    /**
     * Adds default empty LLM parameters when JobSettings are not available.
     */
    private void addDefaultLlmParameters(List<Param> params, Job job) {
        LOGGER.warn("Job {} has NO JobSettings - using empty LLM values", job.getId());

        params.add(createParam(PARAM_LLM_URL, ""));
        params.add(createParam(PARAM_LLM_MODEL_NAME, ""));
        params.add(createParam(PARAM_LLM_API_KEY, ""));
        params.add(createParam(PARAM_EMBEDDINGS_LLM_URL, ""));
        params.add(createParam(PARAM_EMBEDDINGS_LLM_MODEL_NAME, ""));
        params.add(createParam(PARAM_EMBEDDINGS_LLM_API_KEY, ""));
    }

    /**
     * Helper method to create a pipeline parameter.
     */
    private Param createParam(String name, String value) {
        return new ParamBuilder()
                .withName(name)
                .withNewValue(value != null ? value : "")
                .build();
    }

    /**
     * Reads LLM configuration from Kubernetes secret.
     */
    private LlmSecretValues getLlmSecretValues(String secretName) {
        // Return test values in test mode to avoid Kubernetes calls
        if ("test".equals(profile)) {
            LOGGER.info("TEST MODE: Using mock LLM secret values for secret '{}'", secretName);
            return new LlmSecretValues(
                    "http://test-llm-url",
                    "test-model",
                    "test-api-key",
                    "http://test-embeddings-url",
                    "test-embeddings-model",
                    "test-embeddings-key");
        }

        try {
            if (secretName == null || secretName.trim().isEmpty()) {
                LOGGER.warn("Secret name is null or empty");
                return LlmSecretValues.empty();
            }

            // Use the underlying Kubernetes client from TektonClient to access secrets
            Secret secret = tektonClient
                    .adapt(KubernetesClient.class)
                    .secrets()
                    .inNamespace(namespace)
                    .withName(secretName)
                    .get();
            if (secret == null) {
                LOGGER.warn("Secret '{}' not found in namespace '{}'", secretName, namespace);
                return LlmSecretValues.empty();
            }

            if (secret.getData() == null) {
                LOGGER.warn("Secret '{}' has no data", secretName);
                return LlmSecretValues.empty();
            }

            // Extract and decode all values in one pass
            String llmUrl = getDecodedSecretValue(secret, "llm_url");
            String llmApiKey = getDecodedSecretValue(secret, "llm_api_key");
            String embeddingsUrl = getDecodedSecretValue(secret, "embeddings_llm_url");
            String embeddingsApiKey = getDecodedSecretValue(secret, "embeddings_llm_api_key");
            String llmModelName = getDecodedSecretValue(secret, "llm_model_name");
            String embeddingsModelName = getDecodedSecretValue(secret, "embedding_llm_model_name");

            return new LlmSecretValues(
                    llmUrl, llmApiKey, embeddingsUrl, embeddingsApiKey, llmModelName, embeddingsModelName);
        } catch (Exception e) {
            LOGGER.error("Failed to read secret '{}' from namespace '{}'", secretName, namespace, e);
            return LlmSecretValues.empty();
        }
    }

    private String getDecodedSecretValue(Secret secret, String key) {
        try {
            if (!secret.getData().containsKey(key)) {
                LOGGER.debug(
                        "Secret '{}' does not contain key '{}'",
                        secret.getMetadata().getName(),
                        key);
                return "";
            }

            String encodedValue = secret.getData().get(key);
            return new String(java.util.Base64.getDecoder().decode(encodedValue), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.warn("Failed to decode secret value for key '{}'", key, e);
            return "";
        }
    }

    /**
     * Gets model name with fallback: JobSettings first, then secret value
     */
    private String getModelNameWithFallback(String jobSettingsValue, String secretValue) {
        if (jobSettingsValue != null && !jobSettingsValue.trim().isEmpty()) {
            return jobSettingsValue;
        }
        return secretValue != null ? secretValue : "";
    }

    /**
     * Gets the USE_KNOWN_FALSE_POSITIVE_FILE value with fallback to true
     */
    private Boolean getUseKnownFalsePositiveFileValue(Job job) {
        if (job.getJobSettings() != null && job.getJobSettings().getUseKnownFalsePositiveFile() != null) {
            return job.getJobSettings().getUseKnownFalsePositiveFile();
        }
        return true;
    }
}
