package com.redhat.sast.api.v1.resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.enterprise.context.ApplicationScoped;
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
 * Broadcasts events when:
 * - Job status changes
 * - Batch progress updates
 * - OSH scans collected/failed
 */
@ServerEndpoint("/ws/dashboard")
@ApplicationScoped
@Slf4j
public class DashboardWebSocketResource {

    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @OnOpen
    public void onOpen(Session session) {
        sessions.put(session.getId(), session);
        LOGGER.debug("Dashboard WebSocket connected: {} (total: {})", session.getId(), sessions.size());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session.getId());
        LOGGER.debug("Dashboard WebSocket disconnected: {} (remaining: {})", session.getId(), sessions.size());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.error("WebSocket error for session {}: {}", session.getId(), throwable.getMessage());
        sessions.remove(session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.debug("Received message from {}: {}", session.getId(), message);

        if ("ping".equals(message)) {
            sendToSession(session, new WebSocketMessage("pong", Map.of("timestamp", System.currentTimeMillis())));
        }
    }

    /**
     * Broadcast message to all connected dashboard clients.
     */
    public void broadcast(WebSocketMessage message) {
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

        LOGGER.debug("Broadcasted {} to {} clients", message.type(), sessions.size());
    }

    private void sendToSession(Session session, WebSocketMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.getAsyncRemote().sendText(json);
        } catch (Exception e) {
            LOGGER.error("Failed to send to session {}: {}", session.getId(), e.getMessage());
        }
    }

    public int getActiveConnectionCount() {
        return sessions.size();
    }

    public static record WebSocketMessage(String type, Object data) {}
}
