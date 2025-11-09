package com.redhat.sast.api.v1.resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.redhat.sast.api.v1.dto.WSMessage;

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
public class WebSocketResource {

    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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

    public void broadcast(WSMessage message) {
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
}
