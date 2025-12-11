package com.redhat.sast.api.util.input;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InputSourceResolver {

    private static final Pattern SHEET_ID_PATTERN = Pattern.compile("/spreadsheets/d/([a-zA-Z0-9-_]+)");

    /**
     * Extracts the spreadsheet ID from a Google Sheets URL.
     *
     * @param googleSheetsUrl The Google Sheets URL to extract from.
     * @return The spreadsheet ID.
     * @throws IllegalArgumentException if the URL format is invalid or sheet ID cannot be extracted.
     */
    public String extractSpreadsheetId(@Nonnull String googleSheetsUrl) throws IllegalArgumentException {
        Matcher sheetIdMatcher = SHEET_ID_PATTERN.matcher(googleSheetsUrl);
        if (!sheetIdMatcher.find()) {
            throw new IllegalArgumentException("Could not extract a valid Sheet ID from the URL -> " + googleSheetsUrl);
        }
        return sheetIdMatcher.group(1);
    }
}
