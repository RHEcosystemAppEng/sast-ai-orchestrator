package com.redhat.sast.ai.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import com.redhat.sast.ai.dto.WorkflowParamsDto;
import com.redhat.sast.ai.utils.WorkflowStatus;

import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.knative.pkg.apis.Condition;
import io.fabric8.kubernetes.client.Watcher; 
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.v1.Pipeline;
import io.fabric8.tekton.v1.Task;
import io.fabric8.tekton.v1.PipelineRun;
import io.fabric8.tekton.v1.PipelineRunBuilder;
import io.fabric8.tekton.v1.WorkspaceBindingBuilder;
import io.fabric8.tekton.v1.Param;
import io.fabric8.tekton.v1.ParamBuilder;


@ApplicationScoped
public class PlatformService {

    private static final String PIPELINE_NAME = "sast-ai-workflow-pipeline";
    private static final String TEKTON_RESOURCES_DIR = "tekton";
    private static final String KIND_FIELD = "kind:";
    private static final String KIND_TASK = "Task";
    private static final String KIND_PIPELINE = "Pipeline";
    private static final String SUCCEEDED_CONDITION = "Succeeded";
    private static final String STATUS_TRUE = "True";
    private static final String STATUS_FALSE = "False";

    private static final Logger LOG = Logger.getLogger(PlatformService.class);

    @Inject
    TektonClient tektonClient;

    @Inject 
    ManagedExecutor managedExecutor;

    @Inject
    DataService dataService;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    public void startSastAIWorkflow(long workflowId, WorkflowParamsDto params){
        String pipelineRunName = PIPELINE_NAME + "-" + UUID.randomUUID().toString().substring(0, 5);
        LOG.infof("Initiating PipelineRun: %s for Pipeline: %s in namespace: %s", pipelineRunName, PIPELINE_NAME, namespace);

        List<Param> pipelineParams = buildPipelineParams(params);
        PipelineRun pipelineRun = buildPipelineRun(pipelineRunName, pipelineParams);

        try {
            tektonClient.v1().pipelineRuns().inNamespace(namespace).resource(pipelineRun).create();
            LOG.infof("Successfully created PipelineRun: %s", pipelineRunName);

            dataService.updateWorkflowStatus(workflowId, WorkflowStatus.RUNNING);
            managedExecutor.execute(() -> watchPipelineRun(workflowId, pipelineRunName));
        } catch (Exception e) {
            LOG.errorf(e, "Failed to create PipelineRun %s in namespace %s", pipelineRunName, namespace);
            throw new RuntimeException("Failed to start Tekton pipeline", e);
        }
    }

