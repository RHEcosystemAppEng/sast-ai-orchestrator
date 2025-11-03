package com.redhat.sast.api.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;

import com.redhat.sast.api.platform.KubernetesResourceManager;
import com.redhat.sast.api.platform.PipelineRunWatcher;

import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.v1.*;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class PlatformService {


    // Workspace names
    private static final String GITLAB_TOKEN_WORKSPACE = "gitlab-token-ws";
    private static final String LLM_CREDENTIALS_WORKSPACE = "llm-credentials-ws";
    private static final String GOOGLE_SA_JSON_WORKSPACE = "google-sa-json-ws";

    // Secret names
    private static final String GITLAB_TOKEN_SECRET = "sast-ai-gitlab-token";
    private static final String DEFAULT_LLM_SECRET = "sast-ai-default-llm-creds";
    private static final String GOOGLE_SA_SECRET = "sast-ai-google-service-account";

    private final TektonClient tektonClient;
    private final ManagedExecutor managedExecutor;
    private final JobService jobService;
    private final KubernetesResourceManager resourceManager;
    private final DvcMetadataService dvcMetadataService;
    private final DataArtifactService dataArtifactService;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    @ConfigProperty(name = "quarkus.profile", defaultValue = "prod")
    String profile;

    /**
     * Starts a Tekton pipeline workflow.
     *
     * @param jobId the job ID
     * @param pipelineName the Tekton pipeline name (e.g., "sast-ai-workflow-pipeline")
     * @param pipelineParams the pipeline parameters
     * @param llmSecretName the LLM secret name
     */
    public void startPipeline(
            @Nonnull Long jobId,
            @Nonnull String pipelineName,
            @Nonnull List<Param> pipelineParams,
            String llmSecretName) {
        // Skip Kubernetes operations in test mode
        if ("test".equals(profile)) {
            LOGGER.info("TEST MODE: Skipping Kubernetes operations for job {}", jobId);
            return;
        }
        String pipelineRunName = pipelineName + "-" + UUID.randomUUID().toString().substring(0, 5);
        LOGGER.info(
                "Initiating PipelineRun: {} for Pipeline: {} in namespace: {}",
                pipelineRunName,
                pipelineName,
                namespace);

        PipelineRun createdPipelineRun;
        try {
            PipelineRun pipelineRun = buildPipelineRun(pipelineRunName, pipelineName, pipelineParams, llmSecretName);
            createdPipelineRun = tektonClient
                    .v1()
                    .pipelineRuns()
                    .inNamespace(namespace)
                    .resource(pipelineRun)
                    .create();
            LOGGER.info("Successfully created PipelineRun: {}", pipelineRunName);
        } catch (Exception e) {
            LOGGER.error("Failed to create PipelineRun {} in namespace {}", pipelineRunName, namespace, e);
            throw new IllegalStateException("Failed to start Tekton pipeline", e);
        }

        String tektonUrl = buildTektonUrlFromClient(createdPipelineRun);
        updateJobTektonUrl(jobId, tektonUrl);

        // Start monitoring the pipeline in a background thread
        // At this point, we've successfully set up everything
        managedExecutor.execute(() -> watchPipelineRun(jobId, pipelineRunName));
    }

    private void watchPipelineRun(@Nonnull Long jobId, @Nonnull String pipelineRunName) {
        LOGGER.info("Starting watcher for PipelineRun: {}", pipelineRunName);
        final CompletableFuture<Void> future = new CompletableFuture<>();
        try (var ignoredWatch = tektonClient
                .v1()
                .pipelineRuns()
                .inNamespace(namespace)
                .withName(pipelineRunName)
                .watch(new PipelineRunWatcher(
                        pipelineRunName, jobId, future, jobService, dvcMetadataService, dataArtifactService))) {
            future.join();
            LOGGER.info("Watcher for PipelineRun {} is closing.", pipelineRunName);
        } catch (Exception e) {
            LOGGER.error("Watcher for {} failed.", pipelineRunName, e);
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

            return pipelineRunUrl;
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to construct Tekton URL for PipelineRun: {}",
                    pipelineRun.getMetadata().getName(),
                    e);
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
            LOGGER.error("Failed to update Tekton URL for job ID: {}", jobId, e);
        }
    }

    /**
     * Builds a Tekton PipelineRun with the specified configuration.
     */
    private PipelineRun buildPipelineRun(
            @Nonnull String pipelineRunName,
            @Nonnull String pipelineName,
            @Nonnull List<Param> params,
            String llmSecretName) {

        return new PipelineRunBuilder()
                .withNewMetadata()
                .withName(pipelineRunName)
                .withNamespace(namespace)
                .endMetadata()
                .withNewSpec()
                .withNewPipelineRef()
                .withName(pipelineName)
                .endPipelineRef()
                .withWorkspaces(buildWorkspaceBindings(llmSecretName))
                .withParams(params)
                .endSpec()
                .build();
    }

    /**
     * Creates all workspace bindings for the pipeline.
     * Uses helper methods for better readability and maintainability.
     */
    private WorkspaceBinding[] buildWorkspaceBindings(String llmSecretName) {

        return new WorkspaceBinding[] {
            createSecretWorkspace(GITLAB_TOKEN_WORKSPACE, GITLAB_TOKEN_SECRET),
            createSecretWorkspace(
                    LLM_CREDENTIALS_WORKSPACE, Objects.requireNonNullElse(llmSecretName, DEFAULT_LLM_SECRET)),
            createSecretWorkspace(GOOGLE_SA_JSON_WORKSPACE, GOOGLE_SA_SECRET)
        };
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
     * Uses proper Tekton cancellation to preserve execution history.
     *
     * @param job the job whose pipeline should be cancelled
     * @return true if cancellation was successful, false if pipeline was already completed/failed
     */
    public boolean cancelTektonPipelineRun(@Nonnull String tektonUrl, @Nonnull Long jobId) {
        // Skip Kubernetes operations in test mode
        if ("test".equals(profile)) {
            LOGGER.info("TEST MODE: Skipping pipeline cancellation for job {}", jobId);
            return true;
        }

        String pipelineRunName = resourceManager.extractPipelineRunName(tektonUrl);
        if (pipelineRunName == null) {
            LOGGER.warn("Cannot cancel job {}: no pipeline run name found", jobId);
            return false;
        }

        try {
            LOGGER.info("Attempting to cancel PipelineRun {} for job ID: {}", pipelineRunName, jobId);
            return resourceManager.cancelPipelineRun(pipelineRunName);

        } catch (Exception e) {
            LOGGER.error("Error cancelling PipelineRun {} for job ID: {}", pipelineRunName, jobId, e);
            return false;
        }
    }
}
