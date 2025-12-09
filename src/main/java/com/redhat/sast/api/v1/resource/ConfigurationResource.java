package com.redhat.sast.api.v1.resource;

import com.redhat.sast.api.config.OshConfiguration;
import com.redhat.sast.api.v1.dto.response.MonitoredPackagesResponseDto;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST resource for retrieving application configuration information.
 */
@Path("/config")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigurationResource {

    @Inject
    OshConfiguration oshConfiguration;

    /**
     * Retrieves the list of packages currently being monitored in OSH (Open Scan Hub).
     *
     * @return Response containing MonitoredPackagesResponseDto with:
     *         - packages: Set of package names being monitored
     *         - oshEnabled: Whether OSH integration is enabled
     *         - totalPackages: Total count of monitored packages
     *         - packagesFilePath: Path to the packages configuration file
     */
    @GET
    @Path("/monitored-packages")
    public Response getMonitoredPackages() {
        try {
            MonitoredPackagesResponseDto response = new MonitoredPackagesResponseDto(
                    oshConfiguration.getPackageNameSet(),
                    oshConfiguration.isEnabled(),
                    oshConfiguration.getPackageNameSet().size(),
                    oshConfiguration.getPackagesFilePath());

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving monitored packages: " + e.getMessage())
                    .build();
        }
    }
}
