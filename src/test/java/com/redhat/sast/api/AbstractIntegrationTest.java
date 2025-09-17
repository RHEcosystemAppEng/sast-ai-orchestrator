package com.redhat.sast.api;

import org.junit.jupiter.api.BeforeEach;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusIntegrationTest
@TestProfile(com.redhat.sast.api.config.TestProfile.class)
public abstract class AbstractIntegrationTest {

    @BeforeEach
    void setUp() {}
}
