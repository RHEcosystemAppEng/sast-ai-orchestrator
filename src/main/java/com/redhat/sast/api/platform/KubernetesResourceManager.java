package com.redhat.sast.api.platform;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.v1.PipelineRun;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages Kubernetes resources lifecycle including PVCs and PipelineRuns.
 * Handles creation, cleanup, and lifecycle management of platform resources.
 */
@ApplicationScoped
@Slf4j
public class KubernetesResourceManager {

    @Inject
    TektonClient tektonClient;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    private static final String STORAGE_REQUEST_KEY = "storage";

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
            //            if (shouldCleanup && pvcName != null) {
            ////                deletePVC(pvcName);
            //            }
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
                    .addToRequests(STORAGE_REQUEST_KEY, new Quantity(size))
                    .endResources()
                    .endSpec()
                    .build();

            PersistentVolumeClaim createdPvc = k8sClient
                    .persistentVolumeClaims()
                    .inNamespace(namespace)
                    .resource(pvc)
                    .create();

            LOGGER.info("Created PVC: {} with size: {} in namespace: {}", pvcName, size, namespace);
            return createdPvc.getMetadata().getName();
        } catch (Exception e) {
            LOGGER.error("Failed to create PVC: {}", pvcName, e);
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
                LOGGER.info("Successfully deleted PVC: {}", pvcName);
            } else {
                LOGGER.warn("PVC {} was not found or already deleted", pvcName);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to delete PVC: {}", pvcName, e);
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
                LOGGER.info("Successfully deleted PipelineRun: {}", pipelineRunName);
                // Wait a moment for pods to be cleaned up
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn("Cleanup sleep interrupted for PipelineRun: {}", pipelineRunName);
                }
            } else {
                LOGGER.warn("PipelineRun {} was not found or already deleted", pipelineRunName);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to delete PipelineRun: {}", pipelineRunName, e);
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
     * Checks if a pipeline run has completed execution (succeeded or failed).
     *
     * @param pipelineRun the pipeline run to check
     * @return true if the pipeline has completed, false otherwise
     */
    public boolean isPipelineCompleted(@Nonnull PipelineRun pipelineRun) {
        if (pipelineRun.getStatus() == null || pipelineRun.getStatus().getConditions() == null) {
            return false;
        }

        return pipelineRun.getStatus().getConditions().stream()
                .filter(condition -> "Succeeded".equalsIgnoreCase(condition.getType()) && condition.getStatus() != null)
                .anyMatch(condition -> {
                    String status = condition.getStatus();
                    return "True".equalsIgnoreCase(status) || "False".equalsIgnoreCase(status);
                });
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
            LOGGER.error("Failed to get PipelineRun: {}", pipelineRunName, e);
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
     * Cancels a running PipelineRun by setting its status to PipelineRunCancelled.
     * This preserves execution history unlike deletion.
     *
     * @param pipelineRunName the name of the PipelineRun to cancel
     * @return true if cancellation was successful, false otherwise
     */
    public boolean cancelPipelineRun(@Nonnull String pipelineRunName) {
        try {
            PipelineRun pipelineRun = getPipelineRun(pipelineRunName);
            if (pipelineRun == null) {
                LOGGER.warn("PipelineRun {} not found for cancellation", pipelineRunName);
                return false;
            }

            if (isPipelineCompleted(pipelineRun)) {
                LOGGER.info("PipelineRun {} already completed - cannot cancel", pipelineRunName);
                return false;
            }

            // Patch the PipelineRun spec to cancel it gracefully
            // Update the existing PipelineRun's status to cancel it
            PipelineRun updatedRun = new io.fabric8.tekton.v1.PipelineRunBuilder(pipelineRun)
                    .editSpec()
                    .withStatus("Cancelled")
                    .endSpec()
                    .build();

            tektonClient
                    .v1()
                    .pipelineRuns()
                    .inNamespace(namespace)
                    .withName(pipelineRunName)
                    .patch(updatedRun);

            LOGGER.info("Successfully cancelled PipelineRun: {}", pipelineRunName);
            return true;

        } catch (Exception e) {
            LOGGER.error("Failed to cancel PipelineRun: {}", pipelineRunName, e);
            return false;
        }
    }
}
