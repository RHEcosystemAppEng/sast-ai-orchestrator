package com.redhat.sast.api.service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;

import com.redhat.sast.api.platform.KubernetesResourceManager;
import com.redhat.sast.api.platform.MlOpsPipelineRunWatcher;
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

    private static final String PIPELINE_NAME = "sast-ai-workflow-pipeline";

    // Workspace names
    private static final String GITLAB_TOKEN_WORKSPACE = "gitlab-token-ws";
    private static final String LLM_CREDENTIALS_WORKSPACE = "llm-credentials-ws";
    private static final String GOOGLE_SA_JSON_WORKSPACE = "google-sa-json-ws";
    private static final String GCS_SA_JSON_WORKSPACE = "gcs-sa-json-ws";

    // Secret names
    private static final String GITLAB_TOKEN_SECRET = "sast-ai-gitlab-token";
    private static final String DEFAULT_LLM_SECRET = "sast-ai-default-llm-creds";
    private static final String GOOGLE_SA_SECRET = "sast-ai-google-service-account";
    private static final String GCS_SA_SECRET = "sast-ai-gcs-service-account";

    private final TektonClient tektonClient;
    private final ManagedExecutor managedExecutor;
    private final JobService jobService;
    private final MlOpsJobService mlOpsJobService;
    private final MlOpsJobSettingsService mlOpsJobSettingsService;
    private final MlOpsMetricsService mlOpsMetricsService;
    private final MlOpsTokenMetricsService mlOpsTokenMetricsService;
    private final MlOpsExcelReportService mlOpsExcelReportService;
    private final MlOpsNodeFilterEvalService mlOpsNodeFilterEvalService;
    private final MlOpsNodeJudgeEvalService mlOpsNodeJudgeEvalService;
    private final MlOpsNodeSummaryEvalService mlOpsNodeSummaryEvalService;
    private final KubernetesResourceManager resourceManager;
    private final DvcMetadataService dvcMetadataService;
    private final DataArtifactService dataArtifactService;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    @ConfigProperty(name = "quarkus.profile", defaultValue = "prod")
    String profile;

    @ConfigProperty(name = "openshift.console.base.url")
    String openshiftConsoleBaseUrl;

    public void startSastAIWorkflow(@Nonnull Long jobId, @Nonnull List<Param> pipelineParams, String llmSecretName) {
        // Skip Kubernetes operations in test mode
        if ("test".equals(profile)) {
            LOGGER.info("TEST MODE: Skipping Kubernetes operations for job {}", jobId);
            return;
        }
        String pipelineRunName =
                PIPELINE_NAME + "-" + UUID.randomUUID().toString().substring(0, 5);
        LOGGER.info(
                "Initiating PipelineRun: {} for Pipeline: {} in namespace: {}",
                pipelineRunName,
                PIPELINE_NAME,
                namespace);

        PipelineRun createdPipelineRun;
        try {
            PipelineRun pipelineRun = buildPipelineRun(pipelineRunName, pipelineParams, llmSecretName);
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
            String pipelineRunNamespace = pipelineRun.getMetadata().getNamespace();
            String pipelineRunName = pipelineRun.getMetadata().getName();

            String consoleUrl = openshiftConsoleBaseUrl;
            if (consoleUrl.endsWith("/")) {
                consoleUrl = consoleUrl.substring(0, consoleUrl.length() - 1);
            }

            // Construct OpenShift Console URL for PipelineRun
            // Format: https://console-openshift-console.apps.../k8s/ns/{namespace}/tekton.dev~v1~PipelineRun/{name}/
            String pipelineRunUrl = String.format(
                    "%s/k8s/ns/%s/tekton.dev~v1~PipelineRun/%s/", consoleUrl, pipelineRunNamespace, pipelineRunName);

            return pipelineRunUrl;
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to construct OpenShift Console URL for PipelineRun: {}",
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
            @Nonnull String pipelineRunName, @Nonnull List<Param> params, String llmSecretName) {

        return new PipelineRunBuilder()
                .withNewMetadata()
                .withName(pipelineRunName)
                .withNamespace(namespace)
                .endMetadata()
                .withNewSpec()
                .withNewPipelineRef()
                .withName(PIPELINE_NAME)
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
            createSecretWorkspace(GOOGLE_SA_JSON_WORKSPACE, GOOGLE_SA_SECRET),
            createSecretWorkspace(GCS_SA_JSON_WORKSPACE, GCS_SA_SECRET)
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
     * Starts SAST AI workflow for an MLOps job with simplified status tracking.
     * MLOps jobs use polling-based status checking rather than watchers.
     */
    public void startSastAIWorkflowForMlOpsJob(
            @Nonnull Long batchId,
            @Nonnull Long jobId,
            @Nonnull List<Param> pipelineParams,
            String llmSecretName,
            @Nonnull Object mlOpsBatchService) {
        // Skip Kubernetes operations in test mode
        if ("test".equals(profile)) {
            LOGGER.info("TEST MODE: Skipping Kubernetes operations for MLOps job {}", jobId);
            return;
        }

        String pipelineRunName =
                PIPELINE_NAME + "-mlops-" + UUID.randomUUID().toString().substring(0, 5);
        LOGGER.info(
                "Initiating MLOps PipelineRun: {} for Pipeline: {} in namespace: {}",
                pipelineRunName,
                PIPELINE_NAME,
                namespace);

        PipelineRun createdPipelineRun;
        try {
            PipelineRun pipelineRun = buildPipelineRun(pipelineRunName, pipelineParams, llmSecretName);
            createdPipelineRun = tektonClient
                    .v1()
                    .pipelineRuns()
                    .inNamespace(namespace)
                    .resource(pipelineRun)
                    .create();
            LOGGER.info("Successfully created MLOps PipelineRun: {}", pipelineRunName);
        } catch (Exception e) {
            LOGGER.error("Failed to create MLOps PipelineRun {} in namespace {}", pipelineRunName, namespace, e);
            throw new IllegalStateException("Failed to start Tekton pipeline for MLOps job", e);
        }

        String tektonUrl = buildTektonUrlFromClient(createdPipelineRun);
        mlOpsJobService.updateJobTektonUrl(jobId, tektonUrl);

        // Start monitoring the pipeline for MLOps job
        managedExecutor.execute(() -> watchMlOpsPipelineRun(batchId, jobId, pipelineRunName, mlOpsBatchService));
    }

    private void watchMlOpsPipelineRun(
            @Nonnull Long batchId,
            @Nonnull Long jobId,
            @Nonnull String pipelineRunName,
            @Nonnull Object mlOpsBatchServiceParam) {
        LOGGER.info("Starting watcher for MLOps PipelineRun: {}", pipelineRunName);
        final CompletableFuture<Void> future = new CompletableFuture<>();
        try (var ignoredWatch = tektonClient
                .v1()
                .pipelineRuns()
                .inNamespace(namespace)
                .withName(pipelineRunName)
                .watch(new MlOpsPipelineRunWatcher(
                        pipelineRunName,
                        jobId,
                        batchId,
                        future,
                        mlOpsJobService,
                        mlOpsJobSettingsService,
                        mlOpsMetricsService,
                        mlOpsTokenMetricsService,
                        mlOpsExcelReportService,
                        mlOpsNodeFilterEvalService,
                        mlOpsNodeJudgeEvalService,
                        mlOpsNodeSummaryEvalService,
                        mlOpsBatchServiceParam))) {
            future.join();
        } catch (Exception e) {
            LOGGER.error("Error watching MLOps PipelineRun {} for job ID: {}", pipelineRunName, jobId, e);
            mlOpsJobService.updateJobStatus(jobId, com.redhat.sast.api.enums.JobStatus.FAILED);
            // Update batch counter if service provided
            if (mlOpsBatchServiceParam instanceof com.redhat.sast.api.service.MlOpsBatchService batchSvc) {
                batchSvc.incrementBatchFailedJobs(batchId);
            }
        }
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