    private void watchPipelineRun(long workflowId, String pipelineRunName) {
        LOG.infof("Starting watcher for PipelineRun: %s", pipelineRunName);
        final CompletableFuture<Void> future = new CompletableFuture<>();
        try (io.fabric8.kubernetes.client.Watch watch = tektonClient.v1().pipelineRuns().inNamespace(namespace).withName(pipelineRunName).watch(new Watcher<>() {
            @Override
            public void eventReceived(Action action, PipelineRun resource) {
                LOG.infof("Watcher event: %s for PipelineRun: %s", action, resource.getMetadata().getName());
                if (resource.getStatus() != null && resource.getStatus().getConditions() != null) {
                    for (Condition condition : resource.getStatus().getConditions()) {
                        if (SUCCEEDED_CONDITION.equals(condition.getType())) {
                            if (STATUS_TRUE.equalsIgnoreCase(condition.getStatus())) {
                                LOG.infof("PipelineRun %s succeeded.", pipelineRunName);
                                dataService.updateWorkflowStatus(workflowId, WorkflowStatus.COMPLETED);
                                future.complete(null);
                                return;
                            } else if (STATUS_FALSE.equalsIgnoreCase(condition.getStatus())) {
                                LOG.errorf("PipelineRun %s failed. Reason: %s, Message: %s", pipelineRunName, condition.getReason(), condition.getMessage());
                                dataService.updateWorkflowStatus(workflowId, "FAILED");
                                future.complete(null);
                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public void onClose(WatcherException cause) {
                if (cause != null) {
                    LOG.errorf(cause, "Watcher for %s closed due to an error.", pipelineRunName);
                    future.completeExceptionally(cause);
                } else {
                    LOG.warnf("Watcher for %s closed cleanly without a final status.", pipelineRunName);
                    future.complete(null);
                }
            }
        })) {
            future.join();
            LOG.infof("Watcher for PipelineRun %s is closing.", pipelineRunName);
        } catch (Exception e) {
            LOG.errorf(e, "Watcher for %s failed.", pipelineRunName);
        }
    }

    @PostConstruct
    public void init() {
        var classLoader = Thread.currentThread().getContextClassLoader();
        var tektonFolder = classLoader.getResource(TEKTON_RESOURCES_DIR);

        if (tektonFolder == null) {
            LOG.errorf("No '%s' folder found in src/main/resources.", TEKTON_RESOURCES_DIR);
            return;
        }

        try {
            Path tektonBasePath = Paths.get(tektonFolder.toURI());
            try (var files = Files.walk(tektonBasePath)) {
                files.filter(Files::isRegularFile).forEach(filePath -> {
                    try {
                        Path relativePath = tektonBasePath.relativize(filePath);
                        String resourcePath = "tekton/" + relativePath.toString().replace('\\', '/');
                        LOG.debugf("Processing resource: %s", resourcePath);

                        String fileContent = new String(Files.readString(filePath));
                        if (!fileContent.contains(KIND_FIELD)) {
                            return;
                        }

                        String kind = fileContent.lines()
                            .filter(line -> line.trim().startsWith(KIND_FIELD))
                            .map(line -> line.split(":", 2)[1].trim())
                            .findFirst()
                            .orElse("Unknown");

                        switch (kind) {
                            case KIND_TASK:
                                applyTask(resourcePath);
                                break;
                            case KIND_PIPELINE:
                                createPipeline(resourcePath);
                                break;
                            default:
                                LOG.debugf("Ignoring resource of kind '%s' from file: %s", kind, resourcePath);
                                break;
                        }

                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                });
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void applyTask(String resourcePath){
        try (var resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            Task task = Serialization.unmarshal(resourceStream, Task.class);
            task.getMetadata().setNamespace(namespace);
            task = tektonClient.v1().tasks().inNamespace(namespace).resource(task).serverSideApply();
            LOG.debug("Tekton task created: " + task.getMetadata().getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createPipeline(String resourcePath){
        try (var resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            Pipeline pipeline = Serialization.unmarshal(resourceStream, Pipeline.class);
            pipeline.getMetadata().setNamespace(namespace);
            pipeline = tektonClient.v1().pipelines().inNamespace(namespace).resource(pipeline).serverSideApply();
            LOG.debug("Tekton pipeline created: " + pipeline.getMetadata().getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private List<Param> buildPipelineParams(WorkflowParamsDto p) {
        List<Param> params = new ArrayList<>();
        params.add(new ParamBuilder().withName("SOURCE_CODE_URL")
                                     .withNewValue(p.getSrcCodePath())
                                     .build());
        params.add(new ParamBuilder().withName("KNOWN_FALSE_POSITIVES_URL")
                                     .withNewValue(p.getKnownFalsePositivesUrl())
                                     .build());
        params.add(new ParamBuilder().withName("INPUT_REPORT_FILE_PATH")
                                     .withNewValue(p.getInputReportFilePath())
                                     .build());
        params.add(new ParamBuilder().withName("PROJECT_NAME")
                                     .withNewValue(p.getProjectName())
                                     .build());
        params.add(new ParamBuilder().withName("PROJECT_VERSION")
                                     .withNewValue(p.getProjectVersion())
                                     .build());
        params.add(new ParamBuilder().withName("LLM_URL")
                                     .withNewValue(p.getLlmUrl())
                                     .build());
        params.add(new ParamBuilder().withName("LLM_MODEL_NAME")
                                     .withNewValue(p.getLlmModelName())
                                     .build());
        params.add(new ParamBuilder().withName("EMBEDDINGS_LLM_URL")
                                     .withNewValue(p.getEmbeddingsLlmUrl())
                                     .build());
        params.add(new ParamBuilder().withName("EMBEDDINGS_LLM_MODEL_NAME")
                                     .withNewValue(p.getEmbeddingsLlmModelName())
                                     .build());
        return params;
    }

    private PipelineRun buildPipelineRun(String pipelineRunName, List<Param> params) {
        return new PipelineRunBuilder().withNewMetadata()
        .withName(pipelineRunName).withNamespace(namespace)
            .endMetadata().withNewSpec().withNewPipelineRef()
        .withName(PIPELINE_NAME).endPipelineRef()
            .withWorkspaces(
                new WorkspaceBindingBuilder()
                    .withName("shared-workspace")
                    .withNewPersistentVolumeClaim("sast-ai-workflow-pvc", false).build(),
                new WorkspaceBindingBuilder()
                    .withName("cache-workspace")
                    .withNewPersistentVolumeClaim("sast-ai-cache-pvc", false).build(),
                new WorkspaceBindingBuilder()
                    .withName("gitlab-token-ws")
                    .withSecret(new SecretVolumeSourceBuilder()
                                    .withSecretName("gitlab-token-secret")
                                    .build()).build(),
                new WorkspaceBindingBuilder()
                    .withName("llm-api-key-ws")
                    .withSecret(new SecretVolumeSourceBuilder()
                                    .withSecretName("llm-api-key-secret")
                                    .build()).build(),
                new WorkspaceBindingBuilder()
                    .withName("embeddings-api-key-ws")
                    .withSecret(new SecretVolumeSourceBuilder()
                                    .withSecretName("embeddings-api-key-secret")
                                    .build()).build(),
                new WorkspaceBindingBuilder()
                    .withName("google-sa-json-ws")
                    .withSecret(new SecretVolumeSourceBuilder()
                                    .withSecretName("google-service-account-secret")
                                    .build()).build()
            )
            .withParams(params)
        .endSpec()
        .build();
    }
}
