package com.redhat.sast.api.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.platform.LlmSecretValues;
import com.redhat.sast.api.platform.PipelineRunWatcher;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.v1.*;
import jakarta.annotation.Nonnull;
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

    @ConfigProperty(name = "sast.ai.workspace.shared.size", defaultValue = "20Gi")
    String sharedWorkspaceSize;

    @ConfigProperty(name = "sast.ai.workspace.cache.size", defaultValue = "10Gi")
    String cacheWorkspaceSize;

    @ConfigProperty(name = "sast.ai.cleanup.completed.pipelineruns", defaultValue = "true")
    boolean cleanupCompletedPipelineRuns;

    public void startSastAIWorkflow(@Nonnull Job job) {
        String pipelineRunName =
                PIPELINE_NAME + "-" + UUID.randomUUID().toString().substring(0, 5);
        LOG.infof(
                "Initiating PipelineRun: %s for Pipeline: %s in namespace: %s",
                pipelineRunName, PIPELINE_NAME, namespace);

        String sharedPvcName = createDedicatedPVC(pipelineRunName + "-shared", sharedWorkspaceSize);
        String cachePvcName = createDedicatedPVC(pipelineRunName + "-cache", cacheWorkspaceSize);

        List<Param> pipelineParams = extractPipelineParams(job);
        String llmSecretName =
                (job.getJobSettings() != null) ? job.getJobSettings().getSecretName() : "sast-ai-default-llm-creds";
        PipelineRun pipelineRun =
                buildPipelineRun(pipelineRunName, pipelineParams, llmSecretName, sharedPvcName, cachePvcName);

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

            // Start monitoring the pipeline in a background thread
            managedExecutor.execute(() -> watchPipelineRun(job.getId(), pipelineRunName));
        } catch (Exception e) {
            LOG.errorf(e, "Failed to create PipelineRun %s in namespace %s", pipelineRunName, namespace);
            cleanupPVC(sharedPvcName);
            cleanupPVC(cachePvcName);
            throw new IllegalStateException("Failed to start Tekton pipeline", e);
        }
    }

    private String createDedicatedPVC(String pvcName, String size) {
        try {
            KubernetesClient k8sClient = tektonClient.adapt(KubernetesClient.class);

            PersistentVolumeClaim pvc = new PersistentVolumeClaimBuilder()
                    .withNewMetadata()
                    .withName(pvcName)
                    .withNamespace(namespace)
                    .endMetadata()
                    .withNewSpec()
                    .withAccessModes("ReadWriteOnce")
                    .withNewResources()
                    .addToRequests("storage", new Quantity(size))
                    .endResources()
                    .endSpec()
                    .build();

            PersistentVolumeClaim createdPvc = k8sClient
                    .persistentVolumeClaims()
                    .inNamespace(namespace)
                    .resource(pvc)
                    .create();

            LOG.infof("Created dedicated PVC: %s with size: %s", pvcName, size);
            return createdPvc.getMetadata().getName();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to create PVC: %s", pvcName);
            throw new IllegalStateException("Failed to create dedicated PVC", e);
        }
    }

    private void cleanupPVC(String pvcName) {
        try {
            KubernetesClient k8sClient = tektonClient.adapt(KubernetesClient.class);

            boolean deleted = !k8sClient
                    .persistentVolumeClaims()
                    .inNamespace(namespace)
                    .withName(pvcName)
                    .delete()
                    .isEmpty();

            if (deleted) {
                LOG.infof("Successfully deleted PVC: %s", pvcName);
            } else {
                LOG.warnf("PVC %s was not found or already deleted", pvcName);
            }
        } catch (Exception e) {
            LOG.errorf(e, "Failed to delete PVC: %s", pvcName);
        }
    }

    private void cleanupPipelineRun(String pipelineRunName) {
        try {
            boolean deleted = !tektonClient
                    .v1()
                    .pipelineRuns()
                    .inNamespace(namespace)
                    .withName(pipelineRunName)
                    .delete()
                    .isEmpty();

            if (deleted) {
                LOG.infof("Successfully deleted PipelineRun: %s", pipelineRunName);
                // Wait a moment for pods to be cleaned up
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.warnf("Cleanup sleep interrupted for PipelineRun: %s", pipelineRunName);
                }
            } else {
                LOG.warnf("PipelineRun %s was not found or already deleted", pipelineRunName);
            }
        } catch (Exception e) {
            LOG.errorf(e, "Failed to delete PipelineRun: %s", pipelineRunName);
        }
    }

    private void watchPipelineRun(@Nonnull Long jobId, @Nonnull String pipelineRunName) {
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
        } finally {
            LOG.infof("Cleaning up resources for PipelineRun: %s", pipelineRunName);

            if (cleanupCompletedPipelineRuns) {
                cleanupPipelineRun(pipelineRunName);
            } else {
                LOG.infof("Keeping PipelineRun %s for debugging (cleanup disabled)", pipelineRunName);
            }

            cleanupPVC(pipelineRunName + "-shared");
            cleanupPVC(pipelineRunName + "-cache");
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

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void updateJobTektonUrl(@Nonnull Long jobId, @Nonnull String tektonUrl) {
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
            return new String(java.util.Base64.getDecoder().decode(encodedValue), StandardCharsets.UTF_8);
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

    private List<Param> extractPipelineParams(@Nonnull Job job) {
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
                .withNewValue(job.getgSheetUrl())
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
            LOG.infof("Job %d has JobSettings with secretName: '%s'", job.getId(), secretName);

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
            LOG.warnf("Job %d has NO JobSettings - using empty LLM values", job.getId());
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

    private PipelineRun buildPipelineRun(
            @Nonnull String pipelineRunName,
            @Nonnull List<Param> params,
            String llmSecretName,
            @Nonnull String sharedPvcName,
            @Nonnull String cachePvcName) {
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
                                .withNewPersistentVolumeClaim(sharedPvcName, false)
                                .build(),
                        new WorkspaceBindingBuilder()
                                .withName("cache-workspace")
                                .withNewPersistentVolumeClaim(cachePvcName, false)
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
