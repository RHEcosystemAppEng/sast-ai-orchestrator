package com.redhat.sast.api.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class GoogleSheetsService {

    private static final Logger LOG = Logger.getLogger(GoogleSheetsService.class);
    private static final String APPLICATION_NAME = "SAST-AI-Orchestrator";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    @ConfigProperty(name = "google.service.account.secret.path")
    String serviceAccountSecretPath;

    @Inject
    InputSourceResolver inputSourceResolver;

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
        LOG.debugf("Reading Google Sheet with ID: %s using service account", spreadsheetId);

        try {
            Sheets service = createSheetsService();
            ValueRange response = service.spreadsheets()
                    .values()
                    .get(spreadsheetId, "A:Z") // Read all columns, first sheet
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                LOG.warn("No data found in Google Sheet");
                return Collections.emptyList();
            }

            return values;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to read Google Sheet: %s", e.getMessage());
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
            Path credentialsPath = Paths.get(serviceAccountSecretPath);
            return Files.exists(credentialsPath) && Files.isReadable(credentialsPath);
        } catch (Exception e) {
            LOG.debugf("Service account file not available: %s", e.getMessage());
            return false;
        }
    }

    /**
     * Validates that the service account JSON file exists and is readable.
     */
    private void validateServiceAccountFile() throws IOException {
        Path credentialsPath = Paths.get(serviceAccountSecretPath);
        if (!Files.exists(credentialsPath)) {
            throw new IOException("Service account file not found at: " + serviceAccountSecretPath);
        }
        if (!Files.isReadable(credentialsPath)) {
            throw new IOException("Service account file is not readable: " + serviceAccountSecretPath);
        }
    }

    /**
     * Creates an authenticated Sheets service instance.
     */
    private Sheets createSheetsService() throws Exception {
        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(
                        new FileInputStream(serviceAccountSecretPath))
                .createScoped(SCOPES);

        return new Sheets.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance(),
                        new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
