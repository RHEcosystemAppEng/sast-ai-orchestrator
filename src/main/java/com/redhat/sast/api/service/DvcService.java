package com.redhat.sast.api.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.yaml.snakeyaml.Yaml;

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
        // Validate version parameter
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("DVC version cannot be null or empty");
        }

        LOGGER.info("Fetching NVR list from DVC repository: version={}", version);
        LOGGER.debug("Fetching YAML from DVC: path={}", batchYamlPath);

        String yamlContent = fetchFromDvc(batchYamlPath, version);
        LOGGER.debug("Raw YAML content from DVC ({} bytes)", yamlContent.length());

        // Parse YAML to extract NVRs
        Yaml yaml = new Yaml();
        Object data = yaml.load(yamlContent);

        List<String> nvrList = new ArrayList<>();

        if (data instanceof java.util.Map) {
            // YAML has a map structure, find list of strings
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) data;
            for (Object value : map.values()) {
                if (value instanceof List) {
                    List<?> list = (List<?>) value;
                    if (!list.isEmpty() && list.get(0) instanceof String) {
                        nvrList = (List<String>) list;
                        break;
                    }
                }
            }
        } else if (data instanceof List) {
            // YAML is just a list of NVRs
            nvrList = (List<String>) data;
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
     * Fetches raw file content from DVC repository using DVC CLI
     *
     * @param filePath Path to file in DVC repo
     * @param version DVC version tag
     * @return File content as String
     * @throws DvcException if DVC command fails or times out
     */
    private String fetchFromDvc(String filePath, String version) {
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
