package com.redhat.sast.api.v1.resource;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.redhat.sast.api.v1.dto.response.HealthResponseDto;

import io.fabric8.tekton.client.TektonClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    private static final Logger LOG = Logger.getLogger(HealthResource.class);

    @Inject
    DataSource dataSource;

    @Inject
    TektonClient tektonClient;

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "unknown")
    String applicationVersion;

    @ConfigProperty(name = "sast.ai.workflow.namespace")
    String namespace;

    @GET
    public Response getHealth() {
        HealthResponseDto health = new HealthResponseDto();
        Map<String, String> dependencies = new HashMap<>();
        boolean allHealthy = true;

        try {
            // Set application version from build info
            health.setVersion(applicationVersion);

            // Check database connectivity
            String dbStatus = checkDatabaseHealth();
            dependencies.put("database", dbStatus);
            if (!"UP".equals(dbStatus)) {
                allHealthy = false;
            }

            // Check Tekton connectivity
            String tektonStatus = checkTektonHealth();
            dependencies.put("tekton", tektonStatus);
            if (!"UP".equals(tektonStatus)) {
                allHealthy = false;
            }

            health.setDependencies(dependencies);
            health.setStatus(allHealthy ? "UP" : "DOWN");

            return allHealthy 
                ? Response.ok(health).build()
                : Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(health).build();

        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error during health check");
            health.setStatus("DOWN");
            health.setVersion(applicationVersion);

            Map<String, String> errorDependencies = new HashMap<>();
            errorDependencies.put("error", e.getMessage());
            health.setDependencies(errorDependencies);

            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(health)
                    .build();
        }
    }

    private String checkDatabaseHealth() {
        try {
            // Test database connectivity with a simple query
            try (var connection = dataSource.getConnection();
                 var statement = connection.createStatement();
                 var resultSet = statement.executeQuery("SELECT 1")) {
                
                if (resultSet.next() && resultSet.getInt(1) == 1) {
                    LOG.debug("Database health check passed");
                    return "UP";
                } else {
                    LOG.warn("Database health check failed - unexpected result");
                    return "DOWN";
                }
            }
        } catch (SQLException e) {
            LOG.errorf(e, "Database health check failed");
            return "DOWN - " + e.getMessage();
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error during database health check");
            return "DOWN - " + e.getMessage();
        }
    }

    private String checkTektonHealth() {
        try {
            // Test basic Tekton connectivity by listing pipelines in the namespace
            var pipelines = tektonClient.v1().pipelines().inNamespace(namespace).list();
            
            if (pipelines == null) {
                LOG.warn("Tekton health check failed - unable to list pipelines");
                return "DOWN - Unable to list pipelines";
            }

            // Test access to pipeline runs (which is what we actually create)
            try {
                var pipelineRuns = tektonClient.v1().pipelineRuns().inNamespace(namespace).list();
                if (pipelineRuns != null) {
                    LOG.debugf("Tekton health check passed - namespace '%s' accessible, found %d pipelines and %d pipeline runs", 
                        namespace, pipelines.getItems().size(), pipelineRuns.getItems().size());
                    return "UP";
                }
            } catch (Exception e) {
                LOG.warnf("Tekton accessible but pipeline runs may have permission issues in namespace '%s': %s", 
                    namespace, e.getMessage());
                return "UP - Limited pipeline run permissions";
            }

            LOG.debugf("Tekton health check passed - namespace '%s' accessible, found %d pipelines", 
                namespace, pipelines.getItems().size());
            return "UP";

        } catch (Exception e) {
            LOG.errorf(e, "Tekton health check failed");
            return "DOWN - " + e.getMessage();
        }
    }
}
