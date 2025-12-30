package com.redhat.sast.api.v1.resource;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.redhat.sast.api.service.GoogleSheetsService;
import com.redhat.sast.api.v1.dto.response.HealthResponseDto;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.tekton.client.TektonClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class HealthResource {

    @Inject
    DataSource dataSource;

    @Inject
    TektonClient tektonClient;

    @Inject
    GoogleSheetsService googleSheetsService;

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "unknown")
    String applicationVersion;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    @GET
    public Response getHealth() {
        try {
            // Create overall health response
            HealthResponseDto health = HealthResponseDto.overall();
            health.setVersion(applicationVersion);

            // Add all health check dependencies
            health.addDependency(checkDatabaseHealth());
            health.addDependency(checkTektonHealth());
            health.addDependency(checkGoogleServiceAccountHealth());

            // Let the DTO determine overall health based on dependencies
            health.determineOverallHealth();

            // Return appropriate response based on overall status
            return "UP".equals(health.getStatus())
                    ? Response.ok(health).build()
                    : Response.status(Response.Status.SERVICE_UNAVAILABLE)
                            .entity(health)
                            .build();

        } catch (Exception e) {
            LOGGER.error("Unexpected error during health check", e);

            HealthResponseDto health = HealthResponseDto.overall();
            health.setStatus("DOWN");
            health.setVersion(applicationVersion);
            health.addDependency("unknown", e.getMessage());

            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(health)
                    .build();
        }
    }

    private HealthResponseDto checkDatabaseHealth() {
        String databaseLabel = "database";
        try {
            // Test database connectivity with a simple query
            try (var connection = dataSource.getConnection();
                    var statement = connection.createStatement();
                    var resultSet = statement.executeQuery("SELECT 1")) {

                if (resultSet.next() && resultSet.getInt(1) == 1) {
                    LOGGER.debug("Database health check passed");
                    return HealthResponseDto.up(databaseLabel);
                } else {
                    LOGGER.warn("Database health check failed - unexpected result");
                    return HealthResponseDto.down(databaseLabel, "Unexpected result from health query");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Database health check failed", e);
            return HealthResponseDto.down(databaseLabel, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Unexpected error during database health check", e);
            return HealthResponseDto.down(databaseLabel, e.getMessage());
        }
    }

    private HealthResponseDto checkTektonHealth() {
        String namespace = this.namespace;
        String tektonLabel = "tekton";
        try {
            // Check if the specific pipeline we use exists (single resource fetch, not list)
            var pipeline = tektonClient
                    .v1()
                    .pipelines()
                    .inNamespace(namespace)
                    .withName("sast-ai-workflow-pipeline")
                    .get();

            if (pipeline != null) {
                LOGGER.debug("Tekton health check: sast-ai-workflow-pipeline found in namespace={}", namespace);
                return HealthResponseDto.up(tektonLabel);
            } else {
                // Pipeline not found but API is accessible - still healthy, just needs setup
                LOGGER.warn("Tekton health check: sast-ai-workflow-pipeline not found in namespace={}", namespace);
                return HealthResponseDto.up(tektonLabel, "Pipeline not deployed yet");
            }

        } catch (KubernetesClientException e) {
            LOGGER.error("Tekton health check: failed to access Tekton API in namespace={}", namespace, e);
            return HealthResponseDto.down(tektonLabel, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Tekton health check: general failure in namespace={}", namespace, e);
            return HealthResponseDto.down(tektonLabel, e.getMessage());
        }
    }

    private HealthResponseDto checkGoogleServiceAccountHealth() {
        String serviceAccountLabel = "google-service-account";
        try {
            if (googleSheetsService.isServiceAccountAvailable()) {
                LOGGER.debug("Google service account is available");
                return HealthResponseDto.up(serviceAccountLabel, "Service account available");
            } else {
                LOGGER.warn("Google service account is not available - batch functionality will fail");
                return HealthResponseDto.down(
                        serviceAccountLabel, "Service account not available - required for batch functionality");
            }
        } catch (Exception e) {
            LOGGER.error("Google service account health check failed", e);
            return HealthResponseDto.down(serviceAccountLabel, e.getMessage());
        }
    }
}
