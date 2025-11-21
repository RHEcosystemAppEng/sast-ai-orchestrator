package com.redhat.sast.api.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.redhat.sast.api.enums.OshFailureReason;
import com.redhat.sast.api.model.OshUncollectedScan;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

/**
 * Repository for core OSH retry queue operations.
 *
 * This repository handles the essential retry lifecycle: finding eligible scans,
 * updating retry attempts, and cleanup operations. It uses database-level locking
 * (FOR UPDATE SKIP LOCKED) to ensure safe concurrent access if multiple scheduler
 * instances interact with the retry queue.
 *
 * All failure reasons (OshFailureReason) are eligible for retry. Retry eligibility
 * is determined by backoff timing and maximum attempt limits, not by failure type.
 *
 * For statistical queries and monitoring, use OshRetryStatisticsRepository.
 *
 * Key operations:
 * - Atomic retry scan selection with database locking
 * - FIFO retry order with backoff enforcement
 * - Retry attempt tracking and updates
 * - Cleanup of expired and exceeded retry records
 */
@ApplicationScoped
public class OshUncollectedScanRepository implements PanacheRepository<OshUncollectedScan> {

    private static final String OSH_SCAN_ID_FIELD = "oshScanId";

    /**
     * Finds scans eligible for retry using database-level locking for concurrency safety.
     *
     * This method implements the core retry selection algorithm:
     * 1. Filters by backoff time (last_attempt_at < cutoff)
     * 2. Filters by retry limit (attempt_count < maxAttempts)
     * 3. Orders by created_at (FIFO) for fairness
     * 4. Uses FOR UPDATE SKIP LOCKED for concurrent scheduler safety
     *
     * Note: All failure reasons (OshFailureReason) are eligible for retry.
     * Eligibility is determined solely by backoff timing and attempt limits.
     *
     * The FOR UPDATE SKIP LOCKED ensures that if multiple scheduler instances run
     * concurrently, they won't select the same scans for retry, and locked rows
     * are skipped rather than blocking.
     *
     * @param cutoffTime minimum time since last attempt (for backoff enforcement)
     * @param maxAttempts maximum retry attempts allowed (-1 = unlimited)
     * @param limit maximum number of scans to return
     * @return list of scans eligible for retry, locked for update
     */
    @Transactional
    public List<OshUncollectedScan> findRetryableScansWithLock(Instant cutoffTime, int maxAttempts, int limit) {

        String query =
                """
            SELECT u FROM OshUncollectedScan u
            WHERE u.lastAttemptAt < :cutoffTime
            AND (:maxAttempts = -1 OR u.attemptCount < :maxAttempts)
            ORDER BY u.createdAt ASC
            """;

        return getEntityManager()
                .createQuery(query, OshUncollectedScan.class)
                .setParameter("cutoffTime", cutoffTime)
                .setParameter("maxAttempts", maxAttempts)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .setHint("jakarta.persistence.lock.timeout", 0)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * Finds an uncollected scan by OSH scan ID.
     * @param oshScanId OSH scan ID to search for
     * @return uncollected scan record if found
     */
    public Optional<OshUncollectedScan> findByOshScanId(Integer oshScanId) {
        if (oshScanId == null) {
            return Optional.empty();
        }
        return find(OSH_SCAN_ID_FIELD, oshScanId).firstResultOptional();
    }

    /**
     * Deletes an uncollected scan record by OSH scan ID.
     * @param oshScanId OSH scan ID to remove
     * @return number of records deleted (0 or 1)
     */
    @Transactional
    public long deleteByOshScanId(Integer oshScanId) {
        if (oshScanId == null) {
            return 0;
        }
        return delete(OSH_SCAN_ID_FIELD, oshScanId);
    }

    /**
     * Deletes uncollected scan records older than the specified cutoff time.
     * @param cutoffTime delete records created before this time
     * @return number of records deleted
     */
    @Transactional
    public long deleteOlderThan(Instant cutoffTime) {
        if (cutoffTime == null) {
            return 0;
        }
        return delete("createdAt < ?1", cutoffTime);
    }

    /**
     * Deletes uncollected scans that have exceeded maximum retry attempts.
     * @param maxAttempts maximum allowed retry attempts
     * @return number of records deleted
     */
    @Transactional
    public long deleteExceededRetries(int maxAttempts) {
        if (maxAttempts < 0) {
            return 0; // Unlimited retries
        }
        return delete("attemptCount >= ?1", maxAttempts);
    }

    /**
     * Updates the attempt count and last attempt time for a scan.
     *
     * @param scanId database ID of the uncollected scan record
     * @param newFailureReason updated failure reason
     * @param errorMessage error message from the retry attempt
     * @return number of records updated (0 or 1)
     */
    @Transactional
    public int updateRetryAttempt(Long scanId, OshFailureReason newFailureReason, String errorMessage) {
        if (scanId == null) {
            return 0;
        }

        String updateQuery =
                """
            UPDATE OshUncollectedScan u
            SET u.attemptCount = u.attemptCount + 1,
                u.lastAttemptAt = :now,
                u.failureReason = :failureReason,
                u.lastErrorMessage = :errorMessage
            WHERE u.id = :scanId
            """;

        return getEntityManager()
                .createQuery(updateQuery)
                .setParameter("now", Instant.now())
                .setParameter("failureReason", newFailureReason)
                .setParameter("errorMessage", errorMessage)
                .setParameter("scanId", scanId)
                .executeUpdate();
    }

    /**
     * Checks if a scan with the given OSH scan ID already exists in the retry queue.
     *
     * @param oshScanId OSH scan ID to check
     * @return true if scan is already in retry queue
     */
    public boolean existsByOshScanId(Integer oshScanId) {
        if (oshScanId == null) {
            return false;
        }
        return count(OSH_SCAN_ID_FIELD, oshScanId) > 0;
    }

    /**
     * Finds all uncollected scans with pagination.
     * Results are ordered by creation time descending (newest first).
     *
     * @param page page number (0-indexed)
     * @param size page size
     * @return list of uncollected scans
     */
    public List<OshUncollectedScan> findAllWithPagination(int page, int size) {
        return find("ORDER BY createdAt DESC")
                .page(io.quarkus.panache.common.Page.of(page, size))
                .list();
    }
}
