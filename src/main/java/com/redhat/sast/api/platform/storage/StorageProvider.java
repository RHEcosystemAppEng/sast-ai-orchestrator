package com.redhat.sast.api.platform.storage;

import java.util.concurrent.CompletionStage;

/**
 * Abstract interface for dataset storage operations.
 * Supports both filesystem and S3-compatible storage backends.
 */
public interface StorageProvider {

    /**
     * Check if the storage provider is healthy and accessible.
     *
     * @return CompletionStage that completes with true if healthy, false otherwise
     */
    CompletionStage<Boolean> isHealthy();

    /**
     * Check if a bucket exists.
     *
     * @param bucketName the name of the bucket to check
     * @return CompletionStage that completes with true if bucket exists, false otherwise
     */
    CompletionStage<Boolean> bucketExists(String bucketName);

    /**
     * Create a bucket if it doesn't exist.
     *
     * @param bucketName the name of the bucket to create
     * @return CompletionStage that completes when bucket is created or already exists
     */
    CompletionStage<Void> createBucketIfNotExists(String bucketName);

    /**
     * Get the type of storage provider.
     *
     * @return the storage type ("filesystem", "s3", etc.)
     */
    String getStorageType();

    /**
     * Get basic information about the storage provider.
     *
     * @return storage information as a string
     */
    String getStorageInfo();
}
