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
 * This interface uses Quarkus REST Client to generate an HTTP client implementation
 * based on configuration properties defined in application.properties:
 * - quarkus.rest-client.osh-api.url (base URL)
 * - quarkus.rest-client.osh-api.connect-timeout
 * - quarkus.rest-client.osh-api.read-timeout
 *
 */
@Path("/osh")
@RegisterRestClient(configKey = "osh-api")
public interface OshRestClient {

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
    Response fetchScanDetailsRaw(@PathParam("scanId") int scanId);
}
