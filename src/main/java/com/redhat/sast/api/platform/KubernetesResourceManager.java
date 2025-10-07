package com.redhat.sast.api.platform;

import org.eclipse.microprofile.config.inject.ConfigProperty;

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
