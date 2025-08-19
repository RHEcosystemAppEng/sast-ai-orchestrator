package com.redhat.sast.api.mock;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.tekton.v1.Param;
import io.fabric8.tekton.v1.PipelineRun;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

@Alternative
@Priority(1)
@ApplicationScoped
public class MockPlatformService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockPlatformService.class);

    public CompletableFuture<PipelineRun> createPipelineRun(
            Long jobId, List<Param> pipelineParams, String llmSecretName) {

        LOGGER.debug("Creating mock pipeline run for job {}", jobId);

        PipelineRun mockPipelineRun = createMockPipelineRun(jobId);

        return CompletableFuture.completedFuture(mockPipelineRun);
    }

    public void startPipelineWatcher(PipelineRun pipelineRun, Long jobId) {
        LOGGER.debug("Starting mock pipeline watcher for job {}", jobId);
    }

    public void cleanupCompletedPipelineRuns() {
        LOGGER.debug("Mock cleanup of completed pipeline runs");
    }

    private PipelineRun createMockPipelineRun(Long jobId) {
        PipelineRun pipelineRun = new PipelineRun();

        io.fabric8.kubernetes.api.model.ObjectMeta metadata = new io.fabric8.kubernetes.api.model.ObjectMeta();
        metadata.setName("sast-ai-workflow-pipeline-" + generateMockId());
        metadata.setNamespace("test-namespace");
        pipelineRun.setMetadata(metadata);

        pipelineRun.setSpec(new io.fabric8.tekton.v1.PipelineRunSpec());
        pipelineRun.setStatus(new io.fabric8.tekton.v1.PipelineRunStatus());

        LOGGER.debug("Created mock PipelineRun {} for job {}", metadata.getName(), jobId);

        return pipelineRun;
    }

    private String generateMockId() {
        return "mock" + System.currentTimeMillis() % 10000;
    }
}
