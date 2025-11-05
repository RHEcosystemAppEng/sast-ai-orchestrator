package com.redhat.sast.api.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MockPlatformService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockPlatformService.class);

    public void mockMethod() {
        LOGGER.info("This is a placeholder mock class");
    }
}
