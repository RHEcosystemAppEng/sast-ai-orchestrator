package com.redhat.sast.api.v1.resource;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.redhat.sast.api.v1.dto.response.HealthResponseDto;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.tekton.client.TektonClient;
import io.fabric8.tekton.v1.PipelineList;
import io.fabric8.tekton.v1.PipelineRunList;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
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
        try {
            // Create overall health response
            HealthResponseDto health = HealthResponseDto.overall();
            health.setVersion(applicationVersion);

            // Add all health check dependencies
            health.addDependency(checkDatabaseHealth());
            health.addDependency(checkTektonHealth());

            // Let the DTO determine overall health based on dependencies
            health.determineOverallHealth();

            // Return appropriate response based on overall status
            return "UP".equals(health.getStatus())
                    ? Response.ok(health).build()
                    : Response.status(Response.Status.SERVICE_UNAVAILABLE)
                            .entity(health)
                            .build();

        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error during health check");

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
        try {
            // Test database connectivity with a simple query
            try (var connection = dataSource.getConnection();
                    var statement = connection.createStatement();
                    var resultSet = statement.executeQuery("SELECT 1")) {

                if (resultSet.next() && resultSet.getInt(1) == 1) {
                    LOG.debug("Database health check passed");
                    return HealthResponseDto.up("database");
                } else {
                    LOG.warn("Database health check failed - unexpected result");
                    return HealthResponseDto.down("database", "Unexpected result from health query");
                }
            }
        } catch (SQLException e) {
            LOG.errorf(e, "Database health check failed");
            return HealthResponseDto.down("database", e.getMessage());
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error during database health check");
            return HealthResponseDto.down("database", e.getMessage());
        }
    }

    private HealthResponseDto checkTektonHealth() {
        String namespace = this.namespace;
        try {
            // Test basic Tekton connectivity by listing pipelines in the namespace
            PipelineList pipelineList =
                    tektonClient.v1().pipelines().inNamespace(namespace).list();

            if (pipelineList == null) {
                LOG.warnf("Tekton health check: Pipelines list is null in namespace=%s", namespace);
                return HealthResponseDto.down("tekton", "Pipelines list is null");
            }

            try {
                // Test access to pipeline runs (which is what we actually create)
                PipelineRunList pipelineRunList =
                        tektonClient.v1().pipelineRuns().inNamespace(namespace).list();

                if (pipelineRunList == null) {
                    LOG.warnf("Tekton health check: PipelineRuns list is null in namespace=%s", namespace);
                    return HealthResponseDto.down("tekton", "PipelineRuns list is null");
                }

                LOG.infof(
                        "Tekton health check: successful in namespace=%s with %d pipelines and %d pipeline runs",
                        namespace,
                        pipelineList.getItems().size(),
                        pipelineRunList.getItems().size());
                return HealthResponseDto.up("tekton");

            } catch (KubernetesClientException e) {
                LOG.warnf(e, "Tekton health check: limited permissions in namespace=%s", namespace);
                return HealthResponseDto.up("tekton", "Limited Permissions");
            }
        } catch (KubernetesClientException e) {
            LOG.errorf(e, "Tekton health check: failed to list pipelines in namespace=%s", namespace);
            return HealthResponseDto.down("tekton", e.getMessage());
        } catch (Exception e) {
            LOG.errorf(e, "Tekton health check: general failure in namespace=%s", namespace);
            return HealthResponseDto.down("tekton", e.getMessage());
        }
    }
}
