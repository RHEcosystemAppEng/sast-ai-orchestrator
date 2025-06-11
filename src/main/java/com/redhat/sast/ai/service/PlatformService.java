package com.redhat.sast.ai.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

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
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PlatformService {
    @Inject
    TektonClient tektonClient;

    @Inject 
    ManagedExecutor managedExecutor;

    @Inject
    DataService dataService;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    private static final Logger LOG = Logger.getLogger(PlatformService.class);

    public void startSastAIWorkflow(long workflowId, WorkflowParamsDto params){
        String pipelineName = "sast-ai-workflow-pipeline"; 
        String pipelineRunName = pipelineName + "-" + UUID.randomUUID().toString().substring(0, 5);
        LOG.infof("Initiating PipelineRun: %s for Pipeline: %s in namespace: %s", pipelineRunName, pipelineName, namespace);

        List<Param> pipelineParams = new ArrayList<>();
        pipelineParams.add(new ParamBuilder().withName("sourceCodeUrl").withNewValue(params.getSrcRepoPath()).build());
        pipelineParams.add(new ParamBuilder().withName("INPUT_REPORT_FILE_PATH").withNewValue(params.getInputReportFilePath()).build());
        pipelineParams.add(new ParamBuilder().withName("falsePositivesUrl").withNewValue(params.getKnownFalsePositiveUrl()).build());
        pipelineParams.add(new ParamBuilder().withName("PROJECT_NAME").withNewValue(params.getProjectName()).build());
        pipelineParams.add(new ParamBuilder().withName("PROJECT_VERSION").withNewValue(params.getProjectVersion()).build());

        pipelineParams.add(new ParamBuilder().withName("LLM_URL").withNewValue(params.getLlmUrl()).build());
        pipelineParams.add(new ParamBuilder().withName("LLM_MODEL_NAME").withNewValue(params.getLlmModelName()).build());
        
        pipelineParams.add(new ParamBuilder().withName("EMBEDDINGS_LLM_URL").withNewValue(params.getEmbeddingsLlmUrl()).build());
        pipelineParams.add(new ParamBuilder().withName("EMBEDDINGS_LLM_MODEL_NAME").withNewValue(params.getEmbeddingsLlmModelName()).build());

        PipelineRun pipelineRun = new PipelineRunBuilder()
                .withNewMetadata()
                    .withName(pipelineRunName)
                    .withNamespace(namespace)
                .endMetadata()
                .withNewSpec()
                    .withNewPipelineRef()
                        .withName(pipelineName)
                    .endPipelineRef()
                    .withWorkspaces(
                            new WorkspaceBindingBuilder().withName("shared-workspace").withNewPersistentVolumeClaim("sast-ai-workflow-pvc", false).build(),
                            new WorkspaceBindingBuilder().withName("cache-workspace").withNewPersistentVolumeClaim("sast-ai-cache-pvc", false).build(),
                            new WorkspaceBindingBuilder().withName("gitlab-token-ws").withSecret(new SecretVolumeSourceBuilder().withSecretName("gitlab-token-secret").build()).build(),
                            new WorkspaceBindingBuilder().withName("llm-api-key-ws").withSecret(new SecretVolumeSourceBuilder().withSecretName("llm-api-key-secret").build()).build(),
                            new WorkspaceBindingBuilder().withName("embeddings-api-key-ws").withSecret(new SecretVolumeSourceBuilder().withSecretName("embeddings-api-key-secret").build()).build(),
                            new WorkspaceBindingBuilder().withName("google-sa-json-ws").withSecret(new SecretVolumeSourceBuilder().withSecretName("google-service-account-secret").build()).build()
                    )
                    .withParams(pipelineParams) // Use the safe list of parameters
                .endSpec()
                .build();

        try {
            tektonClient.v1().pipelineRuns().inNamespace(namespace).resource(pipelineRun).create();
            LOG.infof("Successfully created PipelineRun: %s", pipelineRunName);

            dataService.updateWorkflowStatus(workflowId, WorkflowStatus.RUNNING);
            managedExecutor.execute(() -> watchPipelineRun(workflowId, pipelineRunName));
        } catch (Exception e) {
            LOG.errorf(e, "Failed to create PipelineRun %s in namespace %s", pipelineRunName, namespace);
            throw new RuntimeException("Failed to start Tekton pipeline", e);
        }

        // Pipeline sastAIPipeline = tektonClient.v1().pipelines().inNamespace(namespace).withName("hello-goodbye").item();
        // LOG.info(sastAIPipeline.getMetadata().getName() + " pipeline available in " + namespace);
    }

    private void watchPipelineRun(long workflowId, String pipelineRunName) {
        LOG.infof("Starting watcher for PipelineRun: %s", pipelineRunName);
        final CountDownLatch latch = new CountDownLatch(1);

        try (io.fabric8.kubernetes.client.Watch watch = tektonClient.v1().pipelineRuns().inNamespace(namespace).withName(pipelineRunName).watch(new Watcher<>() {
            @Override
            public void eventReceived(Action action, PipelineRun resource) {
                LOG.infof("Watcher event: %s for PipelineRun: %s", action, resource.getMetadata().getName());
                if (resource.getStatus() != null && resource.getStatus().getConditions() != null) {
                    for (Condition condition : resource.getStatus().getConditions()) {
                        if ("Succeeded".equals(condition.getType())) {
                            if ("True".equalsIgnoreCase(condition.getStatus())) {
                                LOG.infof("PipelineRun %s succeeded.", pipelineRunName);
                                dataService.updateWorkflowStatus(workflowId, WorkflowStatus.COMPLETED);
                                latch.countDown();
                            } else if ("False".equalsIgnoreCase(condition.getStatus())) {
                                LOG.errorf("PipelineRun %s failed. Reason: %s, Message: %s", pipelineRunName, condition.getReason(), condition.getMessage());
                                dataService.updateWorkflowStatus(workflowId, "FAILED");
                                latch.countDown();
                            }
                        }
                    }
                }
            }

            @Override
            public void onClose(WatcherException cause) {
                LOG.errorf(cause, "Watcher for %s closed due to an error.", pipelineRunName);
                latch.countDown();
            }
        })) {
            latch.await();
            LOG.infof("Watcher for PipelineRun %s is closing.", pipelineRunName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Watcher was interrupted.", e);
        }
    }

    @PostConstruct
    public void init() {
        var classLoader = Thread.currentThread().getContextClassLoader();
        var tektonFolder = classLoader.getResource("tekton");

        if (tektonFolder == null) {
            LOG.error("No 'tekton' folder found in src/main/resources.");
            return;
        }

        try (var files = Files.walk(Paths.get(tektonFolder.toURI()))) {
            files
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        try (var reader = new BufferedReader(new InputStreamReader(
                                Files.newInputStream(filePath), StandardCharsets.UTF_8))) {

                            String kind = reader.lines()
                                    .filter(line -> line.trim().startsWith("kind:"))
                                    .map(line -> line.split(":", 2)[1].trim())
                                    .findFirst()
                                    .orElse("Unknown");

                            String resourcePath = "tekton/" + filePath.getFileName().toString();

                            switch (kind) {
                                case "Task":
                                    createTask(resourcePath);
                                    break;
                                case "Pipeline":
                                    createPipeline(resourcePath);
                                    break;
                                default:
                                    LOG.error("Unknown kind in filename: "+ resourcePath);
                                    break;
                            }

                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void createTask(String fileName){
        try (var is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new IOException("Resource not found: " + fileName);
            }

            Task task = Serialization.unmarshal(is, Task.class);
            task.getMetadata().setNamespace(namespace);
            // Create Task
            task = tektonClient.v1().tasks().inNamespace(namespace).resource(task).serverSideApply();
            LOG.debug("Tekton task created: " + task.getMetadata().getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createPipeline(String fileName){
        try (var is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new IOException("Resource not found: " + fileName);
            }

            Pipeline pipeline = Serialization.unmarshal(is, Pipeline.class);
            pipeline.getMetadata().setNamespace(namespace);
            // Create Pipeline
            pipeline = tektonClient.v1().pipelines().inNamespace(namespace).resource(pipeline).serverSideApply();
            LOG.debug("Tekton pipeline created: " + pipeline.getMetadata().getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
