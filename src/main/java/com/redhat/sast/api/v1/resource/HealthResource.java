package com.redhat.sast.api.v1.resource;

import java.util.HashMap;
import java.util.Map;

import com.redhat.sast.api.v1.dto.response.HealthResponseDto;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @GET
    public Response getHealth() {
        try {
            HealthResponseDto health = new HealthResponseDto();
            health.setStatus("UP");
            health.setVersion("1.0.0-SNAPSHOT");

            // Check dependencies status
            Map<String, String> dependencies = new HashMap<>();
            dependencies.put("database", "UP"); // TODO: Implement actual DB health check
            dependencies.put("tekton", "UP"); // TODO: Implement actual Tekton health check
            health.setDependencies(dependencies);

            return Response.ok(health).build();
        } catch (Exception e) {
            HealthResponseDto health = new HealthResponseDto();
            health.setStatus("DOWN");
            health.setVersion("1.0.0-SNAPSHOT");

            Map<String, String> dependencies = new HashMap<>();
            dependencies.put("error", e.getMessage());
            health.setDependencies(dependencies);

            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(health)
                    .build();
        }
    }
}
