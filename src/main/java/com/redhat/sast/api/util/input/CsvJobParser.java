package com.redhat.sast.api.util.input;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.redhat.sast.api.service.NvrResolutionService;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class CsvJobParser {

    @Inject
    NvrResolutionService nvrResolutionService;

    /**
     * Parses a raw CSV string into a list of JobCreationDto objects.
     * It automatically detects the header row and handles variations in column names.
     *
     * @param csvContent The non-null, raw CSV data as a string.
     * @return A list of parsed jobs.
     * @throws IOException if the CSV is malformed or a valid header cannot be found.
     */
    public List<JobCreationDto> parse(@Nonnull String csvContent) throws IOException {
        return parse(csvContent, null);
    }

    /**
     * Parses a raw CSV string into a list of JobCreationDto objects with custom workflow settings.
     * It automatically detects the header row and handles variations in column names.
     *
     * @param csvContent The non-null, raw CSV data as a string.
     * @param useKnownFalsePositiveFile Whether to use known false positives file (null defaults to true)
     * @return A list of parsed jobs.
     * @throws IOException if the CSV is malformed or a valid header cannot be found.
     */
    public List<JobCreationDto> parse(@Nonnull String csvContent, Boolean useKnownFalsePositiveFile)
            throws IOException {
        if (csvContent == null || csvContent.isBlank()) {
            return new ArrayList<>();
        }

        try (CSVParser parser = CSVParser.parse(new StringReader(csvContent), CSVFormat.DEFAULT)) {
            List<CSVRecord> records = parser.getRecords();
            if (records.isEmpty()) {
                return new ArrayList<>();
            }

            int headerRowIndex = findHeaderRow(records);
            if (headerRowIndex == -1) {
                LOGGER.error("Header detection failed. Raw content was:\n{}", csvContent);
                String requiredHeaders = CsvFieldMapper.getRequiredFieldVariations().entrySet().stream()
                        .map(entry -> "'" + entry.getKey() + "' (variations: " + entry.getValue() + ")")
                        .reduce((a, b) -> a + " and " + b)
                        .orElse("none");
                throw new IOException("Could not find a valid header row. Required columns: " + requiredHeaders);
            }

            CSVRecord headerRecord = records.get(headerRowIndex);

            List<JobCreationDto> jobs = IntStream.range(headerRowIndex + 1, records.size())
                    .mapToObj(records::get)
                    .filter(Predicate.not(this::isRecordEmpty))
                    .map(record -> {
                        try {
                            return createJobFromRecord(record, headerRecord, useKnownFalsePositiveFile);
                        } catch (IllegalArgumentException e) {
                            LOGGER.warn("Skipping record at line {}: {}", record.getRecordNumber(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(job -> job != null)
                    .collect(Collectors.toList());

            LOGGER.info("Successfully parsed {} jobs from CSV content", jobs.size());
            return jobs;
        } catch (IOException e) {
            LOGGER.error("Failed to parse CSV content", e);
            throw new IOException("Failed to parse CSV content: " + e.getMessage(), e);
        }
    }

    private int findHeaderRow(List<CSVRecord> records) {
        for (int i = 0; i < records.size(); i++) {
            CSVRecord record = records.get(i);

            if (hasRequiredColumns(record)) {
                LOGGER.debug("Found valid header at row {}", i + 1);
                return i;
            }
        }
        return -1;
    }

    private boolean hasRequiredColumns(CSVRecord record) {
        Map<String, Boolean> foundHeaders = new HashMap<>();

        // Initialize all required headers as not found
        for (String headerKey : CsvFieldMapper.getRequiredFieldVariations().keySet()) {
            foundHeaders.put(headerKey, false);
        }

        // Check each column in the record
        for (int i = 0; i < record.size(); i++) {
            String header = record.get(i).trim().toLowerCase().replaceAll("\\s+", "");

            // Check against all required headers and their variations
            for (Map.Entry<String, List<String>> entry :
                    CsvFieldMapper.getRequiredFieldVariations().entrySet()) {
                if (entry.getValue().contains(header)) {
                    foundHeaders.put(entry.getKey(), true);
                    break;
                }
            }
        }

        // All required headers must be found
        return foundHeaders.values().stream().allMatch(Boolean::booleanValue);
    }

    private JobCreationDto createJobFromRecord(
            CSVRecord record, CSVRecord headerRecord, Boolean useKnownFalsePositiveFile) {
        int nvrIndex = findColumnIndex(headerRecord, "nvr");
        int googleSheetIndex = findColumnIndex(headerRecord, "googleSheetUrl");

        String packageNvr = getFieldValue(record, nvrIndex);
        String googleSheetUrl = getFieldValue(record, googleSheetIndex);

        JobCreationDto job = new JobCreationDto(packageNvr, googleSheetUrl);
        job.setUseKnownFalsePositiveFile(useKnownFalsePositiveFile);

        validateRequiredFields(job, record.getRecordNumber());
        return job;
    }

    private int findColumnIndex(CSVRecord headerRecord, String targetField) {
        List<String> fieldVariations = CsvFieldMapper.getFieldVariations(targetField);
        if (fieldVariations.isEmpty()) {
            return -1;
        }

        for (int i = 0; i < headerRecord.size(); i++) {
            String header = headerRecord.get(i).trim().toLowerCase().replaceAll("\\s+", "");
            if (fieldVariations.contains(header)) {
                return i;
            }
        }
        return -1;
    }

    private String getFieldValue(CSVRecord record, int index) {
        if (index >= 0 && index < record.size()) {
            String value = record.get(index);
            return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
        }
        return null;
    }

    private void validateRequiredFields(JobCreationDto job, long recordNumber) {
        if (job.getPackageNvr() == null || job.getPackageNvr().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Record %d is missing required field 'nvr'", recordNumber));
        }

        if (job.getInputSourceUrl() == null || job.getInputSourceUrl().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Record %d is missing required field 'googleSheetUrl'", recordNumber));
        }

        // Verify that NVR is valid for inference
        if (!nvrResolutionService.isValidNvr(job.getPackageNvr())) {
            throw new IllegalArgumentException(String.format(
                    "Record %d has invalid NVR '%s' - cannot infer parameters", recordNumber, job.getPackageNvr()));
        }
    }

    private boolean isRecordEmpty(CSVRecord record) {
        return record.stream().allMatch(field -> field == null || field.trim().isEmpty());
    }
}
