package com.redhat.sast.api.service;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jboss.logging.Logger;

import com.redhat.sast.api.enums.InputSourceType;
import com.redhat.sast.api.v1.dto.request.InputSourceDto;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;
import com.redhat.sast.api.v1.dto.request.WorkflowSettingsDto;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service for processing batch input sources
 * and converting them into job creation requests.
 */
@ApplicationScoped
public class BatchInputService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    private static final Logger LOG = Logger.getLogger(BatchInputService.class);
    private static final Pattern SHEET_ID_PATTERN = Pattern.compile("/spreadsheets/d/([a-zA-Z0-9-_]+)");
    private static final int NO_VALID_BATCH_TABLE_FOUND = -1;

    /**
     * Processes input source URL and converts to appropriate data format
     * @param gSheetUrl Original Google Sheet input source URL (batch submission)
     * @return CSV export URL
     * @throws IOException if URL is invalid or if unsupported input type is detected
     */
    public String processInputSource(String gSheetUrl) throws IOException {
        try {
            return convertGoogleSheetsToCsv(gSheetUrl);
        } catch (Exception e) {
            throw new IOException("Failed to process input source URL: " + e.getMessage(), e);
        }
    }

    /**
     * Converts Google Sheets URL to CSV export URL
     * @param sheetsUrl Google Sheets URL
     * @return CSV export URL
     * @throws IOException if URL format is invalid
     */
    private String convertGoogleSheetsToCsv(String sheetsUrl) throws IOException {
        // Extract sheet ID
        Matcher sheetIdMatcher = SHEET_ID_PATTERN.matcher(sheetsUrl);
        if (!sheetIdMatcher.find()) {
            throw new IOException("Invalid Google Sheets URL format. URL: " + sheetsUrl);
        }

        String sheetId = sheetIdMatcher.group(1);

        // Construct CSV export URL
        String csvUrl = String.format("https://docs.google.com/spreadsheets/d/%s/export?format=csv", sheetId);
        return csvUrl;
    }

    /**
     * Fetches input data from the processed URL
     * @param csvUrl CSV export URL for Google Sheets original URL (batch submission)
     * @return Raw input data content
     * @throws IOException if fetching fails
     */
    public String fetchInputData(String csvUrl) throws IOException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(csvUrl))
                    .header("User-Agent", "SAST-AI-Orchestrator/1.0")
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                String errorMessage;
                switch (response.statusCode()) {
                    case 401:
                        errorMessage = "Access denied (401). Please ensure the source is accessible.";
                        break;
                    case 403:
                        errorMessage = "Access forbidden (403). The source may be private or have restricted access.";
                        break;
                    default:
                        errorMessage = "Failed to fetch input data. Status code: " + response.statusCode()
                                + ". Please verify the URL is correct and the source is accessible.";
                        break;
                }
                throw new IOException(errorMessage + " URL: " + csvUrl);
            }

            String inputContent = response.body();

            LOG.infof("Successfully fetched input data. Content length: %d characters", inputContent.length());
            return inputContent;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to fetch input data from URL: %s", csvUrl);
            throw new IOException("Failed to fetch input data: " + e.getMessage(), e);
        }
    }

    /**
     * Parses input content into JobCreationDto objects with table detection
     * @param csvContent CSV content of Google Sheets original URL (batch submission)
     * @return List of job creation DTOs
     */
    public List<JobCreationDto> parse(String csvContent) throws IOException {
        List<JobCreationDto> jobs = new ArrayList<>();

        try {
            String[] lines = csvContent.split("\n");
            int tableStartRow = findTableStartRow(lines);

            if (tableStartRow == NO_VALID_BATCH_TABLE_FOUND) {
                throw new IOException(
                        "Could not find a valid table with recognizable column headers in the input content");
            }

            LOG.infof("Found table starting at row %d", tableStartRow + 1);

            // Create a new input content starting from the detected table
            StringBuilder tableContent = new StringBuilder();
            for (int i = tableStartRow; i < lines.length; i++) {
                tableContent.append(lines[i]).append("\n");
            }

            // Parse the extracted table content
            try (CSVParser parser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .setSkipHeaderRecord(false)
                    .build()
                    .parse(new StringReader(tableContent.toString()))) {

                LOG.infof("Table headers found: %s", parser.getHeaderNames());

                for (CSVRecord record : parser) {
                    try {
                        // Skip empty rows
                        if (isEmptyRecord(record)) {
                            continue;
                        }

                        JobCreationDto job = createJobFromRecord(record);
                        if (job != null) {
                            jobs.add(job);
                       }
                    } catch (Exception e) {
                        LOG.warnf(
                                e,
                                "Failed to parse input record at line %d: %s",
                                record.getRecordNumber(),
                                e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            LOG.errorf(e, "Failed to parse input content");
            throw new IOException("Failed to parse input content: " + e.getMessage(), e);
        }

        LOG.infof("Successfully parsed %d jobs from input content", jobs.size());
        return jobs;
    }

    /**
     * Finds where the actual data table starts in the input lines
     * Expected columns: projectName, packageName, sourceCodeUrl, projectVersion,
     * packageNvr, gSheetUrl, knownFalsePositivesUrl, jiraLink, hostname, oshScanId
     * @param lines Array of input lines
     * @return Row index where table starts, or NO_VALID_BATCH_TABLE_FOUND if not found
     */
    private int findTableStartRow(String[] lines) {
        String[] requiredColumns = {
            "projectName",
            "packageName",
            "sourceCodeUrl",
            "projectVersion",
            "packageNvr",
            "gSheetUrl",
            "knownFalsePositivesUrl",
            "jiraLink",
            "hostname",
            "oshScanId"
        };

        LOG.infof("Looking for table with required columns: %s", String.join(", ", requiredColumns));

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.isEmpty()) {
                continue;
            }

            // Parse this line as potential header
            try {
                List<String> columns = parseInputLine(line);

                // Debug: log what we found in this row
                LOG.infof(
                        "Row %d has %d columns: %s",
                        i + 1,
                        columns.size(),
                        String.join(
                                " | ",
                                columns.stream()
                                        .limit(8) // Show first 8 columns for readability
                                        .toArray(String[]::new)));

                // Check if the line contains all required columns
                int foundRequired = 0;
                for (String column : columns) {
                    String cleanColumn = column.trim();
                    for (String requiredColumn : requiredColumns) {
                        if (cleanColumn.equals(requiredColumn)) {
                            foundRequired++;
                            LOG.infof("Found required column: '%s'", cleanColumn);
                            break;
                        }
                    }
                }

                if (foundRequired == requiredColumns.length) {
                    LOG.infof("FOUND TABLE HEADER at row %d with all %d required columns!", i + 1, foundRequired);
                    return i;
                }

                // Logging partial matches for debugging:
                if (foundRequired > 0) {
                    LOG.infof(
                            "Partial match at row %d: found %d/%d required columns",
                            i + 1, foundRequired, requiredColumns.length);
                }

            } catch (Exception e) {
                LOG.debugf("Could not parse row %d as input line: %s", i + 1, e.getMessage());
                continue;
            }
        }

        LOG.warnf(
                "No valid table header found in %d lines. Required columns: %s",
                lines.length, String.join(", ", requiredColumns));
        return NO_VALID_BATCH_TABLE_FOUND;
    }

    /**
     * Input line parser for header detection
     * @param line Input line to parse
     * @return Array of column values
     */
    private List<String> parseInputLine(String line) throws IOException {

        try (CSVParser parser = CSVParser.parse(line, CSVFormat.DEFAULT)) {
            return parser.getRecords().get(0).toList();
        }
    }

    /**
     * Creates a JobCreationDto from an input record
     * Expected columns: projectName, projectVersion, packageName, packageNvr,
     * sourceCodeUrl, gSheetUrl, knownFalsePositivesUrl, jiraLink, hostname, oshScanId
     */
    private JobCreationDto createJobFromRecord(CSVRecord record) {
        // Skip records with missing required data
        if (record.size() < 3) {
            return null;
        }

        JobCreationDto job = new JobCreationDto();

        // Required fields
        job.setProjectName(getFieldValue(record, "projectName", "project_name", "Project Name"));
        job.setProjectVersion(getFieldValue(record, "projectVersion", "project_version", "Project Version"));
        job.setPackageName(getFieldValue(record, "packageName", "package_name", "Package Name"));
        job.setPackageNvr(getFieldValue(record, "packageNvr", "package_nvr", "Package NVR"));
        job.setPackageSourceCodeUrl(getFieldValue(record, "sourceCodeUrl", "source_code_url", "Source Code URL"));
        job.setJiraLink(getFieldValue(record, "jiraLink", "jira_link", "Jira Link"));
        job.setHostname(getFieldValue(record, "hostname", "Hostname"));
        job.setKnownFalsePositivesUrl(getFieldValue(
                record, "knownFalsePositivesUrl", "known_false_positives_url", "Known False Positives URL"));
        job.setOshScanId(getFieldValue(record, "oshScanId", "osh_scan_id", "OSH Scan ID"));

        // Create input source, default to GOOGLE_SHEET type for batch processing
        String gSheetUrl = getFieldValue(record, "gSheetUrl", "google_sheet_url", "Google Sheet URL");
        if (gSheetUrl == null || gSheetUrl.trim().isEmpty()) {
            gSheetUrl = job.getPackageSourceCodeUrl();
        }

        InputSourceDto inputSource = new InputSourceDto(InputSourceType.GOOGLE_SHEET, gSheetUrl);
        job.setInputSource(inputSource);

        // Create default workflow settings for batch jobs to ensure LLM secrets are loaded
        WorkflowSettingsDto workflowSettings = new WorkflowSettingsDto();
        workflowSettings.setSecretName("sast-ai-default-llm-creds");
        // Model names are optional, if not set they will fallback to secret values
        job.setWorkflowSettings(workflowSettings);

        // Validate required fields
        if (job.getProjectName() == null || job.getPackageName() == null || job.getPackageSourceCodeUrl() == null) {
            throw new IllegalArgumentException(
                    "Missing required fields: projectName, projectVersion, packageName, packageNvr, sourceCodeUrl, gSheetUrl, knownFalsePositivesUrl, jiraLink, hostname, oshScanId are required");
        }

        return job;
    }

    /**
     * Gets field value from CSV record with fallback column names
     */
    private String getFieldValue(CSVRecord record, String... columnNames) {
        for (String columnName : columnNames) {
            try {
                if (record.isMapped(columnName)) {
                    String value = record.get(columnName);
                    if (value != null && !value.trim().isEmpty()) {
                        return value.trim();
                    }
                }
            } catch (IllegalArgumentException e) {
                // Column doesn't exist, try the next one
            }
        }
        return null;
    }

    /**
     * Checks if a record is empty (all fields are null or empty)
     * @param record Input record to check
     * @return true if record is empty
     */
    private boolean isEmptyRecord(CSVRecord record) {
        for (String value : record) {
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
