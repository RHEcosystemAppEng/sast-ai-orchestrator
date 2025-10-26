package com.redhat.sast.api.platform.osh;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.redhat.sast.api.config.OshConfiguration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * Health check for OSH (Open Scan Hub) API integration.
 *
 * Validates that:
 * - OSH API is reachable
 * - Authentication is working
 * - API is responding with expected status codes
 *
 * Uses an extremely high scan ID (near Integer.MAX_VALUE) that cannot exist
 * to test connectivity without affecting real scan data. 404 response is considered healthy.
 */
@ApplicationScoped
@Readiness
@Slf4j
public class OshHealthCheck implements HealthCheck {

    private static final String HEALTH_CHECK_NAME = "osh-api";
    private static final int TEST_SCAN_ID = Integer.MAX_VALUE - 1000; // Very high ID that won't exist

    private static final String STATUS_KEY = "status";
    private static final String TEST_SCAN_ID_KEY = "test_scan_id";
    private static final String BASE_URL_KEY = "base_url";

    @Inject
    @RestClient
    OshRestClient oshClient;

    @Inject
    OshConfiguration config;

    @Override
    public HealthCheckResponse call() {
        if (!config.isEnabled()) {
            return HealthCheckResponse.named(HEALTH_CHECK_NAME)
                    .up()
                    .withData(STATUS_KEY, "disabled")
                    .withData("message", "OSH integration is disabled")
                    .build();
        }

        try {
            Response response = oshClient.fetchScanDetailsRaw(TEST_SCAN_ID);
            int statusCode = response.getStatus();

            if (statusCode == 200 || statusCode == 404) {
                return HealthCheckResponse.named(HEALTH_CHECK_NAME)
                        .up()
                        .withData(STATUS_KEY, "healthy")
                        .withData(TEST_SCAN_ID_KEY, String.valueOf(TEST_SCAN_ID))
                        .withData("response_status", String.valueOf(statusCode))
                        .withData(BASE_URL_KEY, config.getBaseUrl())
                        .build();
            } else {
                return HealthCheckResponse.named(HEALTH_CHECK_NAME)
                        .down()
                        .withData(STATUS_KEY, "unhealthy")
                        .withData(TEST_SCAN_ID_KEY, String.valueOf(TEST_SCAN_ID))
                        .withData("response_status", String.valueOf(statusCode))
                        .withData(BASE_URL_KEY, config.getBaseUrl())
                        .withData("message", "Unexpected status code from OSH API")
                        .build();
            }

        } catch (Exception e) {
            LOGGER.error("OSH health check failed", e);
            return HealthCheckResponse.named(HEALTH_CHECK_NAME)
                    .down()
                    .withData(STATUS_KEY, "error")
                    .withData(TEST_SCAN_ID_KEY, String.valueOf(TEST_SCAN_ID))
                    .withData(BASE_URL_KEY, config.getBaseUrl())
                    .withData("error", e.getMessage())
                    .withData("error_type", e.getClass().getSimpleName())
                    .build();
        }
    }
}
