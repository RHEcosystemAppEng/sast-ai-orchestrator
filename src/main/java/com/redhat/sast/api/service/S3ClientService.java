package com.redhat.sast.api.service;

import java.net.URI;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/**
 * Service for interacting with S3/MinIO storage.
 * Used to download token metrics JSON files from pipeline runs.
 */
@ApplicationScoped
@Slf4j
public class S3ClientService {

    @ConfigProperty(name = "s3.endpoint.url")
    Optional<String> s3EndpointUrl;

    @ConfigProperty(name = "s3.bucket.name")
    Optional<String> s3BucketName;

    @ConfigProperty(name = "s3.access.key")
    Optional<String> s3AccessKey;

    @ConfigProperty(name = "s3.secret.key")
    Optional<String> s3SecretKey;

    private S3Client s3Client;

    @Inject
    public void init() {
        // Only initialize if all S3 config is present
        if (s3EndpointUrl.isPresent()
                && s3BucketName.isPresent()
                && s3AccessKey.isPresent()
                && s3SecretKey.isPresent()
                && !s3EndpointUrl.get().isBlank()) {

            try {
                AwsBasicCredentials credentials = AwsBasicCredentials.create(s3AccessKey.get(), s3SecretKey.get());

                this.s3Client = S3Client.builder()
                        .endpointOverride(URI.create(s3EndpointUrl.get()))
                        .credentialsProvider(StaticCredentialsProvider.create(credentials))
                        .region(Region.US_EAST_1) // MinIO doesn't care about region
                        .forcePathStyle(true) // Required for MinIO
                        .build();

                LOGGER.info(
                        "S3 client initialized successfully for endpoint: {} bucket: {}",
                        s3EndpointUrl.get(),
                        s3BucketName.get());
            } catch (Exception e) {
                LOGGER.error("Failed to initialize S3 client", e);
                this.s3Client = null;
            }
        } else {
            LOGGER.warn(
                    "S3 configuration incomplete - token metrics download from S3 will be disabled. "
                            + "Endpoint: {}, Bucket: {}, AccessKey: {}, SecretKey: {}",
                    s3EndpointUrl.orElse("NOT_SET"),
                    s3BucketName.orElse("NOT_SET"),
                    s3AccessKey.isPresent() ? "SET" : "NOT_SET",
                    s3SecretKey.isPresent() ? "SET" : "NOT_SET");
            this.s3Client = null;
        }
    }

    /**
     * Downloads a file from S3 as a String.
     * Retries up to 3 times with exponential backoff to handle timing issues
     * where files may not be uploaded yet.
     *
     * @param s3Key The S3 object key (path)
     * @return The file content as a String, or null if download failed
     */
    public String downloadFileAsString(String s3Key) {
        byte[] bytes = downloadFileAsBytes(s3Key);
        return bytes != null ? new String(bytes) : null;
    }

    /**
     * Downloads a file from S3 as a byte array.
     * Retries up to 3 times with exponential backoff to handle timing issues
     * where files may not be uploaded yet.
     *
     * @param s3Key The S3 object key (path)
     * @return The file content as a byte array, or null if download failed
     */
    public byte[] downloadFileAsBytes(String s3Key) {
        if (s3Client == null) {
            LOGGER.warn("S3 client not initialized - cannot download file: {}", s3Key);
            return null;
        }

        if (!s3BucketName.isPresent()) {
            LOGGER.error("S3 bucket name not configured");
            return null;
        }

        int maxRetries = 3;
        int[] retryDelays = {2000, 5000, 8000}; // Retry delays: 2s, 5s, 8s

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                LOGGER.debug(
                        "Downloading file from S3 (attempt {}/{}): bucket={}, key={}",
                        attempt,
                        maxRetries,
                        s3BucketName.get(),
                        s3Key);

                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(s3BucketName.get())
                        .key(s3Key)
                        .build();

                try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getObjectRequest)) {
                    byte[] bytes = response.readAllBytes();
                    LOGGER.info("Successfully downloaded file from S3: {} ({} bytes)", s3Key, bytes.length);
                    return bytes;
                }

            } catch (NoSuchKeyException e) {
                LOGGER.warn(
                        "File not found in S3 (attempt {}/{}): bucket={}, key={}",
                        attempt,
                        maxRetries,
                        s3BucketName.get(),
                        s3Key);
                if (attempt < maxRetries) {
                    retryWithDelay(retryDelays[attempt - 1]);
                }
            } catch (Exception e) {
                LOGGER.warn(
                        "Failed to download file from S3 (attempt {}/{}): bucket={}, key={} - {}",
                        attempt,
                        maxRetries,
                        s3BucketName.get(),
                        s3Key,
                        e.getMessage());
                if (attempt < maxRetries) {
                    retryWithDelay(retryDelays[attempt - 1]);
                } else {
                    LOGGER.error("Failed to download file from S3 after {} attempts: {}", maxRetries, s3Key, e);
                }
            }
        }
        return null;
    }

    /**
     * Waits for the specified delay before retrying.
     *
     * @param delayMs Delay in milliseconds
     */
    private void retryWithDelay(int delayMs) {
        try {
            LOGGER.debug("Waiting {}ms before retry", delayMs);
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Retry interrupted");
        }
    }

    /**
     * Constructs the S3 key for a token usage file.
     * Uses the pipeline run UUID as the directory path to match the workflow upload structure.
     *
     * @param packageName Package name (used as filename prefix)
     * @param projectVersion Project version (unused, kept for backward compatibility)
     * @param pipelineRunId Pipeline run UUID
     * @return The S3 key path
     */
    public String constructTokenUsageS3Key(String packageName, String projectVersion, String pipelineRunId) {
        // Format: {pipelineRunUUID}/{package_name}_token_usage.json
        return String.format("%s/%s_token_usage.json", pipelineRunId, packageName);
    }
}
