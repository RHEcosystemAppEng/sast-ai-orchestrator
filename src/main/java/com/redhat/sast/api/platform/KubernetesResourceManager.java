package com.redhat.sast.api.platform;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.redhat.sast.api.enums.JobStatus;
import com.redhat.sast.api.model.Job;
import com.redhat.sast.api.service.JobService;

import io.fabric8.knative.pkg.apis.Condition;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.v1.PipelineRun;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Manages Kubernetes resources lifecycle including PVCs and PipelineRuns.
 * Handles creation, cleanup, and lifecycle management of platform resources.
 */
@ApplicationScoped
public class KubernetesResourceManager {

    private static final Logger LOG = Logger.getLogger(KubernetesResourceManager.class);

    @Inject
    TektonClient tektonClient;

    @Inject
    JobService jobService;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    /**
     * AutoCloseable wrapper for PVC resources to ensure cleanup in case of failures
     */
    public class PvcResource implements AutoCloseable {
        private final String pvcName;
        private boolean shouldCleanup;

        public PvcResource(String pvcName) {
            this.pvcName = pvcName;
            this.shouldCleanup = true;
        }

        public String getName() {
            return pvcName;
        }

        public void disableAutoCleanup() {
            this.shouldCleanup = false;
        }

        @Override
        public void close() {
            if (shouldCleanup && pvcName != null) {
                deletePVC(pvcName);
            }
        }
    }

    /**
     * Creates a dedicated PVC with the specified name and size.
     *
     * @param pvcName the name for the PVC
     * @param size the storage size (e.g., "20Gi")
     * @return the created PVC name
     * @throws IllegalStateException if PVC creation fails
     */
    public String createPVC(@Nonnull String pvcName, @Nonnull String size) {
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

            LOG.infof("Created PVC: %s with size: %s in namespace: %s", pvcName, size, namespace);
            return createdPvc.getMetadata().getName();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to create PVC: %s", pvcName);
            throw new IllegalStateException("Failed to create PVC: " + pvcName, e);
        }
    }

    /**
     * Creates a PVC wrapped in an AutoCloseable resource for automatic cleanup.
     *
     * @param pvcName the name for the PVC
     * @param size the storage size (e.g., "20Gi")
     * @return PvcResource that automatically cleans up on close()
     */
    public PvcResource createManagedPVC(@Nonnull String pvcName, @Nonnull String size) {
        String createdPvcName = createPVC(pvcName, size);
        return new PvcResource(createdPvcName);
    }

    /**
     * Deletes a PVC by name.
     *
     * @param pvcName the name of the PVC to delete
     */
    public void deletePVC(@Nonnull String pvcName) {
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

    /**
     * Deletes a PipelineRun by name.
     * Includes a brief wait for pod cleanup.
     *
     * @param pipelineRunName the name of the PipelineRun to delete
     */
    public void deletePipelineRun(@Nonnull String pipelineRunName) {
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

    /**
     * Cleans up both PVCs associated with a pipeline run.
     *
     * @param pipelineRunName the base name of the pipeline run
     */
    public void cleanupPipelineRunPVCs(@Nonnull String pipelineRunName) {
        deletePVC(pipelineRunName + "-shared");
        deletePVC(pipelineRunName + "-cache");
    }

    /**
     * Gets the configured namespace for resource operations.
     *
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Checks if a pipeline run has reached a final state (succeeded or failed).
     *
     * @param pipelineRun the pipeline run to check
     * @return true if the pipeline is in a final state, false otherwise
     */
    public boolean isPipelineInFinalState(@Nonnull PipelineRun pipelineRun) {
        if (pipelineRun.getStatus() == null || pipelineRun.getStatus().getConditions() == null) {
            return false;
        }

        for (Condition condition : pipelineRun.getStatus().getConditions()) {
            if ("Succeeded".equals(condition.getType())) {
                return "True".equalsIgnoreCase(condition.getStatus())
                        || "False".equalsIgnoreCase(condition.getStatus());
            }
        }
        return false;
    }

    /**
     * Gets a PipelineRun by name from the configured namespace.
     *
     * @param pipelineRunName the name of the pipeline run
     * @return the PipelineRun or null if not found
     */
    public PipelineRun getPipelineRun(@Nonnull String pipelineRunName) {
        try {
            return tektonClient
                    .v1()
                    .pipelineRuns()
                    .inNamespace(namespace)
                    .withName(pipelineRunName)
                    .get();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to get PipelineRun: %s", pipelineRunName);
            return null;
        }
    }

    /**
     * Extracts pipeline run name from a Tekton URL stored in the job.
     *
     * @param tektonUrl the URL string from the job's tektonUrl field
     * @return the pipeline run name, or null if extraction fails
     */
    public String extractPipelineRunName(@Nonnull String tektonUrl) {
        if (tektonUrl.isBlank()) {
            throw new IllegalArgumentException("Tekton URL is blank!");
        }

        // Extract from full API URL like: .../apis/tekton.dev/v1/namespaces/ns/pipelineruns/name
        if (tektonUrl.contains("/pipelineruns/")) {
            String[] parts = tektonUrl.split("/pipelineruns/");
            if (parts.length > 1) {
                return parts[1].split("[?#]")[0]; // Remove any query params or fragments
            }
        }

        // Fallback for custom URL format like: tekton://namespaces/ns/pipelineruns/name
        if (tektonUrl.startsWith("tekton://")) {
            String[] parts = tektonUrl.split("/");
            if (parts.length > 0) {
                return parts[parts.length - 1];
            }
        }

        return null;
    }

    /**
     * Handles pipeline deletion events from the watcher in a transactional context.
     * This method is called when a pipeline is deleted (likely cancelled).
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void handlePipelineDeletion(@Nonnull Long jobId) {
        Job currentJob = jobService.getJobEntityById(jobId);
        if (currentJob != null && currentJob.getStatus() != JobStatus.CANCELLED) {
            jobService.updateJobStatus(jobId, JobStatus.CANCELLED);
        }
    }
}
