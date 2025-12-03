package com.redhat.sast.api.service;

import java.util.*;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import com.redhat.sast.api.exceptions.DvcException;
import com.redhat.sast.api.platform.dvc.DvcRestClient;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class DvcService {

    private final DvcRestClient dvcRestClient;

    public DvcService(@RestClient DvcRestClient dvcRestClient) {
        this.dvcRestClient = dvcRestClient;
    }

    /**
     * Get list of NVRs from DVC repository by version tag.
     * Fetches YAML file from DVC API server and extracts NVR list.
     *
     * @param version DVC version tag (e.g., "1.0.0" or "v1.0.0")
     * @return List of package NVR strings (empty list if no NVRs found)
     * @throws DvcException if DVC fetch fails or parsing fails
     * @throws IllegalArgumentException if version is null or empty
     */
    public List<String> getNvrList(@Nonnull String version) {
        String yamlContent = fetchNvrConfigFromDvc(version);
        LOGGER.debug("Raw YAML content from DVC API ({} bytes)", yamlContent.length());

        Object object;
        try {
            LoaderOptions loaderOptions = new LoaderOptions();
            Yaml yaml = new Yaml(new SafeConstructor(loaderOptions));
            object = yaml.load(yamlContent);
        } catch (RuntimeException e) {
            throw new DvcException("Failed to parse YAML content for version " + version, e);
        }
        Set<String> nvrSet = new HashSet<>();

        try {
            if (object instanceof Map) {
                // YAML has a map structure, find list of strings
                Map<String, List<String>> map = (Map<String, List<String>>) object;
                for (List<String> stringList : map.values()) {
                    nvrSet.addAll(stringList);
                }
            } else if (object instanceof List) {
                // YAML is just a list of NVRs
                List<String> list = (List<String>) object;
                nvrSet.addAll(list);
            }
        } catch (RuntimeException e) {
            throw new DvcException(
                    "Unexpected data type while parsing YAML for version " + version
                            + " (expected Map<String, List<String>> or List<String>)",
                    e);
        }
        if (nvrSet.isEmpty()) {
            LOGGER.warn("No NVRs found in YAML for DVC version {}", version);
            return Collections.emptyList();
        }
        return nvrSet.stream().toList();
    }

    /**
     * Validates repo tag version - expected semantic version (v1.0.0)
     */
    private void validateDvcInputs(String version) {
        // Prevent ReDoS by limiting input length
        if (version.length() > 100) {
            String displayVersion = version.substring(0, 50) + "...";
            throw new IllegalArgumentException("DVC version too long (max 100 characters): " + displayVersion);
        }

        if (!version.matches(
                "^(v?\\d+\\.\\d+\\.\\d+(?:-[a-zA-Z0-9]+)?(?:\\+[a-zA-Z0-9]+)?|[a-zA-Z][a-zA-Z0-9_-]{0,49}|\\d{4}-\\d{2}-\\d{2})$")) {
            throw new IllegalArgumentException("Invalid DVC version format: '" + version
                    + "' - expected semantic version (v1.0.0) or valid identifier");
        }
    }

    /**
     * Fetches NVR configuration from DVC API server.
     *
     * @param version the version/revision to fetch
     * @return the YAML content as a string
     * @throws DvcException if the API call fails
     */
    private String fetchNvrConfigFromDvc(@Nonnull String version) {
        validateDvcInputs(version);
        LOGGER.debug("Fetching testing-data-nvrs.yaml from DVC API (rev={})", version);

        try {
            String content = dvcRestClient.getTestingDataNvrs(version);
            LOGGER.debug("Successfully fetched testing-data-nvrs.yaml from DVC API ({} bytes)", content.length());
            return content;
        } catch (WebApplicationException e) {
            int statusCode = e.getResponse().getStatus();
            String errorMessage = String.format(
                    "DVC API request failed with status %d for version '%s': %s", statusCode, version, e.getMessage());
            LOGGER.error(errorMessage, e);
            throw new DvcException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage =
                    String.format("Failed to fetch data from DVC API for version '%s': %s", version, e.getMessage());
            LOGGER.error(errorMessage, e);
            throw new DvcException(errorMessage, e);
        }
    }

    /**
     * Get any file from DVC repository by path and version.
     *
     * @param path    Relative path to the file in the repository
     * @param version DVC version tag (e.g., "1.0.0" or "v1.0.0")
     * @return File content as string
     * @throws DvcException if the API call fails
     */
    public String getFile(@Nonnull String path, @Nonnull String version) {
        validateDvcInputs(version);
        LOGGER.debug("Fetching file '{}' from DVC API (rev={})", path, version);

        try {
            String content = dvcRestClient.getFile(path, version);
            LOGGER.debug("Successfully fetched file '{}' from DVC API ({} bytes)", path, content.length());
            return content;
        } catch (WebApplicationException e) {
            int statusCode = e.getResponse().getStatus();
            String errorMessage = String.format(
                    "DVC API request failed with status %d for file '%s' version '%s': %s",
                    statusCode, path, version, e.getMessage());
            LOGGER.error(errorMessage, e);
            throw new DvcException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = String.format(
                    "Failed to fetch file '%s' from DVC API for version '%s': %s", path, version, e.getMessage());
            LOGGER.error(errorMessage, e);
            throw new DvcException(errorMessage, e);
        }
    }

    /**
     * Get known-non-issues ignore.err file for a specific package.
     *
     * @param packageName Package name (e.g., 'adcli', 'acl')
     * @param version     DVC version tag
     * @return The ignore.err file content
     * @throws DvcException if the API call fails
     */
    public String getKnownNonIssues(@Nonnull String packageName, @Nonnull String version) {
        validateDvcInputs(version);
        LOGGER.debug("Fetching known-non-issues for package '{}' from DVC API (rev={})", packageName, version);

        try {
            String content = dvcRestClient.getKnownNonIssues(packageName, version);
            LOGGER.debug(
                    "Successfully fetched known-non-issues for package '{}' from DVC API ({} bytes)",
                    packageName,
                    content.length());
            return content;
        } catch (WebApplicationException e) {
            int statusCode = e.getResponse().getStatus();
            String errorMessage = String.format(
                    "DVC API request failed with status %d for known-non-issues package '%s' version '%s': %s",
                    statusCode, packageName, version, e.getMessage());
            LOGGER.error(errorMessage, e);
            throw new DvcException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = String.format(
                    "Failed to fetch known-non-issues for package '%s' from DVC API for version '%s': %s",
                    packageName, version, e.getMessage());
            LOGGER.error(errorMessage, e);
            throw new DvcException(errorMessage, e);
        }
    }

    /**
     * Get a file from the prompts directory.
     *
     * @param filename File name in prompts directory (e.g., 'sast-ai-prompts.yaml')
     * @param version  DVC version tag
     * @return The file content
     * @throws DvcException if the API call fails
     */
    public String getPrompt(@Nonnull String filename, @Nonnull String version) {
        validateDvcInputs(version);
        LOGGER.debug("Fetching prompt file '{}' from DVC API (rev={})", filename, version);

        try {
            String content = dvcRestClient.getPrompt(filename, version);
            LOGGER.debug("Successfully fetched prompt file '{}' from DVC API ({} bytes)", filename, content.length());
            return content;
        } catch (WebApplicationException e) {
            int statusCode = e.getResponse().getStatus();
            String errorMessage = String.format(
                    "DVC API request failed with status %d for prompt file '%s' version '%s': %s",
                    statusCode, filename, version, e.getMessage());
            LOGGER.error(errorMessage, e);
            throw new DvcException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = String.format(
                    "Failed to fetch prompt file '%s' from DVC API for version '%s': %s",
                    filename, version, e.getMessage());
            LOGGER.error(errorMessage, e);
            throw new DvcException(errorMessage, e);
        }
    }

    /**
     * Check if DVC API server is healthy.
     *
     * @return true if the server is healthy, false otherwise
     */
    public boolean isHealthy() {
        try {
            String response = dvcRestClient.healthCheck();
            return response != null && response.contains("healthy");
        } catch (Exception e) {
            LOGGER.warn("DVC API health check failed: {}", e.getMessage());
            return false;
        }
    }
}
