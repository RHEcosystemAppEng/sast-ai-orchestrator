package com.redhat.sast.api.mock;

import java.net.URI;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

@Alternative
@Priority(1)
@ApplicationScoped
public class MockTektonClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockTektonClient.class);
    private static final String TEST_CLUSTER_URL = "https://test-cluster:6443";

    public URL getMasterUrl() {
        try {
            LOGGER.debug("Returning test cluster URL: {}", TEST_CLUSTER_URL);
            return URI.create(TEST_CLUSTER_URL).toURL();
        } catch (Exception e) {
            throw new RuntimeException("Test cluster URL should never be malformed", e);
        }
    }

    public String getApiVersion() {
        return "tekton.dev/v1";
    }

    public String getNamespace() {
        return "test-namespace";
    }

    public MockKubernetesClient adapt() {
        return new MockKubernetesClient();
    }

    public boolean isAdaptable(Class<?> type) {
        return type == KubernetesClient.class;
    }

    public void close() {
        // No resources to clean up in mock implementation
    }

    public static class MockKubernetesClient {

        public void close() {
            // No resources to clean up in mock implementation
        }

        public String getApiVersion() {
            return "v1";
        }

        public String getNamespace() {
            return "test-namespace";
        }

        public URL getMasterUrl() {
            try {
                return URI.create(TEST_CLUSTER_URL).toURL();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Object getKubernetesVersion() {
            return new Object() {
                @SuppressWarnings("unused") // Method is called by external code using this mock
                public String getGitVersion() {
                    return "v1.28.0+test";
                }
            };
        }

        public MockPods pods() {
            return new MockPods();
        }
    }

    public static class MockPods {
        public MockPods inNamespace() {
            return this;
        }

        public Object list() {
            return new java.util.ArrayList<>();
        }
    }
}
