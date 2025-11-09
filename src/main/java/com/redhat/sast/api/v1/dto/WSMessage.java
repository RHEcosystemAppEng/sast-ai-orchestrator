package com.redhat.sast.api.v1.dto;

/**
 * WebSocket message DTO for real-time dashboard updates.
 *
 * @param type the message type (e.g., "job_status_change", "batch_progress")
 * @param data the message payload
 */
public record WSMessage(String type, Object data) {}
