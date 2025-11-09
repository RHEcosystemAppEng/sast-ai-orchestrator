package com.redhat.sast.api.event;

/**
 * @param oshScanId the OSH scan ID that failed
 * @param failureReason the reason for failure
 * @param retryAttempts the number of retry attempts made (1 for initial failure, 2+ for retries)
 */
public record OshScanFailedEvent(String oshScanId, String failureReason, Integer retryAttempts) {}
