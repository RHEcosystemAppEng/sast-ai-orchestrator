package com.redhat.sast.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.redhat.sast.api.enums.OshFailureReason;
import com.redhat.sast.api.model.OshUncollectedScan;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for statistical and aggregation queries on the OSH retry queue.
 *
 * This repository is focused on read-only analytical operations for monitoring
 * and admin interfaces. For core retry operations, use OshUncollectedScanRepository.
 *
 * Key operations:
 * - Failure reason analysis and counting
 * - Retry attempt distribution statistics
 * - Queue aging and eligibility metrics
 * - Package-based filtering for troubleshooting
 * - Admin queue inspection with pagination
 */
@ApplicationScoped
public class OshRetryStatisticsRepository implements PanacheRepository<OshUncollectedScan> {

    /**
     * Counts uncollected scans by failure reason for failure analysis.
     *
     * @return list of [OshFailureReason, Long] pairs ordered by count descending
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
     * Counts uncollected scans by attempt count for retry analysis.
     *
     * @return list of [Integer, Long] pairs ordered by attempt count ascending
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
     * Counts scans eligible for retry based on backoff and attempt limits.
     *
     * @param cutoffTime minimum time since last attempt (for backoff enforcement)
     * @param maxAttempts maximum retry attempts allowed (0 = unlimited)
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
     * Finds uncollected scans with a specific failure reason for diagnostic purposes.
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
     * Finds uncollected scans for a specific package for package-specific analysis.
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
     * Finds the oldest uncollected scan for debugging purposes.
     *
     * @return oldest scan record if any exist
     */
    public Optional<OshUncollectedScan> findOldest() {
        return find("ORDER BY createdAt ASC").page(Page.ofSize(1)).firstResultOptional();
    }

    /**
     * Finds the most recently created uncollected scan for debugging purposes.
     *
     * @return newest scan record if any exist
     */
    public Optional<OshUncollectedScan> findNewest() {
        return find("ORDER BY createdAt DESC").page(Page.ofSize(1)).firstResultOptional();
    }

    /**
     * Finds recent scans for admin queue inspection with input validation.
     *
     * @param limit maximum number of records to return
     * @return list of recent retry records
     * @throws IllegalArgumentException if limit is invalid
     */
    public List<OshUncollectedScan> findRecentScans(int limit) {
        // Validate limit at repository layer for defense-in-depth
        if (limit <= 0 || limit > 1000) {
            throw new IllegalArgumentException("Invalid limit: " + limit + " (must be 1-1000)");
        }
        return find("ORDER BY createdAt DESC").page(Page.ofSize(limit)).list();
    }
}
