package com.redhat.sast.api.v1.resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.redhat.sast.api.event.BatchProgressEvent;
import com.redhat.sast.api.event.JobStatusChangedEvent;
import com.redhat.sast.api.event.OshScanCollectedEvent;
import com.redhat.sast.api.event.OshScanFailedEvent;
import com.redhat.sast.api.mapper.JobMapper;
import com.redhat.sast.api.v1.dto.WSMessage;
import com.redhat.sast.api.v1.dto.response.JobResponseDto;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket endpoint for real-time dashboard updates.
 *
 * Observes CDI events and broadcasts them to connected WebSocket clients:
 * - Job status changes
 * - Batch progress updates
 * - OSH scans collected/failed
 */
@ServerEndpoint("/ws/dashboard")
@ApplicationScoped
@Slf4j
public class WebSocketResource {

    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @OnOpen
    public void onOpen(Session session) {
        sessions.put(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.error("WebSocket error for session {}: {}", session.getId(), throwable.getMessage());
        sessions.remove(session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {

        if ("ping".equals(message)) {
            sendTestReply(session, new WSMessage("pong", Map.of("timestamp", System.currentTimeMillis())));
        }
    }

    public static void broadcast(WSMessage message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            LOGGER.error("Failed to serialize WebSocket message", e);
            return;
        }

        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getAsyncRemote().sendText(json);
                } catch (Exception e) {
                    LOGGER.error("Failed to send to session {}: {}", session.getId(), e.getMessage());
                }
            }
        });
    }

    private void sendTestReply(Session session, WSMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.getAsyncRemote().sendText(json);
        } catch (Exception e) {
            LOGGER.error("Failed to send to test reply {}: {}", session.getId(), e.getMessage());
        }
    }

    /**
     * Observes job status change events and broadcasts to WebSocket clients.
     */
    public void onJobStatusChanged(@Observes JobStatusChangedEvent event) {
        try {
            JobResponseDto jobDto = JobMapper.INSTANCE.jobToJobResponseDto(event.job());
            var message = new WSMessage("job_status_change", jobDto);
            broadcast(message);
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to broadcast job status change for job ID {}: {}",
                    event.job().getId(),
                    e.getMessage(),
                    e);
        }
    }

    /**
     * Observes batch progress events and broadcasts to WebSocket clients.
     */
    public void onBatchProgress(@Observes BatchProgressEvent event) {
        try {
            var message = new WSMessage("batch_progress", event.jobBatch());
            broadcast(message);
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to broadcast batch progress for batch ID {}: {}",
                    event.jobBatch().getId(),
                    e.getMessage(),
                    e);
        }
    }

    /**
     * Observes OSH scan collected events and broadcasts to WebSocket clients.
     */
    public void onOshScanCollected(@Observes OshScanCollectedEvent event) {
        try {
            JobResponseDto jobDto = JobMapper.INSTANCE.jobToJobResponseDto(event.job());
            var message = new WSMessage("osh_scan_collected", Map.of("job", jobDto));
            broadcast(message);
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to broadcast OSH scan collection for job ID {}: {}",
                    event.job().getId(),
                    e.getMessage(),
                    e);
        }
    }

    /**
     * Observes OSH scan failed events and broadcasts to WebSocket clients.
     */
    public void onOshScanFailed(@Observes OshScanFailedEvent event) {
        try {
            var message = new WSMessage(
                    "osh_scan_failed",
                    Map.of(
                            "oshScanId",
                            event.oshScanId(),
                            "failureReason",
                            event.failureReason(),
                            "retryAttempts",
                            event.retryAttempts()));
            broadcast(message);
        } catch (Exception e) {
            LOGGER.error(
                    "Failed to broadcast OSH scan failure for scan ID {}: {}", event.oshScanId(), e.getMessage(), e);
        }
    }
}
