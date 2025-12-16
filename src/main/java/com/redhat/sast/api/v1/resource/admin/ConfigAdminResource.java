package com.redhat.sast.api.v1.resource.admin;

import java.time.Instant;

import com.redhat.sast.api.config.OshConfiguration;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Admin REST endpoints for configuration management.
 *
 * Provides endpoints for:
 * - Reloading monitored package list from file
 * - Other dynamic configuration updates
 */
@Path("/admin/config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Slf4j
public class ConfigAdminResource {

    private final OshConfiguration oshConfiguration;

    /**
     * Reloads the monitored package list from the packages.txt file.
     *
     * @return reload result with package count and timestamp
     */
    @POST
    @Path("/reload-packages")
    public Response reloadPackages() {
        try {
            LOGGER.info("Package reload requested via admin endpoint");

            int packageCount = oshConfiguration.reloadPackageList();

            String message = String.format("Successfully reloaded %d packages from configuration file", packageCount);
            LOGGER.info(message);

            return Response.ok()
                    .entity(new ReloadResult(message, packageCount, Instant.now()))
                    .build();

        } catch (IllegalStateException e) {
            LOGGER.error("Failed to reload package list: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ReloadResult("Failed to reload packages: " + e.getMessage(), 0, Instant.now()))
                    .build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error reloading package list: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ReloadResult("Internal server error during reload", 0, Instant.now()))
                    .build();
        }
    }

    /**
     * DTO for package reload operation results.
     *
     * @param message descriptive message about the reload operation
     * @param packageCount number of packages loaded
     * @param timestamp when the reload occurred
     */
    public static record ReloadResult(String message, int packageCount, Instant timestamp) {}
}
