package com.redhat.sast.api.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.sast.api.platform.osh.OshClient;
import com.redhat.sast.api.util.url.NvrParser;
import com.redhat.sast.api.v1.dto.osh.OshScanResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * Service layer for OSH (Open Scan Hub) API integration.
 *
 * Handles business logic for:
 * - OSH API communication via OshClient
 * - Response parsing (both JSON and HTML formats)
 * - Batch scan discovery using sequential scan IDs
 * - Error handling and 404 normalization
 *
 */
@ApplicationScoped
@Slf4j
public class OshClientService {

    @Inject
    @RestClient
    OshClient oshClient;

    @Inject
    NvrParser nvrParser;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Fetches details for a single OSH scan by ID.
     *
     * Handles OSH API behavior:
     * - 200: Parse response (JSON or HTML)
     * - 404: Normal for missing scan IDs - return empty
     * - Other errors: Log and return empty
     *
     * @param scanId sequential OSH scan ID
     * @return scan details if found and parseable, empty otherwise
     */
    public Optional<OshScanResponse> getScanDetail(int scanId) {
        try {
            Response response = oshClient.getScanDetailRaw(scanId);

            if (response.getStatus() == 200) {
                String content = response.readEntity(String.class);
                return parseOshResponse(content, scanId);
            } else if (response.getStatus() == 404) {
                LOGGER.debug("Scan {} not found (404) - normal for missing scan IDs", scanId);
                return Optional.empty();
            } else {
                LOGGER.warn("OSH API error for scan {}: status {}", scanId, response.getStatus());
                return Optional.empty();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to fetch scan {}: {}", scanId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Discovers finished scans in a batch using sequential scan ID iteration.
     *
     * OSH API pattern:
     * - No list endpoint - must try individual scan IDs
     * - Sequential discovery: startId, startId+1, startId+2, etc.
     * - Filter for "CLOSED" state only
     *
     * @param startId first scan ID to check
     * @param batchSize number of sequential IDs to check
     * @return list of completed scans found in the ID range
     */
    public List<OshScanResponse> getFinishedScans(int startId, int batchSize) {
        List<OshScanResponse> finishedScans = new ArrayList<>();

        LOGGER.debug("Discovering OSH scans from ID {} to {}", startId, startId + batchSize - 1);

        for (int scanId = startId; scanId < startId + batchSize; scanId++) {
            final int currentScanId = scanId;
            getScanDetail(scanId)
                    .filter(scan -> "CLOSED".equals(scan.getState()))
                    .ifPresent(scan -> {
                        LOGGER.debug("Found completed scan: {} ({})", currentScanId, scan.getComponent());
                        finishedScans.add(scan);
                    });
        }

        LOGGER.info(
                "Discovered {} finished scans in range {}-{}", finishedScans.size(), startId, startId + batchSize - 1);
        return finishedScans;
    }

    /**
     * Parses OSH response content, handling both JSON and HTML formats.
     *
     * 1. Try JSON parsing first (preferred format)
     * 2. Fall back to HTML table parsing if JSON fails
     *
     * @param content raw response content from OSH
     * @param scanId scan ID for context in error messages
     * @return parsed scan response or empty if parsing fails
     */
    private Optional<OshScanResponse> parseOshResponse(String content, int scanId) {
        try {
            JsonNode json = objectMapper.readTree(content);
            return Optional.of(parseJsonResponse(json, scanId));
        } catch (JsonProcessingException e) {
            LOGGER.debug("JSON parsing failed for scan {}, trying HTML", scanId);
            return parseHtmlResponse(content, scanId);
        }
    }

    /**
     * Parses JSON response from OSH API.
     *
     * Maps OSH JSON fields to OshScanResponse DTO.
     * Preserves raw data for debugging.
     *
     * @param json parsed JSON response
     * @param scanId scan ID for context
     * @return parsed scan response
     */
    private OshScanResponse parseJsonResponse(JsonNode json, int scanId) {
        OshScanResponse response = new OshScanResponse();
        response.setScanId(scanId);

        Map<String, Object> rawData = new HashMap<>();

        json.fieldNames().forEachRemaining(fieldName -> {
            String value = getJsonStringValue(json, fieldName);
            rawData.put(fieldName, value);

            // Set specific fields during the same iteration
            switch (fieldName) {
                case "state" -> response.setState(value);
                case "owner" -> response.setOwner(value);
                case "scan_type" -> response.setScanType(value);
                case "created" -> response.setCreated(value);
                case "started" -> response.setStarted(value);
                case "finished" -> response.setFinished(value);
                case "arch" -> response.setArch(value);
                case "channel" -> response.setChannel(value);
                case "label" -> parseComponentFromLabel(value, response);
            }
        });

        response.setRawData(rawData);

        LOGGER.debug(
                "Parsed JSON response for scan {}: state={}, component={}",
                scanId,
                response.getState(),
                response.getComponent());

        return response;
    }

    /**
     * Parses HTML response from OSH API.
     *
     * OSH returns HTML table with scan details when JSON is not available.
     *
     * @param html raw HTML content
     * @param scanId scan ID for context
     * @return parsed scan response or empty if parsing fails
     */
    private Optional<OshScanResponse> parseHtmlResponse(String html, int scanId) {
        try {
            Document doc = Jsoup.parse(html);

            // Look for table rows with scan details
            Elements rows = doc.select("table tr");

            if (rows.isEmpty()) {
                LOGGER.warn("No table rows found in HTML response for scan {}", scanId);
                return Optional.empty();
            }

            OshScanResponse response = new OshScanResponse();
            response.setScanId(scanId);

            Map<String, Object> rawData = new HashMap<>();

            for (Element row : rows) {
                Elements cells = row.select("td");
                if (cells.size() >= 2) {
                    String key = cells.get(0).text().trim().toLowerCase();
                    String value = cells.get(1).text().trim();

                    rawData.put(key, value);

                    switch (key) {
                        case "state" -> response.setState(value);
                        case "owner" -> response.setOwner(value);
                        case "label" -> parseComponentFromLabel(value, response);
                        case "arch" -> response.setArch(value);
                        case "created" -> response.setCreated(value);
                        case "started" -> response.setStarted(value);
                        case "finished" -> response.setFinished(value);
                        default -> {
                            /* Unknown field - already stored in rawData */
                        }
                    }
                }
            }

            response.setRawData(rawData);

            LOGGER.debug(
                    "Parsed HTML response for scan {}: state={}, component={}",
                    scanId,
                    response.getState(),
                    response.getComponent());

            return Optional.of(response);

        } catch (Exception e) {
            LOGGER.error("Failed to parse HTML response for scan {}: {}", scanId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extracts component name and version from OSH label field using NvrParser.
     *
     * OSH label format examples:
     * - "systemd-252-13.el9" → component: "systemd", version: "252"
     * - "kernel-5.14.0-284.el9" → component: "kernel", version: "5.14.0"
     * - "zlib-ng-2.1.6-2.el10" → component: "zlib-ng", version: "2.1.6"
     *
     * @param label OSH label string (NVR format)
     * @param response response object to populate
     */
    private void parseComponentFromLabel(String label, OshScanResponse response) {
        if (label == null || label.trim().isEmpty()) {
            return;
        }

        String trimmedLabel = label.trim();

        String component = nvrParser.extractPackageName(trimmedLabel);
        String version = nvrParser.extractVersion(trimmedLabel);

        if (component != null) {
            response.setComponent(component);
            response.setVersion(version); // May be null if parsing fails
        } else {
            // Fallback: if NVR parsing fails, use the label as-is for component
            LOGGER.debug("NVR parsing failed for label '{}', using '{}' as component name", label, trimmedLabel);
            response.setComponent(trimmedLabel);
        }
    }

    /**
     * Safely extracts string value from JSON node.
     *
     * @param json JSON node
     * @param fieldName field to extract
     * @return field value or null if not present
     */
    private String getJsonStringValue(JsonNode json, String fieldName) {
        JsonNode node = json.get(fieldName);
        return (node != null && !node.isNull()) ? node.asText() : null;
    }
}
