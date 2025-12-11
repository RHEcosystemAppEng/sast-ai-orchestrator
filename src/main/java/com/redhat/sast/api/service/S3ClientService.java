package com.redhat.sast.api.service;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.redhat.sast.api.common.constants.MlOpsConstants;
import com.redhat.sast.api.config.RetryConfiguration;

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
                    s3EndpointUrl.orElse(MlOpsConstants.VALUE_NOT_SET),
                    s3BucketName.orElse(MlOpsConstants.VALUE_NOT_SET),
                    s3AccessKey.isPresent() ? "SET" : MlOpsConstants.VALUE_NOT_SET,
                    s3SecretKey.isPresent() ? "SET" : MlOpsConstants.VALUE_NOT_SET);
            this.s3Client = null;
        }
    }

    /**
     * Downloads a file from S3 as a String.
     * Uses exponential backoff with jitter retry algorithm optimized for file operations.
     *
     * @param s3Key The S3 object key (path)
     * @return The file content as a String, or empty string if download failed
     */
    public String downloadFileAsString(String s3Key) {
        byte[] bytes = downloadFileAsBytes(s3Key);
        return bytes.length > 0 ? new String(bytes, StandardCharsets.UTF_8) : "";
    }

    /**
     * Downloads a file from S3 as a byte array.
     * Uses exponential backoff with jitter retry algorithm optimized for file operations.
     *
     * @param s3Key The S3 object key (path)
     * @return The file content as a byte array, or empty array if download failed
     */
    public byte[] downloadFileAsBytes(String s3Key) {
        if (isValidS3Configuration(s3Key)) {
            return performDownloadWithRetry(s3Key);
        }
        return new byte[0];
    }

    private boolean isValidS3Configuration(String s3Key) {
        if (s3Client == null) {
            LOGGER.error("S3 client not initialized - cannot download file: {}", s3Key);
            return false;
        }

        if (s3BucketName.isEmpty()) {
            LOGGER.error("S3 bucket name not configured");
            return false;
        }
        return true;
    }

    private byte[] performDownloadWithRetry(String s3Key) {

        return RetryConfiguration.forFileOperations()
                .executeWithRetryOptional(
                        () -> {
                            try {
                                byte[] result = downloadFromS3(s3Key);
                                if (result.length == 0) {
                                    throw new RuntimeException("Downloaded empty file or file not found");
                                }
                                return result;
                            } catch (IOException e) {
                                throw new RuntimeException("S3 download failed: " + e.getMessage(), e);
                            }
                        },
                        "S3 file download: " + s3Key)
                .orElse(new byte[0]);
    }

    private byte[] downloadFromS3(String s3Key) throws IOException {
        GetObjectRequest request =
                GetObjectRequest.builder().bucket(s3BucketName.get()).key(s3Key).build();

        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request)) {
            byte[] bytes = response.readAllBytes();
            LOGGER.info("Successfully downloaded file from S3: {} ({} bytes)", s3Key, bytes.length);
            return bytes;
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
