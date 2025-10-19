package com.redhat.sast.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.redhat.sast.api.enums.OshFailureReason;
import com.redhat.sast.api.model.OshUncollectedScan;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;

/**
 * Repository for managing OSH uncollected scans (retry queue).
 *
 * This repository provides atomic operations for managing the retry queue of failed OSH scans.
 * It uses database-level locking (FOR UPDATE SKIP LOCKED) to ensure safe concurrent access
 * when multiple scheduler instances or manual operations interact with the retry queue.
 *
 * Key operations:
 * - Atomic retry scan selection with database locking
 * - FIFO retry order
 * - Efficient cleanup of expired retry records
 * - Status queries for monitoring and debugging
 */
@ApplicationScoped
public class OshUncollectedScanRepository implements PanacheRepository<OshUncollectedScan> {

    /**
     * Finds scans eligible for retry using database-level locking for concurrency safety.
     *
     * This method implements the core retry selection algorithm:
     * 1. Filters by backoff time (last_attempt_at < cutoff)
     * 2. Filters by retry limit (attempt_count < maxAttempts)
     * 3. Orders by created_at (FIFO) for fairness
     * 4. Uses FOR UPDATE SKIP LOCKED for concurrent scheduler safety
     *
     * The FOR UPDATE SKIP LOCKED ensures that if multiple scheduler instances run
     * concurrently, they won't select the same scans for retry, and locked rows
     * are skipped rather than blocking.
     *
     * @param cutoffTime minimum time since last attempt (for backoff enforcement)
     * @param maxAttempts maximum retry attempts allowed (0 = unlimited)
     * @param limit maximum number of scans to return
     * @return list of scans eligible for retry, locked for update
     */
    @Transactional
    public List<OshUncollectedScan> findRetryableScansWithLock(LocalDateTime cutoffTime, int maxAttempts, int limit) {

        String query =
                """
            SELECT u FROM OshUncollectedScan u
            WHERE u.lastAttemptAt < :cutoffTime
            AND (:maxAttempts = 0 OR u.attemptCount < :maxAttempts)
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
        return find("oshScanId", oshScanId).firstResultOptional();
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
        return delete("oshScanId", oshScanId);
    }

    /**
     * Deletes uncollected scan records older than the specified cutoff time.
     * @param cutoffTime delete records created before this time
     * @return number of records deleted
     */
    @Transactional
    public long deleteOlderThan(LocalDateTime cutoffTime) {
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
        if (maxAttempts <= 0) {
            return 0; // Unlimited retries
        }
        return delete("attemptCount >= ?1", maxAttempts);
    }

    /**
     * Counts uncollected scans by failure reason.
     *
     * @return list of failure reason counts
     */
    public List<Object[]> countByFailureReason() {
        return getEntityManager()
                .createQuery(
                        "SELECT u.failureReason, COUNT(u) FROM OshUncollectedScan u "
                                + "GROUP BY u.failureReason ORDER BY COUNT(u) DESC",
                        Object[].class)
                .getResultList();
    }

    /**
     * Counts uncollected scans by attempt count.
     *
     * @return list of attempt count distributions
     */
    public List<Object[]> countByAttemptCount() {
        return getEntityManager()
                .createQuery(
                        "SELECT u.attemptCount, COUNT(u) FROM OshUncollectedScan u "
                                + "GROUP BY u.attemptCount ORDER BY u.attemptCount ASC",
                        Object[].class)
                .getResultList();
    }

    /**
     * Counts scans eligible for retry right now.
     *
     * @param cutoffTime minimum time since last attempt
     * @param maxAttempts maximum retry attempts allowed
     * @return number of immediately eligible scans
     */
    public long countEligibleForRetry(LocalDateTime cutoffTime, int maxAttempts) {
        String query =
                """
            SELECT COUNT(u) FROM OshUncollectedScan u
            WHERE u.lastAttemptAt < :cutoffTime
            AND (:maxAttempts = 0 OR u.attemptCount < :maxAttempts)
            """;

        return getEntityManager()
                .createQuery(query, Long.class)
                .setParameter("cutoffTime", cutoffTime)
                .setParameter("maxAttempts", maxAttempts)
                .getSingleResult();
    }

    /**
     * Finds the oldest uncollected scan (debugging purposes).
     *
     * @return oldest scan record if any exist
     */
    public Optional<OshUncollectedScan> findOldest() {
        return find("ORDER BY createdAt ASC").page(Page.ofSize(1)).firstResultOptional();
    }

    /**
     * Finds the most recently created uncollected scan.
     *
     * @return newest scan record if any exist
     */
    public Optional<OshUncollectedScan> findNewest() {
        return find("ORDER BY createdAt DESC").page(Page.ofSize(1)).firstResultOptional();
    }

    /**
     * Finds uncollected scans for a specific package.
     *
     * @param packageName package name to filter by
     * @return list of uncollected scans for the package
     */
    public List<OshUncollectedScan> findByPackageName(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return List.of();
        }
        return find("packageName", packageName.trim()).list();
    }

    /**
     * Finds uncollected scans with a specific failure reason.
     *
     * @param failureReason specific failure reason to filter by
     * @return list of scans with that failure reason
     */
    public List<OshUncollectedScan> findByFailureReason(OshFailureReason failureReason) {
        if (failureReason == null) {
            return List.of();
        }
        return find("failureReason", failureReason).list();
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
                .setParameter("now", LocalDateTime.now())
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
        return count("oshScanId", oshScanId) > 0;
    }

    /**
     * Counts scans that have exceeded maximum retry attempts.
     *
     * @param maxAttempts maximum allowed retry attempts
     * @return number of scans that exceeded limit
     */
    public long countExceededRetries(int maxAttempts) {
        if (maxAttempts <= 0) {
            return 0; // Unlimited retries
        }
        return count("attemptCount >= ?1", maxAttempts);
    }

    /**
     * Finds recent scans for admin inspection.
     *
     * @param limit maximum number of records to return
     * @return list of recent retry records
     */
    public List<OshUncollectedScan> findRecentScans(int limit) {
        return find("ORDER BY createdAt DESC").page(Page.ofSize(limit)).list();
    }
}
