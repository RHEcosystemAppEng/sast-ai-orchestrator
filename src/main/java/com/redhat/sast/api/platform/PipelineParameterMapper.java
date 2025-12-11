package com.redhat.sast.api.platform;

import static com.redhat.sast.api.common.constants.ApplicationConstants.IS_NOT_NULL_AND_NOT_BLANK;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.redhat.sast.api.common.constants.ApplicationConstants;
import com.redhat.sast.api.enums.InputSourceType;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.model.MlOpsJobSettings;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.v1.Param;
import io.fabric8.tekton.v1.ParamBuilder;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Setter;
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
    private static final String PARAM_LLM_API_TYPE = "LLM_API_TYPE";
    private static final String PARAM_EMBEDDINGS_LLM_URL = "EMBEDDINGS_LLM_URL";
    private static final String PARAM_EMBEDDINGS_LLM_MODEL_NAME = "EMBEDDINGS_LLM_MODEL_NAME";
    private static final String PARAM_EMBEDDINGS_LLM_API_KEY = "EMBEDDINGS_LLM_API_KEY";
    private static final String PARAM_USE_KNOWN_FALSE_POSITIVE_FILE = "USE_KNOWN_FALSE_POSITIVE_FILE";
    private static final String PARAM_AGGREGATE_RESULTS_G_SHEET = "AGGREGATE_RESULTS_G_SHEET";
    private static final String PARAM_GCS_BUCKET_NAME = "GCS_BUCKET_NAME";
    private static final String PARAM_GCS_SA_FILE_NAME = "GCS_SA_FILE_NAME";
    private static final String PARAM_OSH_TASK_ID = "OSH_TASK_ID";
    // MLOps-specific parameter names
    private static final String PARAM_CONTAINER_IMAGE = "CONTAINER_IMAGE";
    private static final String PARAM_PROMPTS_VERSION = "PROMPTS_VERSION";
    private static final String PARAM_KNOWN_NON_ISSUES_VERSION = "KNOWN_NON_ISSUES_VERSION";
    private static final String PARAM_EVALUATE_SPECIFIC_NODE = "EVALUATE_SPECIFIC_NODE";
    private static final String PARAM_S3_ENDPOINT_URL = "S3_ENDPOINT_URL";
    private static final String PARAM_S3_BUCKET_NAME = "S3_BUCKET_NAME";

    @Inject
    TektonClient tektonClient;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    /**
     * -- SETTER --
     *  Sets the profile for testing purposes.
     *
     */
    @Setter
    @ConfigProperty(name = "quarkus.profile", defaultValue = "prod")
    String profile;

    /**
     * -- SETTER --
     *  Sets the GCS bucket name for testing purposes.
     *
     */
    @Setter
    @ConfigProperty(name = "gcs.bucket.name")
    Optional<String> gcsBucketName;

    @ConfigProperty(name = "s3.endpoint.url")
    Optional<String> s3EndpointUrl;

    @ConfigProperty(name = "s3.bucket.name")
    Optional<String> s3BucketName;

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

        if (job.getInputSourceType() == InputSourceType.OSH_SCAN) {
            addGcsParameters(params);
        }

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

        params.add(createParam(PARAM_AGGREGATE_RESULTS_G_SHEET, job.getAggregateResultsGSheet()));
    }

    /**
     * Adds GCS parameters for OSH scan jobs only.
     */
    private void addGcsParameters(List<Param> params) {
        params.add(createParam(PARAM_GCS_BUCKET_NAME, gcsBucketName.orElse("")));
        params.add(createParam(PARAM_GCS_SA_FILE_NAME, "gcs_service_account.json"));
    }

    /**
     * Adds input source parameters based on the job's input source type.
     * For OSH scans: passes OSH URL and OSH task ID
     * For Google Sheets/SARIF: passes input source URL
     */
    private void addInputSourceParameters(List<Param> params, Job job) {
        InputSourceType inputSourceType = job.getInputSourceType();

        if (inputSourceType == null) {
            LOGGER.warn("Job {} has null input source type, defaulting to GOOGLE_SHEET", job.getId());
            inputSourceType = InputSourceType.GOOGLE_SHEET;
        }

        params.add(createParam(PARAM_INPUT_SOURCE_TYPE, inputSourceType.toString()));

        // Common parameters for all input source types
        params.add(createParam(PARAM_INPUT_REPORT_FILE_PATH, job.getGSheetUrl()));
        params.add(createParam(PARAM_INPUT_REPORT_CONTENT, ""));

        // OSH-specific: inject task ID for pipeline traceability
        if (inputSourceType == InputSourceType.OSH_SCAN) {
            String oshTaskId = job.getOshScanId() != null ? job.getOshScanId() : "";
            params.add(createParam(PARAM_OSH_TASK_ID, oshTaskId));
            LOGGER.debug(
                    "Job {} using OSH_SCAN input with URL: {}, OSH task ID: {}",
                    job.getId(),
                    job.getGSheetUrl(),
                    oshTaskId);
        } else {
            LOGGER.debug("Job {} using {} input with URL: {}", job.getId(), inputSourceType, job.getGSheetUrl());
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
        params.add(createParam(
                PARAM_LLM_API_TYPE,
                getLlmApiTypeWithFallback(job.getJobSettings().getLlmApiType(), llmSecretValues.llmApiType())));

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
                    "test-api-key",
                    "openai",
                    "http://test-embeddings-url",
                    "test-embeddings-key",
                    "test-model",
                    "test-embeddings-model");
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
            String llmApiType = getDecodedSecretValue(secret, "llm_api_type");
            String embeddingsUrl = getDecodedSecretValue(secret, "embeddings_llm_url");
            String embeddingsApiKey = getDecodedSecretValue(secret, "embeddings_llm_api_key");
            String llmModelName = getDecodedSecretValue(secret, "llm_model_name");
            String embeddingsModelName = getDecodedSecretValue(secret, "embedding_llm_model_name");

            return new LlmSecretValues(
                    llmUrl, llmApiKey, llmApiType, embeddingsUrl, embeddingsApiKey, llmModelName, embeddingsModelName);
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
        if (IS_NOT_NULL_AND_NOT_BLANK.test(jobSettingsValue)) {
            return jobSettingsValue;
        }
        return secretValue != null ? secretValue : "";
    }

    private String getLlmApiTypeWithFallback(String jobSettingsValue, String secretValue) {
        if (IS_NOT_NULL_AND_NOT_BLANK.test(jobSettingsValue)) {
            return jobSettingsValue;
        }
        if (IS_NOT_NULL_AND_NOT_BLANK.test(secretValue)) {
            return secretValue;
        }
        // Default to "openai" if both DTO and secret are empty
        return "openai";
    }

    private Boolean getUseKnownFalsePositiveFileValue(Job job) {
        if (job.getJobSettings() != null && job.getJobSettings().getUseKnownFalsePositiveFile() != null) {
            return job.getJobSettings().getUseKnownFalsePositiveFile();
        }
        return true;
    }

    private boolean getUseKnownFalsePositiveFileValue(MlOpsJob mlOpsJob) {
        if (mlOpsJob.getMlOpsJobSettings() != null
                && mlOpsJob.getMlOpsJobSettings().getUseKnownFalsePositiveFile() != null) {
            return mlOpsJob.getMlOpsJobSettings().getUseKnownFalsePositiveFile();
        }
        return true;
    }

    /**
     * Extracts and converts MLOpsJob data to pipeline parameters for MLOps workflows.
     * Includes INPUT_REPORT_FILE_PATH for ground truth sheets stored in MinIO.
     *
     * @param mlOpsJob the MLOps job containing data to be converted
     * @param promptsVersion the DVC version for prompts configuration
     * @param knownNonIssuesVersion the DVC version for known non-issues
     * @param containerImage the container image to use for the workflow
     * @return list of pipeline parameters
     */
    public List<Param> extractMlOpsPipelineParams(
            @Nonnull MlOpsJob mlOpsJob,
            @Nonnull String promptsVersion,
            @Nonnull String knownNonIssuesVersion,
            @Nonnull String containerImage) {
        List<Param> result = new ArrayList<>();

        addBasicParameters(result, mlOpsJob);
        addMlOpsInputReportParameters(result, mlOpsJob);

        result.add(createParam(PARAM_CONTAINER_IMAGE, containerImage));
        result.add(createParam(PARAM_PROMPTS_VERSION, promptsVersion));
        result.add(createParam(PARAM_KNOWN_NON_ISSUES_VERSION, knownNonIssuesVersion));

        addMlOpsEvaluationParameter(result, mlOpsJob);
        addS3StorageParameters(result);
        addLlmParameters(result, mlOpsJob);

        LOGGER.debug(
                "Generated MLOps pipeline parameters for job {} (NVR: {}) with prompts={}, knownNonIssues={}, image={}, groundTruth={}",
                mlOpsJob.getId(),
                mlOpsJob.getPackageNvr(),
                promptsVersion,
                knownNonIssuesVersion,
                containerImage,
                buildGroundTruthUrl(mlOpsJob.getPackageNvr()));
        return result;
    }

    private void addBasicParameters(List<Param> params, MlOpsJob mlOpsJob) {
        params.add(createParam(PARAM_REPO_REMOTE_URL, mlOpsJob.getPackageSourceCodeUrl()));
        params.add(createParam(PARAM_PROJECT_NAME, mlOpsJob.getProjectName()));
        params.add(createParam(PARAM_PROJECT_VERSION, mlOpsJob.getProjectVersion()));

        boolean useKnownFalsePositiveFile = getUseKnownFalsePositiveFileValue(mlOpsJob);
        var url = useKnownFalsePositiveFile ? mlOpsJob.getKnownFalsePositivesUrl() : "";

        params.add(createParam(PARAM_USE_KNOWN_FALSE_POSITIVE_FILE, String.valueOf(useKnownFalsePositiveFile)));
        params.add(createParam(PARAM_FALSE_POSITIVES_URL, url));
    }

    private void addMlOpsInputReportParameters(List<Param> params, MlOpsJob mlOpsJob) {
        String groundTruthUrl = buildGroundTruthUrl(mlOpsJob.getPackageNvr());
        params.add(createParam(PARAM_INPUT_REPORT_FILE_PATH, groundTruthUrl));
        params.add(createParam(PARAM_INPUT_REPORT_CONTENT, ""));
    }

    private String buildGroundTruthUrl(String packageNvr) {
        return String.format(
                "http://minio-api-minio.apps.appeng.clusters.se-apps.redhat.com/test/ground_truth_sheets/%s.xlsx",
                packageNvr);
    }

    private void addMlOpsEvaluationParameter(List<Param> result, MlOpsJob mlOpsJob) {
        String evaluateSpecificNode = getEvaluateSpecificNodeValue(mlOpsJob);
        result.add(createParam(PARAM_EVALUATE_SPECIFIC_NODE, evaluateSpecificNode));
    }

    private String getEvaluateSpecificNodeValue(MlOpsJob mlOpsJob) {
        var settings = mlOpsJob.getMlOpsJobSettings();
        if (settings != null && settings.getEvaluateSpecificNode() != null) {
            return settings.getEvaluateSpecificNode();
        }
        return "all";
    }

    private void addS3StorageParameters(List<Param> result) {
        result.add(createParam(PARAM_S3_ENDPOINT_URL, s3EndpointUrl.orElse("")));
        result.add(createParam(PARAM_S3_BUCKET_NAME, s3BucketName.orElse("")));
    }

    private void addLlmParameters(List<Param> result, MlOpsJob mlOpsJob) {
        var settings = mlOpsJob.getMlOpsJobSettings();
        String llmSecretName = getLlmSecretNameForMlOps(settings);
        LlmSecretValues secrets = getLlmSecretValues(llmSecretName);

        addMainLlmParameters(result, settings, secrets);
        addEmbeddingsLlmParameters(result, settings, secrets);
    }

    private String getLlmSecretNameForMlOps(MlOpsJobSettings settings) {
        if (settings != null && IS_NOT_NULL_AND_NOT_BLANK.test(settings.getSecretName())) {
            return settings.getSecretName();
        }
        return ApplicationConstants.DEFAULT_SECRET_NAME;
    }

    private void addMainLlmParameters(List<Param> result, MlOpsJobSettings settings, LlmSecretValues secrets) {
        result.add(createParam(PARAM_LLM_URL, secrets.llmUrl()));
        result.add(createParam(PARAM_LLM_API_KEY, secrets.llmApiKey()));

        String modelName = getModelNameWithFallback(getLlmModelName(settings), secrets.llmModelName());
        result.add(createParam(PARAM_LLM_MODEL_NAME, modelName));

        String apiType = getLlmApiTypeWithFallback(getLlmApiType(settings), secrets.llmApiType());
        result.add(createParam(PARAM_LLM_API_TYPE, apiType));
    }

    private void addEmbeddingsLlmParameters(
            List<Param> result, MlOpsJobSettings mlOpsJobSettings, LlmSecretValues llmSecretValues) {
        result.add(createParam(PARAM_EMBEDDINGS_LLM_URL, llmSecretValues.embeddingsUrl()));
        result.add(createParam(PARAM_EMBEDDINGS_LLM_API_KEY, llmSecretValues.embeddingsApiKey()));

        String embeddingsModelName = getModelNameWithFallback(
                getEmbeddingLlmModelName(mlOpsJobSettings), llmSecretValues.embeddingsModelName());
        result.add(createParam(PARAM_EMBEDDINGS_LLM_MODEL_NAME, embeddingsModelName));
    }

    private String getLlmModelName(MlOpsJobSettings settings) {
        return settings != null ? settings.getLlmModelName() : null;
    }

    private String getLlmApiType(MlOpsJobSettings settings) {
        return settings != null ? settings.getLlmApiType() : null;
    }

    private String getEmbeddingLlmModelName(MlOpsJobSettings settings) {
        return settings != null ? settings.getEmbeddingLlmModelName() : null;
    }
}
