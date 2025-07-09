package com.redhat.sast.api.util.input;

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

import com.redhat.sast.api.enums.InputSourceType;
import com.redhat.sast.api.service.UrlInferenceService;
import com.redhat.sast.api.v1.dto.request.InputSourceDto;
import com.redhat.sast.api.v1.dto.request.JobCreationDto;
import com.redhat.sast.api.v1.dto.request.WorkflowSettingsDto;

import jakarta.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CsvJobParser {

    private static final Logger LOG = Logger.getLogger(CsvJobParser.class);
    private static final Set<String> REQUIRED_HEADERS = Set.of("nvr");

    @Inject
    UrlInferenceService urlInferenceService;

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
                LOG.errorf("Header detection failed. Raw content was:\n%s", csvContent);
                throw new IOException("Could not find a valid header row. Required columns include: "
                        + String.join(", ", REQUIRED_HEADERS));
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

        job.setPackageNvr(getFieldValue(record, headerMap, List.of("nvr", "packageNvr", "packagenvr", "package_nvr")));
        
        job.setProjectName(urlInferenceService.inferProjectName(job.getPackageNvr()));
        job.setProjectVersion(urlInferenceService.inferProjectVersion(job.getPackageNvr()));
        job.setPackageName(urlInferenceService.inferPackageName(job.getPackageNvr()));
        job.setPackageSourceCodeUrl(urlInferenceService.inferSourceCodeUrl(job.getPackageNvr()));
        job.setKnownFalsePositivesUrl(urlInferenceService.inferKnownFalsePositivesUrl(job.getPackageNvr()));
        job.setJiraLink(getFieldValue(record, headerMap, List.of("jiraLink", "jiralink", "jira_link")));
        job.setHostname(getFieldValue(record, headerMap, List.of("hostname")));
        job.setOshScanId(getFieldValue(record, headerMap, List.of("oshScanId", "oshscanid", "osh_scan_id")));
        String gSheetUrl = getFieldValue(
                record,
                headerMap,
                List.of(
                        "gSheetUrl",
                        "googlesheeturl",
                        "gsheeturl",
                        "googleSheetUrl",
                        "google_sheet_url",
                        "inputSourceUrl",
                        "inputsourceurl",
                        "input_source_url"));

        LOG.infof("Parsed gSheetUrl for job: '%s'", gSheetUrl);
        LOG.infof("Available headers: %s", headerMap.keySet());

        job.setInputSource(new InputSourceDto(
                InputSourceType.GOOGLE_SHEET, Optional.ofNullable(gSheetUrl).orElse(job.getPackageSourceCodeUrl())));
        WorkflowSettingsDto workflowSettings = new WorkflowSettingsDto();
        workflowSettings.setSecretName("sast-ai-default-llm-creds");
        job.setWorkflowSettings(workflowSettings);

        LOG.infof("Set workflow settings with secretName: '%s'", workflowSettings.getSecretName());

        validateRequiredFields(job, record.getRecordNumber());

        return job;
    }

    private String getFieldValue(CSVRecord record, Map<String, Integer> headerMap, List<String> possibleNames) {
        LOG.infof("Looking for field in possible names: %s", possibleNames);
        for (String name : possibleNames) {
            if (headerMap.containsKey(name)) {
                int index = headerMap.get(name);
                if (index < record.size()) {
                    String value = record.get(index);
                    if (value != null && !value.trim().isEmpty()) {
                        LOG.infof("Found field '%s' with value: '%s'", name, value.trim());
                        return value.trim();
                    }
                }
            }
        }
        LOG.infof("No field found for possible names: %s", possibleNames);
        return null;
    }

    private void validateRequiredFields(JobCreationDto job, long recordNumber) {
        if (job.getPackageNvr() == null || job.getPackageNvr().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Record %d is missing required field 'nvr'.", recordNumber));
        }
        
        // Verify that inference was successful
        if (job.getPackageName() == null || job.getProjectName() == null || job.getProjectVersion() == null) {
            throw new IllegalArgumentException(
                    String.format("Record %d has invalid NVR '%s' - failed to infer parameters.", recordNumber, job.getPackageNvr()));
        }
    }

    private boolean isRecordEmpty(CSVRecord record) {
        return record.stream().allMatch(field -> field == null || field.trim().isEmpty());
    }
}
