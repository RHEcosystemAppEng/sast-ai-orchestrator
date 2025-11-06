package com.redhat.sast.api.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import com.redhat.sast.api.exceptions.DvcException;
import com.redhat.sast.api.util.dvc.ProcessExecutor;

import jakarta.annotation.Nonnull;
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
    public List<String> getNvrList(@Nonnull String version) {

        String yamlContent = fetchNvrConfigFromDvc(version);
        LOGGER.debug("Raw YAML content from DVC ({} bytes)", yamlContent.length());

        Object object;
        try {
            LoaderOptions loaderOptions = new LoaderOptions();
            Yaml yaml = new Yaml(new SafeConstructor(loaderOptions));
            object = yaml.load(yamlContent);
        } catch (RuntimeException e) {
            throw new DvcException("Failed to parse YAML content for version " + version, e);
        }
        Set<String> nvrSet = new HashSet<>();

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

    private String fetchNvrConfigFromDvc(@Nonnull String version) {
        validateDvcInputs(version);
        LOGGER.debug("Executing DVC get command: repo={}, path={}, version={}", dvcRepoUrl, batchYamlPath, version);
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("dvc-fetch-", ".tmp");
            ProcessExecutor.runDvcCommand(dvcRepoUrl, batchYamlPath, version, tempFile);
            // read content from temp file which has filled by DVC command
            return Files.readString(tempFile, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DvcException("I/O error during DVC fetch operation", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DvcException("DVC fetch operation was interrupted", e);
        } finally {
            // clean up temp file
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    LOGGER.warn("[ACTION REQUIRED] Failed to delete temp file: {}", tempFile, e);
                }
            }
        }
    }
}
