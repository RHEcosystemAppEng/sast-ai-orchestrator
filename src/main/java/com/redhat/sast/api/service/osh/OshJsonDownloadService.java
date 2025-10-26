package com.redhat.sast.api.service.osh;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for downloading SAST report JSON files from OSH (Open Scan Hub) logs.
 * Downloads JSON content directly from OSH log URLs without storing it in database.
 */
@ApplicationScoped
@Slf4j
public class OshJsonDownloadService {

    private static final List<String> JSON_FILE_PRIORITIES =
            Arrays.asList("scan-results-all.json", "scan-results-imp.json", "scan-results-summary.json");

    @ConfigProperty(name = "osh.api.base-url")
    String oshBaseUrl;

    @ConfigProperty(name = "osh.json.download-timeout", defaultValue = "30s")
    Duration downloadTimeout;

    @ConfigProperty(name = "osh.json.primary-file", defaultValue = "scan-results-all.json")
    String primaryJsonFile;

    private final HttpClient httpClient;

    public OshJsonDownloadService() {
        this.httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * Downloads the primary SAST report JSON content from OSH logs.
     * Tries multiple JSON files in priority order until one is found.
     *
     * @param scanId the OSH scan ID
     * @return JSON content as string, or empty if no JSON files are available
     */
    public Optional<String> downloadSastReport(Integer scanId) {
        LOGGER.debug("Downloading SAST report for OSH scan ID: {}", scanId);

        if (scanId == null) {
            LOGGER.warn("Scan ID is null, cannot download SAST report");
            return Optional.empty();
        }

        Optional<String> jsonContent = downloadJsonFile(scanId, primaryJsonFile);
        if (jsonContent.isPresent()) {
            LOGGER.debug("Successfully downloaded {} for OSH scan {}", primaryJsonFile, scanId);
            return jsonContent;
        }

        for (String jsonFile : JSON_FILE_PRIORITIES) {
            if (!jsonFile.equals(primaryJsonFile)) {
                jsonContent = downloadJsonFile(scanId, jsonFile);
                if (jsonContent.isPresent()) {
                    LOGGER.info("Successfully downloaded {} for OSH scan {} (fallback)", jsonFile, scanId);
                    return jsonContent;
                }
            }
        }

        LOGGER.warn("No SAST report JSON files available for OSH scan {}", scanId);
        return Optional.empty();
    }

    /**
     * Downloads a specific JSON file from OSH logs.
     *
     * @param scanId the OSH scan ID
     * @param jsonFileName the JSON file name (e.g., "scan-results-all.json")
     * @return JSON content as string, or empty if file not available
     */
    public Optional<String> downloadJsonFile(Integer scanId, String jsonFileName) {
        String jsonUrl = buildJsonFileUrl(scanId, jsonFileName);

        try {
            LOGGER.debug("Downloading JSON from URL: {}", jsonUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jsonUrl))
                    .timeout(downloadTimeout)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String jsonContent = response.body();
                if (jsonContent != null && !jsonContent.trim().isEmpty()) {
                    LOGGER.debug("Successfully downloaded {} bytes from {}", jsonContent.length(), jsonUrl);
                    return Optional.of(jsonContent);
                } else {
                    LOGGER.warn("Empty JSON content from {}", jsonUrl);
                    return Optional.empty();
                }
            } else if (response.statusCode() == 404) {
                LOGGER.debug(
                        "JSON file {} not found for scan {} (404) - normal for missing files", jsonFileName, scanId);
                return Optional.empty();
            } else {
                LOGGER.warn(
                        "Failed to download JSON from {} - HTTP {}: {}",
                        jsonUrl,
                        response.statusCode(),
                        response.body());
                return Optional.empty();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("JSON download interrupted for {}: {}", jsonUrl, e.getMessage());
            return Optional.empty();
        } catch (IOException e) {
            LOGGER.error("Network error downloading JSON from {}: {}", jsonUrl, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error("Unexpected error downloading JSON from {}: {}", jsonUrl, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Returns list of available JSON log files for a scan.
     * This method attempts to detect which JSON files are available
     * by making HEAD requests.
     *
     * @param scanId the OSH scan ID
     * @return list of available JSON file names
     */
    public List<String> getAvailableJsonFiles(Integer scanId) {
        return JSON_FILE_PRIORITIES.stream()
                .filter(jsonFile -> isJsonFileAvailable(scanId, jsonFile))
                .toList();
    }

    /**
     * Checks if a specific JSON file is available for a scan.
     * Uses HEAD request for lightweight availability check.
     *
     * @param scanId the OSH scan ID
     * @param jsonFileName the JSON file name
     * @return true if file is available (HTTP 200)
     */
    public boolean isJsonFileAvailable(Integer scanId, String jsonFileName) {
        String jsonUrl = buildJsonFileUrl(scanId, jsonFileName);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jsonUrl))
                    .timeout(Duration.ofSeconds(5))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.debug("Availability check interrupted for {} on scan {}: {}", jsonFileName, scanId, e.getMessage());
            return false;
        } catch (Exception e) {
            LOGGER.debug("Could not check availability of {} for scan {}: {}", jsonFileName, scanId, e.getMessage());
            return false;
        }
    }

    /**
     * Builds the URL for a JSON log file from OSH.
     *
     * @param scanId the OSH scan ID
     * @param jsonFileName the JSON file name
     * @return complete URL to the JSON file
     */
    private String buildJsonFileUrl(Integer scanId, String jsonFileName) {
        String baseUrl = oshBaseUrl.endsWith("/") ? oshBaseUrl.substring(0, oshBaseUrl.length() - 1) : oshBaseUrl;
        return String.format("%s/osh/task/%d/logs/%s", baseUrl, scanId, jsonFileName);
    }
}
