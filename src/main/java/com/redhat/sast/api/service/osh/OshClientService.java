package com.redhat.sast.api.service.osh;

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
import com.redhat.sast.api.platform.osh.OshRestClient;
import com.redhat.sast.api.util.url.NvrParser;
import com.redhat.sast.api.v1.dto.osh.OshScanDto;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
    OshRestClient oshClient;

    @Inject
    NvrParser nvrParser;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public List<OshScanDto> fetchOshScansForProcessing(int startId, int batchSize) {
        List<OshScanDto> oshScanList = new ArrayList<>();

        int end = startId + batchSize;
        LOGGER.debug("Discovering OSH scans from ID {} to {} (exclusive)", startId, end);

        int scanId = startId;
        while (scanId < end) {
            fetchOshScanData(scanId)
                    .filter(scanObj -> "CLOSED".equals(scanObj.getState()))
                    .ifPresent(oshScanList::add);
            scanId++;
        }

        LOGGER.debug("Discovered {} finished scans in range {}-{} (exclusive)", oshScanList.size(), startId, end);
        return oshScanList;
    }

    public Optional<OshScanDto> fetchOshScanData(int oshScanId) {
        try (var httpResp = oshClient.fetchScanDetailsRaw(oshScanId)) {
            switch (httpResp.getStatus()) {
                case 200 -> {
                    var strContent = httpResp.readEntity(String.class);
                    return parseHttpResponse(strContent, oshScanId);
                }
                case 404 -> LOGGER.debug("Scan {} not found (404) - normal for missing scan IDs", oshScanId);
                default -> LOGGER.warn("OSH API error for scan {}: status {}", oshScanId, httpResp.getStatus());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to fetch scan {}: {}", oshScanId, e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<OshScanDto> parseHttpResponse(String content, int scanId) {
        try {
            if (content == null || content.isBlank()) {
                return Optional.empty();
            }
            JsonNode json = objectMapper.readTree(content);
            return parseJsonResponse(json, scanId);
        } catch (JsonProcessingException e) {
            LOGGER.debug("JSON parsing failed for scan {}, trying HTML: {}", scanId, e.getMessage());
        }

        return parseHtmlResponse(content, scanId);
    }

    private Optional<OshScanDto> parseJsonResponse(JsonNode json, int scanId) {
        if (json == null || json.isNull() || (!json.isObject() && !json.isArray())) {
            LOGGER.warn("Invalid JSON structure for scan {}: not an object or array", scanId);
            return Optional.empty();
        }
        OshScanDto response = new OshScanDto();
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

        return Optional.of(response);
    }

    private Optional<OshScanDto> parseHtmlResponse(String html, int scanId) {
        try {
            Document doc = Jsoup.parse(html);

            Elements rows = doc.select("table.details tr");

            if (rows.isEmpty()) {
                LOGGER.debug("No table.details found for scan {}, trying generic table selector", scanId);
                rows = doc.select("table tr");
            }

            if (rows.isEmpty()) {
                LOGGER.warn("No table rows found in HTML response for scan {}", scanId);
                return Optional.empty();
            }

            OshScanDto response = new OshScanDto();
            response.setScanId(scanId);

            Map<String, Object> rawData = new HashMap<>();

            for (Element row : rows) {
                Element keyElement = row.selectFirst("th");
                Element valueElement = row.selectFirst("td");

                if (keyElement != null && valueElement != null) {
                    String key = keyElement.text().trim().toLowerCase();
                    String value = valueElement.text().trim();

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
                    "Parsed HTML response for scan {}: state={}, component={}, rawData size={}",
                    scanId,
                    response.getState(),
                    response.getComponent(),
                    rawData.size());

            return Optional.of(response);

        } catch (Exception e) {
            LOGGER.error("Failed to parse HTML response for scan {}: {}", scanId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private void parseComponentFromLabel(String label, OshScanDto response) {
        if (label == null || label.isBlank()) {
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

    private String getJsonStringValue(JsonNode json, String fieldName) {
        JsonNode node = json.get(fieldName);
        return (node != null && !node.isNull()) ? node.asText() : null;
    }
}
