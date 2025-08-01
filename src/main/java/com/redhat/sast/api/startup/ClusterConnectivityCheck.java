package com.redhat.sast.api.startup;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.fabric8.tekton.client.TektonClient;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ClusterConnectivityCheck {

    @Inject
    TektonClient tektonClient;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    // Suppress SpotBugs warning: This method intentionally throws RuntimeException for control flow.
    // Suppress SpotBugs warning: System.exit is used for graceful shutdown on fatal error.
    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("Checking OpenShift/Kubernetes cluster connectivity...");

        try {
            // Test basic cluster connectivity
            String masterUrl = tektonClient.getMasterUrl().toString();
            LOGGER.info("Attempting to connect to cluster: {}", masterUrl);

            // Try to access the cluster by getting cluster version info
            var k8sClient = tektonClient.adapt(io.fabric8.kubernetes.client.KubernetesClient.class);
            var version = k8sClient.getKubernetesVersion();

            if (version != null) {
                LOGGER.info("✅ Successfully connected to OpenShift/Kubernetes cluster");
                LOGGER.info("   Cluster URL: {}", masterUrl);
                LOGGER.info("   Cluster Version: {}", version.getGitVersion());
                LOGGER.info("   Target Namespace: {}", namespace);

                // Check if target namespace is accessible (works with namespace-scoped permissions)
                try {
                    // Instead of trying to get the namespace object, test if we can list resources in the namespace
                    // This works with namespace-scoped permissions
                    k8sClient.pods().inNamespace(namespace).list();
                    LOGGER.info("   Namespace '{}' is accessible", namespace);
                } catch (Exception e) {
                    LOGGER.warn(
                            "   Warning: Could not access namespace '{}' (may be a permissions issue): {}",
                            namespace,
                            e.getMessage());
                    LOGGER.info("   Note: Application will continue with namespace-scoped permissions");
                }

            } else {
                throw new RuntimeException("Unable to retrieve cluster version information");
            }

        } catch (Exception e) {
            LOGGER.error("❌ Failed to connect to OpenShift/Kubernetes cluster");
            LOGGER.error("   Error: {}", e.getMessage());
            LOGGER.error("   Cluster URL: {}", tektonClient.getMasterUrl().toString());
            LOGGER.error("");
            LOGGER.error("🔧 Possible causes:");
            LOGGER.error("   • Cluster is not running or unreachable");
            LOGGER.error("   • Invalid or expired authentication credentials");
            LOGGER.error("   • Network connectivity issues");
            LOGGER.error("   • Missing or incorrect kubeconfig");
            LOGGER.error("   • Service account permissions insufficient");
            LOGGER.error("");
            LOGGER.error("💡 To resolve:");
            LOGGER.error("   • Verify cluster is running: kubectl cluster-info");
            LOGGER.error("   • Check authentication: kubectl auth whoami");
            LOGGER.error("   • Test basic connectivity: kubectl get nodes");
            LOGGER.error("");
            LOGGER.error("🚫 Application will now exit gracefully...");

            // Exit gracefully
            System.exit(1);
        }
    }
}
