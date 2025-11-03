package com.redhat.sast.api.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket server endpoint for real-time dashboard updates.
 *
 * Manages client connections and handles bidirectional communication
 * for dashboard real-time monitoring features.
 *
 */
@ServerEndpoint("/ws/dashboard")
@ApplicationScoped
@Slf4j
public class DashboardWebSocket {

    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    /**
     * Handles new WebSocket connection.
     *
     * @param session the WebSocket session
     */
    @OnOpen
    public void onOpen(Session session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);

        LOGGER.info(
                "Dashboard WebSocket connection opened: {} (Total active connections: {})", sessionId, sessions.size());
    }

    /**
     * Handles WebSocket connection close.
     *
     * @param session the WebSocket session
     */
    @OnClose
    public void onClose(Session session) {
        String sessionId = session.getId();
        sessions.remove(sessionId);

        LOGGER.debug(
                "Dashboard WebSocket connection closed: {} (Total active connections: {})", sessionId, sessions.size());
    }

    /**
     * Handles WebSocket errors.
     *
     * @param session the WebSocket session
     * @param throwable the error that occurred
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        String sessionId = session.getId();
        LOGGER.error("Dashboard WebSocket error for session {}: {}", sessionId, throwable.getMessage(), throwable);

        sessions.remove(sessionId);
    }

    /**
     * Handles incoming messages from clients.
     *
     * Currently supports:
     * - ping: Keepalive request (responds with pong)
     *
     * @param message the received message
     * @param session the WebSocket session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        LOGGER.debug("Dashboard WebSocket message received from {}: {}", session.getId(), message);

        try {
            WebSocketMessage wsMessage = WebSocketMessage.fromJson(message);

            if ("ping".equals(wsMessage.getType())) {
                sendMessage(session, WebSocketMessage.pong());
            } else {
                LOGGER.warn("Unknown message type received: {}", wsMessage.getType());
            }

        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to parse WebSocket message: {}", e.getMessage());
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message the message to broadcast
     */
    public void broadcast(WebSocketMessage message) {
        String jsonMessage = message.toJson();
        int successCount = 0;
        int failureCount = 0;

        for (Session session : sessions.values()) {
            if (sendMessage(session, jsonMessage)) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        if (failureCount > 0) {
            LOGGER.warn(
                    "Broadcast message '{}' delivered to {} clients, failed for {} clients",
                    message.getType(),
                    successCount,
                    failureCount);
        } else {
            LOGGER.debug("Broadcast message '{}' delivered to {} clients", message.getType(), successCount);
        }
    }

    /**
     * Sends a message to a specific session.
     *
     * @param session the WebSocket session
     * @param message the WebSocketMessage to send
     */
    private void sendMessage(Session session, WebSocketMessage message) {
        sendMessage(session, message.toJson());
    }

    /**
     * Sends a JSON string message to a specific session.
     *
     * @param session the WebSocket session
     * @param message the JSON message to send
     * @return true if message was sent successfully, false otherwise
     */
    private boolean sendMessage(Session session, String message) {
        if (session == null || !session.isOpen()) {
            LOGGER.warn("Cannot send message to closed or null session");
            return false;
        }

        try {
            session.getAsyncRemote().sendText(message);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to send WebSocket message to session {}: {}", session.getId(), e.getMessage());
            sessions.remove(session.getId());
            return false;
        }
    }

    /**
     * Gets the current number of active connections.
     *
     * @return number of active WebSocket sessions
     */
    public int getActiveConnectionCount() {
        return sessions.size();
    }
}
