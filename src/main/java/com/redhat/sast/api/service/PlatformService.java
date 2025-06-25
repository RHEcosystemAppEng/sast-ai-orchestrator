package com.redhat.sast.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.platform.LlmSecretValues;
import com.redhat.sast.api.platform.PipelineRunWatcher;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.v1.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PlatformService {

    private static final String PIPELINE_NAME = "sast-ai-workflow-pipeline";

    private static final Logger LOG = Logger.getLogger(PlatformService.class);

    @Inject
    TektonClient tektonClient;

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    JobService jobService;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    public void startSastAIWorkflow(Job job) {
        String pipelineRunName =
                PIPELINE_NAME + "-" + UUID.randomUUID().toString().substring(0, 5);
        LOG.infof(
                "Initiating PipelineRun: %s for Pipeline: %s in namespace: %s",
                pipelineRunName, PIPELINE_NAME, namespace);

        List<Param> pipelineParams = extractPipelineParams(job);
        String llmSecretName =
                (job.getJobSettings() != null) ? job.getJobSettings().getSecretName() : "sast-ai-default-llm-creds";
        PipelineRun pipelineRun = buildPipelineRun(pipelineRunName, pipelineParams, llmSecretName);

        try {
            PipelineRun createdPipelineRun = tektonClient
                    .v1()
                    .pipelineRuns()
                    .inNamespace(namespace)
                    .resource(pipelineRun)
                    .create();
            LOG.infof("Successfully created PipelineRun: %s", pipelineRunName);

            // Set the Tekton URL on the job using cluster information from TektonClient
            String tektonUrl = buildTektonUrlFromClient(createdPipelineRun);
            updateJobTektonUrl(job.getId(), tektonUrl);
            LOG.infof("Updated job %d with Tekton URL: %s", job.getId(), tektonUrl);

            jobService.updateJobStatus(job.getId(), JobStatus.RUNNING);
            managedExecutor.execute(() -> watchPipelineRun(job.getId(), pipelineRunName));
        } catch (Exception e) {
            LOG.errorf(e, "Failed to create PipelineRun %s in namespace %s", pipelineRunName, namespace);
            throw new RuntimeException("Failed to start Tekton pipeline", e);
        }
    }

    private void watchPipelineRun(long jobId, String pipelineRunName) {
        LOG.infof("Starting watcher for PipelineRun: %s", pipelineRunName);
        final CompletableFuture<Void> future = new CompletableFuture<>();
        try (var ignoredWatch = tektonClient
                .v1()
                .pipelineRuns()
                .inNamespace(namespace)
                .withName(pipelineRunName)
                .watch(new PipelineRunWatcher(pipelineRunName, jobId, future, jobService))) {
            future.join();
            LOG.infof("Watcher for PipelineRun %s is closing.", pipelineRunName);
        } catch (Exception e) {
            LOG.errorf(e, "Watcher for %s failed.", pipelineRunName);
        }
    }

    private String buildTektonUrlFromClient(PipelineRun pipelineRun) {
        try {
            // Get the Kubernetes cluster URL from the client
            String masterUrl = tektonClient.getMasterUrl().toString();
            // Remove trailing slash if present
            if (masterUrl.endsWith("/")) {
                masterUrl = masterUrl.substring(0, masterUrl.length() - 1);
            }

            // Construct the Kubernetes API URL for the PipelineRun resource
            String pipelineRunUrl = String.format(
                    "%s/apis/tekton.dev/v1/namespaces/%s/pipelineruns/%s",
                    masterUrl,
                    pipelineRun.getMetadata().getNamespace(),
                    pipelineRun.getMetadata().getName());

            LOG.infof("Constructed Tekton URL: %s", pipelineRunUrl);
            return pipelineRunUrl;
        } catch (Exception e) {
            LOG.errorf(
                    e,
                    "Failed to construct Tekton URL for PipelineRun: %s",
                    pipelineRun.getMetadata().getName());
            // Return a fallback URL if construction fails
            return String.format(
                    "tekton://namespaces/%s/pipelineruns/%s",
                    pipelineRun.getMetadata().getNamespace(),
                    pipelineRun.getMetadata().getName());
        }
    }

    @Transactional
    public void updateJobTektonUrl(Long jobId, String tektonUrl) {
        try {
            jobService.updateJobTektonUrl(jobId, tektonUrl);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to update Tekton URL for job ID: %d", jobId);
        }
    }

    private LlmSecretValues getLlmSecretValues(String secretName) {
        try {
            if (secretName == null || secretName.trim().isEmpty()) {
                LOG.warnf("Secret name is null or empty");
                return LlmSecretValues.empty();
            }

            // Use the underlying Kubernetes client from TektonClient to access secrets
            Secret secret = tektonClient
                    .adapt(io.fabric8.kubernetes.client.KubernetesClient.class)
                    .secrets()
                    .inNamespace(namespace)
                    .withName(secretName)
                    .get();
            if (secret == null) {
                LOG.warnf("Secret '%s' not found in namespace '%s'", secretName, namespace);
                return LlmSecretValues.empty();
            }

            if (secret.getData() == null) {
                LOG.warnf("Secret '%s' has no data", secretName);
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
            LOG.errorf(e, "Failed to read secret '%s' from namespace '%s'", secretName, namespace);
            return LlmSecretValues.empty();
        }
    }

    private String getDecodedSecretValue(Secret secret, String key) {
        try {
            if (!secret.getData().containsKey(key)) {
                LOG.debugf(
                        "Secret '%s' does not contain key '%s'",
                        secret.getMetadata().getName(), key);
                return "";
            }

            String encodedValue = secret.getData().get(key);
            return new String(java.util.Base64.getDecoder().decode(encodedValue));
        } catch (Exception e) {
            LOG.warnf(e, "Failed to decode secret value for key '%s'", key);
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

    private List<Param> extractPipelineParams(Job job) {
        List<Param> params = new ArrayList<>();

        // Basic job parameters
        params.add(new ParamBuilder()
                .withName("REPO_REMOTE_URL")
                .withNewValue(job.getPackageSourceCodeUrl())
                .build());
        params.add(new ParamBuilder()
                .withName("FALSE_POSITIVES_URL")
                .withNewValue(job.getKnownFalsePositivesUrl())
                .build());
        params.add(new ParamBuilder()
                .withName("INPUT_REPORT_FILE_PATH")
                .withNewValue(job.getInputSourceUrl())
                .build());
        params.add(new ParamBuilder()
                .withName("PROJECT_NAME")
                .withNewValue(job.getProjectName())
                .build());
        params.add(new ParamBuilder()
                .withName("PROJECT_VERSION")
                .withNewValue(job.getProjectVersion())
                .build());

        // LLM settings from JobSettings and OCP secrets
        if (job.getJobSettings() != null) {
            String secretName = job.getJobSettings().getSecretName();

            // Read all LLM configuration from OCP secret in one call
            LlmSecretValues llmSecretValues = getLlmSecretValues(secretName);

            // Add LLM parameters (URLs and API keys always from secret)
            params.add(new ParamBuilder()
                    .withName("LLM_URL")
                    .withNewValue(llmSecretValues.llmUrl())
                    .build());
            params.add(new ParamBuilder()
                    .withName("LLM_MODEL_NAME")
                    .withNewValue(getModelNameWithFallback(
                            job.getJobSettings().getLlmModelName(), llmSecretValues.llmModelName()))
                    .build());
            params.add(new ParamBuilder()
                    .withName("LLM_API_KEY")
                    .withNewValue(llmSecretValues.llmApiKey())
                    .build());

            // Add embeddings parameters (URLs and API keys always from secret)
            params.add(new ParamBuilder()
                    .withName("EMBEDDINGS_LLM_URL")
                    .withNewValue(llmSecretValues.embeddingsUrl())
                    .build());
            params.add(new ParamBuilder()
                    .withName("EMBEDDINGS_LLM_MODEL_NAME")
                    .withNewValue(getModelNameWithFallback(
                            job.getJobSettings().getEmbeddingLlmModelName(), llmSecretValues.embeddingsModelName()))
                    .build());
            params.add(new ParamBuilder()
                    .withName("EMBEDDINGS_LLM_API_KEY")
                    .withNewValue(llmSecretValues.embeddingsApiKey())
                    .build());
        } else {
            // Add default empty values if no job settings
            params.add(new ParamBuilder().withName("LLM_URL").withNewValue("").build());
            params.add(new ParamBuilder()
                    .withName("LLM_MODEL_NAME")
                    .withNewValue("")
                    .build());
            params.add(
                    new ParamBuilder().withName("LLM_API_KEY").withNewValue("").build());
            params.add(new ParamBuilder()
                    .withName("EMBEDDINGS_LLM_URL")
                    .withNewValue("")
                    .build());
            params.add(new ParamBuilder()
                    .withName("EMBEDDINGS_LLM_MODEL_NAME")
                    .withNewValue("")
                    .build());
            params.add(new ParamBuilder()
                    .withName("EMBEDDINGS_LLM_API_KEY")
                    .withNewValue("")
                    .build());
        }

        return params;
    }

    private PipelineRun buildPipelineRun(String pipelineRunName, List<Param> params, String llmSecretName) {
        return new PipelineRunBuilder()
                .withNewMetadata()
                .withName(pipelineRunName)
                .withNamespace(namespace)
                .endMetadata()
                .withNewSpec()
                .withNewPipelineRef()
                .withName(PIPELINE_NAME)
                .endPipelineRef()
                .withWorkspaces(
                        new WorkspaceBindingBuilder()
                                .withName("shared-workspace")
                                .withNewPersistentVolumeClaim("sast-ai-workflow-pvc", false)
                                .build(),
                        new WorkspaceBindingBuilder()
                                .withName("cache-workspace")
                                .withNewPersistentVolumeClaim("sast-ai-cache-pvc", false)
                                .build(),
                        new WorkspaceBindingBuilder()
                                .withName("gitlab-token-ws")
                                .withSecret(new SecretVolumeSourceBuilder()
                                        .withSecretName("gitlab-token-secret")
                                        .build())
                                .build(),
                        new WorkspaceBindingBuilder()
                                .withName("llm-credentials-ws")
                                .withSecret(new SecretVolumeSourceBuilder()
                                        .withSecretName(
                                                llmSecretName != null ? llmSecretName : "sast-ai-default-llm-creds")
                                        .build())
                                .build(),
                        new WorkspaceBindingBuilder()
                                .withName("google-sa-json-ws")
                                .withSecret(new SecretVolumeSourceBuilder()
                                        .withSecretName("google-service-account-secret")
                                        .build())
                                .build())
                .withParams(params)
                .endSpec()
                .build();
    }
}
