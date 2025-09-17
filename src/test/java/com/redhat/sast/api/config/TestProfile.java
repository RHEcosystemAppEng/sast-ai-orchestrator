package com.redhat.sast.api.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.testcontainers.containers.PostgreSQLContainer;

import io.quarkus.test.junit.QuarkusTestProfile;

public class TestProfile implements QuarkusTestProfile {

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("sast_ai_test")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withStartupTimeout(Duration.ofMinutes(2));

    static {
        postgres.start();
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        Map<String, String> config = new HashMap<>();
        config.put("quarkus.datasource.db-kind", "postgresql");
        config.put("quarkus.datasource.jdbc.url", postgres.getJdbcUrl());
        config.put("quarkus.datasource.username", postgres.getUsername());
        config.put("quarkus.datasource.password", postgres.getPassword());
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
