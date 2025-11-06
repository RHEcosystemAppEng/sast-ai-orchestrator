package com.redhat.sast.api.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import com.redhat.sast.api.exceptions.DvcException;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class DvcService {

    @ConfigProperty(name = "dvc.repo.url")
    String dvcRepoUrl;

    @ConfigProperty(name = "dvc.batch.yaml.path")
    String batchYamlPath;

    /**
     * Get list of NVRs from DVC repository by version tag
     * Fetches YAML file from DVC and extracts NVR list
     *
     * @param version DVC version tag (e.g., "1.0.0" or "v1.0.0")
     * @return List of package NVR strings (empty list if no NVRs found)
     * @throws DvcException if DVC fetch fails or parsing fails
     * @throws IllegalArgumentException if version is null or empty
     */
    public List<String> getNvrListByVersion(String version) {
        LOGGER.info("Fetching NVR list from DVC repository: version={}", version);
        LOGGER.debug("Fetching YAML from DVC: path={}", batchYamlPath);

        String yamlContent = fetchFromDvc(batchYamlPath, version);
        LOGGER.debug("Raw YAML content from DVC ({} bytes)", yamlContent.length());

        // Parse YAML to extract NVRs
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(new SafeConstructor(loaderOptions));
        Object data = yaml.load(yamlContent);

        List<String> nvrList = new ArrayList<>();

        if (data instanceof java.util.Map) {
            // YAML has a map structure, find list of strings
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) data;
            for (Object value : map.values()) {
                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    // Validate each item individually
                    for (Object item : list) {
                        if (item instanceof String) {
                            nvrList.add((String) item);
                        } else {
                            LOGGER.warn("Skipping non-string item in NVR list: {}", item);
                        }
                    }
                    if (!nvrList.isEmpty()) {
                        break;
                    }
                }
            }
        } else if (data instanceof List) {
            // YAML is just a list of NVRs
            List<?> list = (List<?>) data;
            for (Object item : list) {
                if (item instanceof String) {
                    nvrList.add((String) item);
                } else {
                    LOGGER.warn("Skipping non-string item in NVR list: {}", item);
                }
            }
        }

        if (nvrList.isEmpty()) {
            LOGGER.warn("No NVRs found in YAML for DVC version {}", version);
            return nvrList;
        }

        LOGGER.info("Successfully retrieved {} NVRs from YAML (DVC version {})", nvrList.size(), version);
        LOGGER.debug("NVR list: {}", nvrList);
        return nvrList;
    }

    /**
     * Validates DVC command inputs to prevent command injection - ALIGNED WITH some of DVCMETADATASERVICE validations
     *
     * @param filePath Path to file in DVC repo
     * @param version DVC version tag
     * @throws IllegalArgumentException if inputs contain unsafe characters
     */
    private void validateDvcInputs(String filePath, String version) {
        // Validate filePath
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("DVC filePath cannot be null or empty");
        }

        // Prevent directory traversal attacks
        if (filePath.contains("..")) {
            throw new IllegalArgumentException("DVC filePath cannot contain '..' (directory traversal)");
        }

        // Allow only safe characters in file path: alphanumeric, dash, underscore, dot, forward slash
        if (!filePath.matches("^[a-zA-Z0-9._/-]+$")) {
            throw new IllegalArgumentException("DVC filePath contains invalid characters: " + filePath);
        }

        // Validate version matches DvcMetadataService.validateDataVersion() for consistency
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("DVC version cannot be null or empty");
        }

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
     * Fetches raw file content from DVC repository using DVC CLI
     *
     * @param filePath Path to file in DVC repo
     * @param version DVC version tag
     * @return File content as String
     * @throws DvcException if DVC command fails or times out
     */
    private String fetchFromDvc(String filePath, String version) {
        // Validate inputs to prevent command injection
        validateDvcInputs(filePath, version);

        LOGGER.debug("Executing DVC get command: repo={}, path={}, version={}", dvcRepoUrl, filePath, version);

        java.nio.file.Path tempFile = null;
        Process process = null;
        try {
            tempFile = java.nio.file.Files.createTempFile("dvc-fetch-", ".tmp");

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "dvc", "get", dvcRepoUrl, filePath, "--rev", version, "-o", tempFile.toString(), "--force");

            process = processBuilder.start();

            // read stderr for error messages
            String error = new String(process.getErrorStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                LOGGER.error("DVC command timed out after 60 seconds");
                throw new DvcException("DVC command timed out after 60 seconds");
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                LOGGER.error("DVC command failed with exit code {}: {}", exitCode, error);
                throw new DvcException("Failed to fetch data from DVC (exit code " + exitCode + "): " + error);
            }

            // read content from temp file - the nvrs content
            String output = java.nio.file.Files.readString(tempFile, java.nio.charset.StandardCharsets.UTF_8);
            LOGGER.debug("Successfully fetched {} bytes from DVC", output.length());
            return output;
        } catch (IOException e) {
            throw new DvcException("I/O error during DVC fetch operation", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DvcException("DVC fetch operation was interrupted", e);
        } finally {
            // force kill process if still running
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            // clean up temp file
            if (tempFile != null) {
                try {
                    java.nio.file.Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    LOGGER.warn("Failed to delete temp file: {}", tempFile, e);
                }
            }
        }
    }
}
