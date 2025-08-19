package com.redhat.sast.api;

import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusIntegrationTest
@Testcontainers
@TestProfile(com.redhat.sast.api.config.TestProfile.class)
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("sast_ai_test")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withStartupTimeout(Duration.ofMinutes(2));

    @BeforeAll
    static void configureProperties() {
        postgres.start();

        // Override database properties for the test
        System.setProperty("quarkus.datasource.jdbc.url", postgres.getJdbcUrl());
        System.setProperty("quarkus.datasource.username", postgres.getUsername());
        System.setProperty("quarkus.datasource.password", postgres.getPassword());
    }

    @BeforeEach
    void setUp() {}
}
