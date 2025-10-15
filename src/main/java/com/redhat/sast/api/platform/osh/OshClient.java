package com.redhat.sast.api.platform.osh;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST client for OSH (Open Scan Hub) API integration.
 *
 * OSH API Overview:
 * - Base URL: https://cov01.lab.eng.brq2.redhat.com
 * - Single endpoint: GET /osh/task/{scanId}
 * - No pagination - uses sequential scan IDs (1001, 1002, 1003...)
 * - Returns JSON or HTML responses (both formats possible)
 * - 404 responses are normal for missing scan IDs
 *
 * This client returns raw Response objects to handle:
 * - Mixed JSON/HTML content types
 * - Normal 404 responses for missing scan IDs
 * - Error status codes that need special handling
 *
 * Business logic and response parsing are handled in OshClientService.
 */
@Path("/osh")
@RegisterRestClient(configKey = "osh-api")
public interface OshClient {

    /**
     * Fetches OSH scan details by scan ID.
     *
     * Response parsing is handled by OshClientService to accommodate
     * both JSON and HTML response formats.
     *
     * @param scanId sequential integer scan ID (e.g., 1001, 1002, etc.)
     * @return raw HTTP response for flexible parsing
     */
    @GET
    @Path("/task/{scanId}")
    @Produces(MediaType.WILDCARD) // Accept both JSON and HTML responses
    Response getScanDetailRaw(@PathParam("scanId") int scanId);
}
