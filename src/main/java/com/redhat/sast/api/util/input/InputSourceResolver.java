package com.redhat.sast.api.util.input;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.annotation.Nonnull;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InputSourceResolver {

    private static final Pattern SHEET_ID_PATTERN = Pattern.compile("/spreadsheets/d/([a-zA-Z0-9-_]+)");

    /**
     * Takes a user-provided Google Sheets URL and resolves it into a direct,
     * fetchable CSV data URL.
     *
     * @param gSheetUrl The non-null URL of the Google Sheet.
     * @return The direct URL for downloading the sheet as a CSV file.
     * @throws IOException if the URL is null, blank, malformed, or cannot be resolved.
     */
    public String resolve(@Nonnull String gSheetUrl) throws IOException {
        if (gSheetUrl == null || gSheetUrl.isBlank()) {
            throw new IllegalArgumentException("Input URL cannot be null or empty.");
        }

        try {
            return convertGoogleSheetsToCsv(gSheetUrl);
        } catch (Exception e) {
            throw new IOException("Failed to resolve Google Sheets URL: " + gSheetUrl, e);
        }
    }

    private String convertGoogleSheetsToCsv(@Nonnull String sheetsUrl) throws IOException {
        // Extract sheet ID
        Matcher sheetIdMatcher = SHEET_ID_PATTERN.matcher(sheetsUrl);
        if (!sheetIdMatcher.find()) {
            throw new IOException("Could not extract a valid Sheet ID from the URL. Please check the format.");
        }

        String sheetId = sheetIdMatcher.group(1);

        // Construct CSV export URL
        String csvUrl = String.format("https://docs.google.com/spreadsheets/d/%s/export?format=csv", sheetId);
        return csvUrl;
    }
}
