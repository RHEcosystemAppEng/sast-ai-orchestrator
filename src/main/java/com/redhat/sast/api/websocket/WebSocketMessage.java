package com.redhat.sast.api.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket message structure for dashboard real-time updates.
 *
 * Provides a consistent message format for all WebSocket communications
 * between the orchestrator and frontend dashboard.
 *
 * Message types:
 * - connected: Connection confirmation
 * - pong: Keepalive response
 * - job_status_change: Job status update
 * - batch_progress: Batch progress update
 * - osh_scan_collected: OSH scan collection success
 * - osh_scan_failed: OSH scan collection failure
 * - summary_update: Dashboard summary statistics update
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class WebSocketMessage {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @JsonProperty("type")
    private String type;

    @JsonProperty("data")
    private Object data;

    /**
     * Serializes the message to JSON string for sending over WebSocket.
     *
     * @return JSON string representation of the message
     */
    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize WebSocket message to JSON: {}", e.getMessage(), e);
            return "{\"type\":\"error\",\"data\":{\"message\":\"Serialization error\"}}";
        }
    }

    /**
     * Creates a message from JSON string.
     *
     * @param json JSON string to parse
     * @return WebSocketMessage instance
     * @throws JsonProcessingException if parsing fails
     */
    public static WebSocketMessage fromJson(String json) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(json, WebSocketMessage.class);
    }

    public static WebSocketMessage connected(String message) {
        return new WebSocketMessage("connected", new ConnectionData(message, java.time.LocalDateTime.now()));
    }

    public static WebSocketMessage pong() {
        return new WebSocketMessage("pong", new EmptyData());
    }

    public static WebSocketMessage jobStatusChange(Object jobData) {
        return new WebSocketMessage("job_status_change", jobData);
    }

    public static WebSocketMessage batchProgress(Object batchData) {
        return new WebSocketMessage("batch_progress", batchData);
    }

    public static WebSocketMessage summaryUpdate(Object summaryData) {
        return new WebSocketMessage("summary_update", summaryData);
    }

    public static WebSocketMessage oshScanCollected(Object jobData) {
        return new WebSocketMessage("osh_scan_collected", new OshScanCollectedData(jobData));
    }

    public static WebSocketMessage oshScanFailed(String oshScanId, String failureReason, Integer retryAttempts) {
        return new WebSocketMessage("osh_scan_failed", new OshScanFailedData(oshScanId, failureReason, retryAttempts));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectionData {
        @JsonProperty("message")
        private String message;

        @JsonProperty("timestamp")
        private java.time.LocalDateTime timestamp;
    }

    @Data
    @NoArgsConstructor
    public static class EmptyData {}

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OshScanCollectedData {
        @JsonProperty("job")
        private Object job;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OshScanFailedData {
        @JsonProperty("oshScanId")
        private String oshScanId;

        @JsonProperty("failureReason")
        private String failureReason;

        @JsonProperty("retryAttempts")
        private Integer retryAttempts;
    }
}
