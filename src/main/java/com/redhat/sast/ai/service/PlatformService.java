package com.redhat.sast.ai.service;

import com.redhat.sast.ai.dto.WorkflowParamsDto;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.v1.Pipeline;
import io.fabric8.tekton.v1.Task;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@ApplicationScoped
public class PlatformService {
    @Inject
    TektonClient tektonClient;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    private static final Logger LOG = Logger.getLogger(PlatformService.class);

    public void startSastAIWorkflow(WorkflowParamsDto params){
        Pipeline sastAIPipeline = tektonClient.v1().pipelines().inNamespace(namespace).withName("hello-goodbye").item();
        LOG.info(sastAIPipeline.getMetadata().getName() + " pipeline available in " + namespace);
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
