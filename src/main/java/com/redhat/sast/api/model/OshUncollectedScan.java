package com.redhat.sast.api.model;

import java.time.LocalDateTime;

import com.redhat.sast.api.enums.OshFailureReason;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing an OSH scan that failed processing and is eligible for retry.
 *
 * This table serves as a "retry queue" for OSH scans that encountered transient failures
 * during processing (network errors, temporary service unavailability, etc.). Each record
 * tracks the failure reason, retry attempt count, and timing information to support
 * configurable retry policies with backoff strategies.
 *
 * All failure reasons (OshFailureReason) are eligible for retry. Retry eligibility
 * depends only on backoff timing (lastAttemptAt) and attempt count limits.
 */
@Entity
@Table(
        name = "osh_uncollected_scan",
        indexes = {
            // Unique constraint on OSH scan ID to prevent duplicates
            @Index(name = "idx_osh_uncollected_scan_id", columnList = "osh_scan_id", unique = true),

            // Optimized for retry eligibility query (filters by backoff time and attempt count)
            @Index(name = "idx_osh_uncollected_retry_eligible", columnList = "last_attempt_at, attempt_count"),

            // Optimized for retention cleanup and FIFO ordering
            @Index(name = "idx_osh_uncollected_cleanup", columnList = "created_at")
        })
@Data
@NoArgsConstructor
public class OshUncollectedScan {

    /**
     * Primary key for the uncollected scan record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * OSH scan ID that failed processing.
     * Must be unique to prevent duplicate retry entries for the same scan.
     */
    @Column(name = "osh_scan_id", nullable = false, unique = true)
    private Integer oshScanId;

    /**
     * Package name from the OSH scan.
     * Extracted from scan metadata.
     */
    @Column(name = "package_name", length = 255)
    private String packageName;

    /**
     * Package NVR (Name-Version-Release) if successfully parsed.
     * May be null if failure occurred before NVR parsing.
     */
    @Column(name = "package_nvr", length = 512)
    private String packageNvr;

    /**
     * Classification of the failure that caused this scan to be uncollected.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason", nullable = false, length = 50)
    private OshFailureReason failureReason;

    /**
     * Number of retry attempts made for this scan.
     * Used to enforce maximum retry limits.
     */
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    /**
     * Timestamp when this scan was first recorded as uncollected.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of the most recent retry attempt.
     * Used for backoff calculations to prevent rapid retry attempts.
     */
    @Column(name = "last_attempt_at", nullable = false)
    private LocalDateTime lastAttemptAt;

    /**
     * Optimistic locking version field.
     * Prevents concurrent modification conflicts when multiple schedulers
     * or manual operations attempt to update the same retry record.
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Original OSH scan response data as JSON.
     * Stored to avoid re-fetching scan details from OSH during retry attempts.
     * This preserves the exact scan state that was being processed when failure occurred.
     */
    @Column(name = "scan_data_json", columnDefinition = "TEXT")
    private String scanDataJson;

    /**
     * Error message from the most recent failure.
     */
    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;

    /**
     * Constructor for creating a new uncollected scan record.
     *
     * @param oshScanId OSH scan ID that failed
     * @param packageName package name from scan metadata
     * @param failureReason classification of the failure
     * @param scanDataJson original scan data as JSON
     * @param errorMessage error message from the failure
     */
    public OshUncollectedScan(
            Integer oshScanId,
            String packageName,
            OshFailureReason failureReason,
            String scanDataJson,
            String errorMessage) {
        this.oshScanId = oshScanId;
        this.packageName = packageName;
        this.failureReason = failureReason;
        this.scanDataJson = scanDataJson;
        this.lastErrorMessage = errorMessage;
        this.attemptCount = 0;
        this.createdAt = LocalDateTime.now();
        this.lastAttemptAt = LocalDateTime.now();
    }

    /**
     * JPA callback to set timestamps on persist.
     * Ensures created_at and last_attempt_at are set when entity is first saved.
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (lastAttemptAt == null) {
            lastAttemptAt = now;
        }
        if (attemptCount == null) {
            attemptCount = 0;
        }
    }

    /**
     * Records a new retry attempt by incrementing the attempt count
     * and updating the last attempt timestamp.
     *
     * @param newFailureReason updated failure reason if different from previous
     * @param errorMessage error message from this retry attempt
     */
    public void recordRetryAttempt(OshFailureReason newFailureReason, String errorMessage) {
        this.attemptCount++;
        this.lastAttemptAt = LocalDateTime.now();
        this.failureReason = newFailureReason;
        this.lastErrorMessage = errorMessage;
    }

    /**
     * Gets a human-readable summary of this uncollected scan for logging.
     *
     * @return formatted string with key information
     */
    public String getSummary() {
        return String.format(
                "OSH scan %d (%s) - %s attempts, last: %s, reason: %s",
                oshScanId, packageName, attemptCount, lastAttemptAt, failureReason);
    }

    @Override
    public String toString() {
        return String.format(
                "OshUncollectedScan{id=%d, oshScanId=%d, packageName='%s', "
                        + "failureReason=%s, attemptCount=%d, createdAt=%s}",
                id, oshScanId, packageName, failureReason, attemptCount, createdAt);
    }
}
