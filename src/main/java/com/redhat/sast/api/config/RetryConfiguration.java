package com.redhat.sast.api.config;

import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Functional retry configuration with exponential backoff and jitter.
 * Implements a complete retry mechanism using functional programming style.
 * <p>
 * This class can be used throughout the application for consistent retry behavior
 * in various scenarios like HTTP requests, database operations, file downloads, etc.
 */
public record RetryConfiguration(
        int maxAttempts, long baseDelayMs, double multiplier, double jitterFactor, long maxDelayMs) {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryConfiguration.class);

    /**
     * Creates a retry configuration with specified parameters.
     *
     * @param maxAttempts  Maximum number of retry attempts
     * @param baseDelayMs  Base delay in milliseconds for the first retry
     * @param multiplier   Multiplier for exponential backoff (e.g., 2.0 for doubling)
     * @param jitterFactor Jitter factor to add randomness (0.0 to 1.0, e.g., 0.1 for ±10%)
     * @param maxDelayMs   Maximum delay cap in milliseconds to prevent excessive waits
     */
    public RetryConfiguration {
        if (maxAttempts <= 0) throw new IllegalArgumentException("maxAttempts must be positive");
        if (baseDelayMs <= 0) throw new IllegalArgumentException("baseDelayMs must be positive");
        if (multiplier <= 1.0) throw new IllegalArgumentException("multiplier must be greater than 1.0");
        if (jitterFactor < 0.0 || jitterFactor > 1.0)
            throw new IllegalArgumentException("jitterFactor must be between 0.0 and 1.0");
        if (maxDelayMs <= 0) throw new IllegalArgumentException("maxDelayMs must be positive");
    }

    /**
     * Creates a retry configuration with common defaults.
     *
     * @param maxAttempts Maximum number of retry attempts
     * @param baseDelayMs Base delay in milliseconds
     */
    public RetryConfiguration(int maxAttempts, long baseDelayMs) {
        this(maxAttempts, baseDelayMs, 2.0, 0.1, 30_000);
    }

    /**
     * Creates a default retry configuration suitable for most scenarios.
     * - 3 attempts
     * - 1 second base delay
     * - 2x exponential backoff
     * - 10% jitter
     * - 30 second max delay
     */
    public static RetryConfiguration defaultConfig() {
        return new RetryConfiguration(3, 1000);
    }

    /**
     * Creates a retry configuration optimized for file operations.
     * - 5 attempts (files may take time to appear)
     * - 2 second base delay
     * - 1.5x multiplier (gentler backoff)
     * - 15% jitter
     * - 60 second max delay
     */
    public static RetryConfiguration forFileOperations() {
        return new RetryConfiguration(5, 2000, 1.5, 0.15, 60_000);
    }

    /**
     * Creates a retry configuration optimized for HTTP requests.
     * - 3 attempts
     * - 500ms base delay
     * - 2x exponential backoff
     * - 20% jitter (more randomness for distributed systems)
     * - 10 second max delay
     */
    public static RetryConfiguration forHttpRequests() {
        return new RetryConfiguration(3, 500, 2.0, 0.2, 10_000);
    }

    /**
     * Creates a retry configuration for database operations.
     * - 2 attempts (usually quick to resolve)
     * - 100ms base delay
     * - 3x exponential backoff
     * - 5% jitter
     * - 5 second max delay
     */
    public static RetryConfiguration forDatabaseOperations() {
        return new RetryConfiguration(2, 100, 3.0, 0.05, 5_000);
    }

    /**
     * Determines if another retry attempt should be made.
     *
     * @param attempt Current attempt number (1-based)
     * @return true if should retry, false if max attempts reached
     */
    public boolean shouldRetry(int attempt) {
        return attempt < maxAttempts;
    }

    /**
     * Calculates exponential backoff delay with jitter.
     * Formula: baseDelay * (multiplier ^ (attempt-1)) * (1 ± jitter)
     *
     * @param attempt Current attempt number (1-based)
     * @return Delay in milliseconds before next retry
     */
    private long calculateBackoffWithJitter(int attempt) {
        if (attempt <= 1) {
            return baseDelayMs;
        }

        // Calculate exponential backoff: baseDelay * (multiplier ^ (attempt-1))
        long exponentialDelay = Math.round(baseDelayMs * Math.pow(multiplier, attempt - 1));

        // Add jitter to prevent thundering herd: delay * (1 ± jitterFactor)
        double jitter = 1.0 + (Math.random() - 0.5) * 2 * jitterFactor;
        long delayWithJitter = Math.round(exponentialDelay * jitter);

        return Math.min(delayWithJitter, maxDelayMs);
    }

    /**
     * Executes a supplier function with retry logic using exponential backoff and jitter.
     * This is the main functional interface for retry operations.
     *
     * @param <T>           Return type of the operation
     * @param operation     The operation to execute (as a Supplier)
     * @param operationName Name for logging purposes
     * @return The result of the operation, or throws the last exception if all retries fail
     * @throws RuntimeException if all retry attempts fail
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (RuntimeException e) {
                lastException = e;
                LOGGER.warn("Failed {} (attempt {}/{}): {}", operationName, attempt, maxAttempts, e.getMessage());

                if (shouldRetry(attempt)) {
                    long delayMs = calculateBackoffWithJitter(attempt);
                    sleepWithInterruptHandling(delayMs);
                }
            }
        }
        throw new RuntimeException(
                "Operation failed after " + maxAttempts + " attempts: " + operationName, lastException);
    }

    /**
     * Executes a supplier function with retry logic that returns an Optional result.
     * Useful when you want to handle failures gracefully without exceptions.
     *
     * @param <T>           Return type of the operation
     * @param operation     The operation to execute (as a Supplier)
     * @param operationName Name for logging purposes
     * @return Optional containing the result, or empty if all retries fail
     */
    public <T> Optional<T> executeWithRetryOptional(Supplier<T> operation, String operationName) {
        try {
            T result = executeWithRetry(operation, operationName);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            LOGGER.debug("All retry attempts exhausted for {}", operationName);
            return Optional.empty();
        }
    }

    private void sleepWithInterruptHandling(long delayMs) {
        try {
            LOGGER.debug("Waiting {}ms before retry (exponential backoff with jitter)", delayMs);
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Retry interrupted");
            throw new RuntimeException("Retry interrupted", ie);
        }
    }

    @Override
    public String toString() {
        return "RetryConfiguration{" + "maxAttempts="
                + maxAttempts + ", baseDelayMs="
                + baseDelayMs + ", multiplier="
                + multiplier + ", jitterFactor="
                + jitterFactor + ", maxDelayMs="
                + maxDelayMs + '}';
    }
}
