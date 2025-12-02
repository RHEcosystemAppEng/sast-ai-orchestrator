package com.redhat.sast.api.platform.dvc;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * REST client for the DVC API server.
 *
 * This interface uses Quarkus REST Client to generate an HTTP client implementation
 * based on configuration properties defined in application.properties:
 * - quarkus.rest-client.dvc-api.url (base URL)
 */
@Path("/")
@RegisterRestClient(configKey = "dvc-api")
public interface DvcRestClient {

    /**
     * Get the testing-data-nvrs.yaml file content.
     *
     * @param rev Optional git revision (branch, tag, or commit hash)
     * @return The YAML file content as a string
     */
    @GET
    @Path("/testing-data-nvrs")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    String getTestingDataNvrs(@QueryParam("rev") String rev);

    /**
     * Get any file from the repository by its relative path.
     *
     * @param path Relative path to the file in the repository
     * @param rev  Optional git revision (branch, tag, or commit hash)
     * @return The file content as a string
     */
    @GET
    @Path("/file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    String getFile(@QueryParam("path") String path, @QueryParam("rev") String rev);

    /**
     * Get known-non-issues ignore.err file for a specific package.
     *
     * @param packageName Package name (e.g., 'adcli', 'acl')
     * @param rev         Optional git revision (branch, tag, or commit hash)
     * @return The ignore.err file content
     */
    @GET
    @Path("/known-non-issues-el10/{package}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    String getKnownNonIssues(@PathParam("package") String packageName, @QueryParam("rev") String rev);

    /**
     * Get a file from the prompts directory.
     *
     * @param filename File name in prompts directory (e.g., 'sast-ai-prompts.yaml')
     * @param rev      Optional git revision (branch, tag, or commit hash)
     * @return The file content
     */
    @GET
    @Path("/prompts/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    String getPrompt(@PathParam("filename") String filename, @QueryParam("rev") String rev);

    /**
     * Health check endpoint.
     *
     * @return Health status JSON
     */
    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    String healthCheck();
}
