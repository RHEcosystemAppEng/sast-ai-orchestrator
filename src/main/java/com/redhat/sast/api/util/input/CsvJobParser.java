package com.redhat.sast.api.util.input;

import com.redhat.sast.api.enums.InputSourceType;
import com.redhat.sast.api.v1.dto.request.InputSourceDto;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;
import com.redhat.sast.api.v1.dto.request.WorkflowSettingsDto;
import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CsvJobParser {

    private static final Logger LOG = Logger.getLogger(CsvJobParser.class);
    private static final Set<String> REQUIRED_HEADERS = Set.of(
        "projectname", "packagename"
    );

    /**
     * Parses a raw CSV string into a list of JobCreationDto objects.
     * It automatically detects the header row and handles variations in column names.
     *
     * @param csvContent The non-null, raw CSV data as a string.
     * @return A list of parsed jobs.
     * @throws IOException if the CSV is malformed or a valid header cannot be found.
     */
    public List<JobCreationDto> parse(@Nonnull String csvContent) throws IOException {
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
                throw new IOException("Could not find a valid header row. Required columns include: " + String.join(", ", REQUIRED_HEADERS));
            }

            List<JobCreationDto> jobs = new ArrayList<>();
            Map<String, Integer> headerMap = buildHeaderMap(records.get(headerRowIndex));

            for (int i = headerRowIndex + 1; i < records.size(); i++) {
                CSVRecord record = records.get(i);
                if (isRecordEmpty(record)) {
                    continue;
                }

                try {
                    jobs.add(createJobFromRecord(record, headerMap));
                } catch (IllegalArgumentException e) {
                    LOG.warnf("Skipping record at line %d: %s", record.getRecordNumber(), e.getMessage());
                }
            }

            LOG.infof("Successfully parsed %d jobs from input content.", jobs.size());
            return jobs;
        } catch (IOException e) {
            LOG.error("Failed to parse CSV content.", e);
            throw new IOException("Failed to parse CSV content: " + e.getMessage(), e);
        }
    }

    private int findHeaderRow(List<CSVRecord> records) {
        for (int i = 0; i < records.size(); i++) {
            Set<String> currentHeaders = records.get(i).stream()
                .map(header -> header.trim().toLowerCase().replaceAll("\\s+", ""))
                .collect(Collectors.toSet());

            if (currentHeaders.containsAll(REQUIRED_HEADERS)) {
                LOG.infof("Found valid header at row %d.", i + 1);
                return i;
            }
        }
        return -1;
    }

    private Map<String, Integer> buildHeaderMap(CSVRecord headerRecord) {
        Map<String, Integer> headerMap = new java.util.HashMap<>();
        for (int i = 0; i < headerRecord.size(); i++) {
            String header = headerRecord.get(i).trim().toLowerCase().replaceAll("\\s+", "");
            headerMap.put(header, i);
        }
        return headerMap;
    }

    private JobCreationDto createJobFromRecord(CSVRecord record, Map<String, Integer> headerMap) {
        JobCreationDto job = new JobCreationDto();

        job.setProjectName(getFieldValue(record, headerMap, List.of("projectname", "project_name")));
        job.setProjectVersion(getFieldValue(record, headerMap, List.of("projectversion", "project_version")));
        job.setPackageName(getFieldValue(record, headerMap, List.of("packagename", "package_name")));
        job.setPackageNvr(getFieldValue(record, headerMap, List.of("packagenvr", "package_nvr")));
        job.setPackageSourceCodeUrl(getFieldValue(record, headerMap, List.of("sourcecodeurl", "source_code_url")));
        job.setJiraLink(getFieldValue(record, headerMap, List.of("jiralink", "jira_link")));
        job.setHostname(getFieldValue(record, headerMap, List.of("hostname")));
        job.setKnownFalsePositivesUrl(getFieldValue(record, headerMap, List.of("knownfalsepositivesurl", "known_false_positives_url")));
        job.setOshScanId(getFieldValue(record, headerMap, List.of("oshscanid", "osh_scan_id")));

        String gSheetUrl = getFieldValue(record, headerMap, List.of("gsheeturl", "google_sheet_url"));
        job.setInputSource(new InputSourceDto(InputSourceType.GOOGLE_SHEET, Optional.ofNullable(gSheetUrl).orElse(job.getPackageSourceCodeUrl())));
        WorkflowSettingsDto workflowSettings = new WorkflowSettingsDto();
        workflowSettings.setSecretName("sast-ai-default-llm-creds");
        job.setWorkflowSettings(workflowSettings);

        validateRequiredFields(job, record.getRecordNumber());

        return job;
    }

    private String getFieldValue(CSVRecord record, Map<String, Integer> headerMap, List<String> possibleNames) {
        for (String name : possibleNames) {
            if (headerMap.containsKey(name)) {
                int index = headerMap.get(name);
                if (index < record.size()) {
                    String value = record.get(index);
                    if (value != null && !value.trim().isEmpty()) {
                        return value.trim();
                    }
                }
            }
        }
        return null;
    }

    private void validateRequiredFields(JobCreationDto job, long recordNumber) {
        if (job.getProjectName() == null || job.getPackageName() == null || job.getPackageSourceCodeUrl() == null) {
            throw new IllegalArgumentException(String.format(
                "Record %d is missing one or more required fields (projectName, packageName).", recordNumber
            ));
        }
    }

    private boolean isRecordEmpty(CSVRecord record) {
        return record.stream().allMatch(field -> field == null || field.trim().isEmpty());
    }
}