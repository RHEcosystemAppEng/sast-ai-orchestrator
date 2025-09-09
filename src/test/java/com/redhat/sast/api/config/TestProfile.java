package com.redhat.sast.api.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.quarkus.test.junit.QuarkusTestProfile;

public class TestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> config = new HashMap<>();
        config.put("quarkus.datasource.db-kind", "postgresql");
        config.put("quarkus.hibernate-orm.database.generation", "drop-and-create");
        config.put("quarkus.hibernate-orm.log.sql", "true");

        config.put("sast.ai.workflow.namespace", "test-namespace");
        config.put("google.service.account.secret.path", "/tmp/test-service-account.json");
        config.put("sast.ai.workspace.shared.size", "1Gi");
        config.put("sast.ai.workspace.cache.size", "1Gi");
        config.put("sast.ai.cleanup.completed.pipelineruns", "false");
        return config;
    }

    @Override
    public String getConfigProfile() {
        return "test";
    }

    @Override
    public Set<Class<?>> getEnabledAlternatives() {
        // Using profile-based mocking
        return Set.of();
    }
}
