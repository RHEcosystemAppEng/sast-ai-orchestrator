package com.redhat.sast.api.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.tekton.v1.PipelineRun;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

@Alternative
@Priority(1)
@ApplicationScoped
public class MockKubernetesResourceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockKubernetesResourceManager.class);

    public PersistentVolumeClaim createSharedWorkspacePvc(String pvcName, String size) {
        LOGGER.debug("Creating mock shared workspace PVC: {}", pvcName);

        PersistentVolumeClaim pvc = new PersistentVolumeClaim();
        io.fabric8.kubernetes.api.model.ObjectMeta metadata = new io.fabric8.kubernetes.api.model.ObjectMeta();
        metadata.setName(pvcName);
        metadata.setNamespace("test-namespace");
        pvc.setMetadata(metadata);

        return pvc;
    }

    public PersistentVolumeClaim createCacheWorkspacePvc(String pvcName, String size) {
        LOGGER.debug("Creating mock cache workspace PVC: {}", pvcName);

        PersistentVolumeClaim pvc = new PersistentVolumeClaim();
        io.fabric8.kubernetes.api.model.ObjectMeta metadata = new io.fabric8.kubernetes.api.model.ObjectMeta();
        metadata.setName(pvcName);
        metadata.setNamespace("test-namespace");
        pvc.setMetadata(metadata);

        return pvc;
    }

    public void deletePipelineRun(PipelineRun pipelineRun) {
        String pipelineName = (pipelineRun != null && pipelineRun.getMetadata() != null
                ? pipelineRun.getMetadata().getName()
                : "unknown");
        LOGGER.debug("Deleting mock pipeline run: {}", pipelineName);
    }

    public void deleteResourcesForJob(Long jobId) {
        LOGGER.debug("Cleaning up mock resources for job: {}", jobId);
    }

    public boolean resourceExists(String resourceName) {
        LOGGER.debug("Checking if mock resource exists: {}", resourceName);
        return false;
    }
}
