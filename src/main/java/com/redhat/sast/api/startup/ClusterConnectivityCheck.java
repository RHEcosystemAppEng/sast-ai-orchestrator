package com.redhat.sast.api.startup;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.fabric8.tekton.client.TektonClient;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class ClusterConnectivityCheck {

    private static final Logger LOG = Logger.getLogger(ClusterConnectivityCheck.class);

    @Inject
    TektonClient tektonClient;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("Checking OpenShift/Kubernetes cluster connectivity...");

        try {
            // Test basic cluster connectivity
            String masterUrl = tektonClient.getMasterUrl().toString();
            LOG.infof("Attempting to connect to cluster: %s", masterUrl);

            // Try to access the cluster by getting cluster version info
            var k8sClient = tektonClient.adapt(io.fabric8.kubernetes.client.KubernetesClient.class);
            var version = k8sClient.getKubernetesVersion();

            if (version != null) {
                LOG.infof("✅ Successfully connected to OpenShift/Kubernetes cluster");
                LOG.infof("   Cluster URL: %s", masterUrl);
                LOG.infof("   Cluster Version: %s", version.getGitVersion());
                LOG.infof("   Target Namespace: %s", namespace);

                // Optionally check if target namespace exists
                try {
                    var targetNamespace =
                            k8sClient.namespaces().withName(namespace).get();
                    if (targetNamespace != null) {
                        LOG.infof("   Namespace '%s' is accessible", namespace);
                    } else {
                        LOG.warnf("   Warning: Target namespace '%s' not found, but cluster is reachable", namespace);
                    }
                } catch (Exception e) {
                    LOG.warnf(
                            "   Warning: Could not verify namespace '%s' (may be a permissions issue): %s",
                            namespace, e.getMessage());
                }

            } else {
                throw new RuntimeException("Unable to retrieve cluster version information");
            }

        } catch (Exception e) {
            LOG.errorf("❌ Failed to connect to OpenShift/Kubernetes cluster");
            LOG.errorf("   Error: %s", e.getMessage());
            LOG.errorf("   Cluster URL: %s", tektonClient.getMasterUrl().toString());
            LOG.errorf("");
            LOG.errorf("🔧 Possible causes:");
            LOG.errorf("   • Cluster is not running or unreachable");
            LOG.errorf("   • Invalid or expired authentication credentials");
            LOG.errorf("   • Network connectivity issues");
            LOG.errorf("   • Missing or incorrect kubeconfig");
            LOG.errorf("   • Service account permissions insufficient");
            LOG.errorf("");
            LOG.errorf("💡 To resolve:");
            LOG.errorf("   • Verify cluster is running: kubectl cluster-info");
            LOG.errorf("   • Check authentication: kubectl auth whoami");
            LOG.errorf("   • Test basic connectivity: kubectl get nodes");
            LOG.errorf("");
            LOG.errorf("🚫 Application will now exit gracefully...");

            // Exit gracefully
            System.exit(1);
        }
    }
}
