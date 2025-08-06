package com.redhat.sast.api.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.redhat.sast.api.util.input.InputSourceResolver;

import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class GoogleSheetsService {

    private static final String APPLICATION_NAME = "SAST-AI-Orchestrator";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    @ConfigProperty(name = "google.service.account.secret.path")
    Optional<String> serviceAccountSecretPath;

    private final InputSourceResolver inputSourceResolver;

    @PostConstruct
    void logConfiguration() {
        String path = getServiceAccountPath();
        LOGGER.debug("GoogleSheetsService initialized with service account path: {}", path);
    }

    private String getServiceAccountPath() {
        String envPath = System.getenv("GOOGLE_SERVICE_ACCOUNT_SECRET_PATH");
        if (envPath != null && !envPath.isEmpty()) {
            return envPath;
        }
        
        if (serviceAccountSecretPath.isPresent()) {
            return serviceAccountSecretPath.get();
        }
        
        throw new IllegalStateException(
            "Google service account path not configured. Please ensure GOOGLE_SERVICE_ACCOUNT_SECRET_PATH environment variable is set.");
    }

    /**
     * Reads a Google Sheet using service account authentication and returns the raw sheet data.
     *
     * @param googleSheetUrl The Google Sheet URL
     * @return Raw sheet data as List of List of Objects
     * @throws IOException if authentication fails or sheet cannot be read
     */
    public List<List<Object>> readSheetData(@Nonnull String googleSheetUrl) throws IOException {
        validateServiceAccountFile();

        String spreadsheetId = inputSourceResolver.extractSpreadsheetId(googleSheetUrl);
        LOGGER.debug("Reading Google Sheet with ID: {} using service account", spreadsheetId);

        try {
            Sheets service = createSheetsService();
            ValueRange response = service.spreadsheets()
                    .values()
                    .get(spreadsheetId, "A:Z") // Read all columns, first sheet
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                LOGGER.warn("No data found in Google Sheet");
                return Collections.emptyList();
            }

            return values;
        } catch (Exception e) {
            LOGGER.error("Failed to read Google Sheet: {}", e.getMessage(), e);
            if (e.getMessage().contains("authentication")
                    || e.getMessage().contains("credentials")
                    || e.getMessage().contains("permission")
                    || e.getMessage().contains("access")) {
                throw new IOException("Google Sheets authentication failed: " + e.getMessage(), e);
            }
            throw new IOException("Failed to read Google Sheet: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if the service account is properly configured and available.
     *
     * @return true if credentials file exists and is readable
     */
    public boolean isServiceAccountAvailable() {
        try {
            String path = getServiceAccountPath();
            LOGGER.info("Checking service account availability at path: {}", path);
            Path credentialsPath = Paths.get(path);
            boolean exists = Files.exists(credentialsPath);
            boolean readable = Files.isReadable(credentialsPath);
            LOGGER.info("Service account file exists: {}, readable: {}", exists, readable);

            if (!exists) {
                LOGGER.error("Service account file does not exist at path: {}", path);
            }
            if (!readable) {
                LOGGER.error("Service account file is not readable at path: {}", path);
            }

            // Additional debugging
            try {
                LOGGER.info("Parent directory exists: {}", Files.exists(credentialsPath.getParent()));
                LOGGER.info("Is directory: {}", Files.isDirectory(credentialsPath.getParent()));
            } catch (Exception dirException) {
                LOGGER.error("Error checking parent directory: {}", dirException.getMessage());
            }

            return exists && readable;
        } catch (Exception e) {
            LOGGER.error("Exception while checking service account file availability: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Validates that the service account JSON file exists and is readable.
     */
    private void validateServiceAccountFile() throws IOException {
        String path = getServiceAccountPath();
        Path credentialsPath = Paths.get(path);
        if (!Files.exists(credentialsPath)) {
            throw new IOException("Service account file not found at: " + path);
        }
        if (!Files.isReadable(credentialsPath)) {
            throw new IOException("Service account file is not readable: " + path);
        }
    }

    /**
     * Creates an authenticated Sheets service instance.
     */
    private Sheets createSheetsService() throws Exception {
        String path = getServiceAccountPath();
        GoogleCredentials credentials =
                ServiceAccountCredentials.fromStream(new FileInputStream(path)).createScoped(SCOPES);

        return new Sheets.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance(),
                        new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
