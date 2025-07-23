package com.redhat.sast.api.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.platform.KubernetesResourceManager;
import com.redhat.sast.api.platform.PipelineParameterMapper;
import com.redhat.sast.api.platform.PipelineRunWatcher;

import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.v1.*;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PlatformService {

    private static final String PIPELINE_NAME = "sast-ai-workflow-pipeline";

    // Workspace names
    private static final String SHARED_WORKSPACE = "shared-workspace";
    private static final String CACHE_WORKSPACE = "cache-workspace";
    private static final String GITLAB_TOKEN_WORKSPACE = "gitlab-token-ws";
    private static final String LLM_CREDENTIALS_WORKSPACE = "llm-credentials-ws";
    private static final String GOOGLE_SA_JSON_WORKSPACE = "google-sa-json-ws";

    // Secret names
    private static final String GITLAB_TOKEN_SECRET = "gitlab-token-secret";
    private static final String DEFAULT_LLM_SECRET = "sast-ai-default-llm-creds";
    private static final String GOOGLE_SA_SECRET = "google-service-account-secret";

    private static final Logger LOG = Logger.getLogger(PlatformService.class);

    @Inject
    TektonClient tektonClient;

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    JobService jobService;

    @Inject
    KubernetesResourceManager resourceManager;

    @Inject
    PipelineParameterMapper parameterMapper;

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

        // Use try-with-resources to ensure PVC cleanup on any failure
        try (KubernetesResourceManager.PvcResource sharedPvc =
                        resourceManager.createManagedPVC(pipelineRunName + "-shared", sharedWorkspaceSize);
                KubernetesResourceManager.PvcResource cachePvc =
                        resourceManager.createManagedPVC(pipelineRunName + "-cache", cacheWorkspaceSize)) {

            List<Param> pipelineParams = parameterMapper.extractPipelineParams(job);
            String llmSecretName =
                    (job.getJobSettings() != null) ? job.getJobSettings().getSecretName() : "sast-ai-default-llm-creds";
            PipelineRun pipelineRun = buildPipelineRun(
                    pipelineRunName, pipelineParams, llmSecretName, sharedPvc.getName(), cachePvc.getName());

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
            // At this point, we've successfully set up everything, so we don't want automatic cleanup
            // The watcher's finally block will handle cleanup when the pipeline completes
            managedExecutor.execute(() -> watchPipelineRun(job.getId(), pipelineRunName));

            // Mark PVCs as managed by the watcher now - disable auto-cleanup
            sharedPvc.disableAutoCleanup();
            cachePvc.disableAutoCleanup();

        } catch (Exception e) {
            LOG.errorf(e, "Failed to create PipelineRun %s in namespace %s", pipelineRunName, namespace);
            // PVCs will be automatically cleaned up by try-with-resources
            throw new IllegalStateException("Failed to start Tekton pipeline", e);
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
                resourceManager.deletePipelineRun(pipelineRunName);
            } else {
                LOG.infof("Keeping PipelineRun %s for debugging (cleanup disabled)", pipelineRunName);
            }

            resourceManager.cleanupPipelineRunPVCs(pipelineRunName);
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

    /**
     * Builds a Tekton PipelineRun with the specified configuration.
     */
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
                .withWorkspaces(buildWorkspaceBindings(llmSecretName, sharedPvcName, cachePvcName))
                .withParams(params)
                .endSpec()
                .build();
    }

    /**
     * Creates all workspace bindings for the pipeline.
     * Uses helper methods for better readability and maintainability.
     */
    private WorkspaceBinding[] buildWorkspaceBindings(String llmSecretName, String sharedPvcName, String cachePvcName) {

        return new WorkspaceBinding[] {
            createPvcWorkspace(SHARED_WORKSPACE, sharedPvcName),
            createPvcWorkspace(CACHE_WORKSPACE, cachePvcName),
            createSecretWorkspace(GITLAB_TOKEN_WORKSPACE, GITLAB_TOKEN_SECRET),
            createSecretWorkspace(
                    LLM_CREDENTIALS_WORKSPACE, Objects.requireNonNullElse(llmSecretName, DEFAULT_LLM_SECRET)),
            createSecretWorkspace(GOOGLE_SA_JSON_WORKSPACE, GOOGLE_SA_SECRET)
        };
    }

    /**
     * Creates a workspace binding for PersistentVolumeClaim.
     *
     * @param workspaceName the name of the workspace
     * @param pvcName the name of the PVC
     * @return configured WorkspaceBinding
     */
    private WorkspaceBinding createPvcWorkspace(String workspaceName, String pvcName) {
        return new WorkspaceBindingBuilder()
                .withName(workspaceName)
                .withNewPersistentVolumeClaim(pvcName, false)
                .build();
    }

    /**
     * Creates a workspace binding for Kubernetes Secret.
     *
     * @param workspaceName the name of the workspace
     * @param secretName the name of the secret
     * @return configured WorkspaceBinding
     */
    private WorkspaceBinding createSecretWorkspace(String workspaceName, String secretName) {
        return new WorkspaceBindingBuilder()
                .withName(workspaceName)
                .withSecret(new SecretVolumeSourceBuilder()
                        .withSecretName(secretName)
                        .build())
                .build();
    }

    /**
     * Cancels a running pipeline for the given job.
     *
     * @param job the job whose pipeline should be cancelled
     * @return true if cancellation was successful, false if pipeline was already completed/failed
     */
    public boolean cancelWorkflow(@Nonnull Job job) {
        String pipelineRunName = resourceManager.extractPipelineRunName(job.getTektonUrl());
        if (pipelineRunName == null) {
            LOG.warnf("Cannot cancel job %d: no pipeline run name found", job.getId());
            return false;
        }

        LOG.infof("Cancelling PipelineRun: %s for job ID: %d", pipelineRunName, job.getId());

        try {
            // Check if pipeline still exists and is running
            PipelineRun pipelineRun = resourceManager.getPipelineRun(pipelineRunName);

            if (pipelineRun == null) {
                LOG.warnf("PipelineRun %s not found - may have already completed", pipelineRunName);
                return false;
            }

            // Check if pipeline is still running
            if (resourceManager.isPipelineInFinalState(pipelineRun)) {
                LOG.infof("PipelineRun %s already completed - cannot cancel", pipelineRunName);
                return false;
            }

            resourceManager.deletePipelineRun(pipelineRunName);

            managedExecutor.execute(() -> resourceManager.cleanupPipelineRunPVCs(pipelineRunName));

            return true;

        } catch (Exception e) {
            LOG.errorf(e, "Error cancelling PipelineRun %s for job ID: %d", pipelineRunName, job.getId());
            return false;
        }
    }
}
