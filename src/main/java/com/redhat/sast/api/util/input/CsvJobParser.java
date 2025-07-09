package com.redhat.sast.api.util.input;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jboss.logging.Logger;

import com.redhat.sast.api.common.constants.ApplicationConstants;
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
                throw new IOException(
                        "Could not find a valid header row. Required columns: "
                                + "'nvr' and at least one of ['googlesheeturl', 'gsheeturl', 'google_sheet_url', 'inputsourceurl', 'input_source_url']");
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

            LOG.infof("Successfully parsed %d jobs from CSV content", jobs.size());
            return jobs;
        } catch (IOException e) {
            LOG.error("Failed to parse CSV content", e);
            throw new IOException("Failed to parse CSV content: " + e.getMessage(), e);
        }
    }

    private int findHeaderRow(List<CSVRecord> records) {
        for (int i = 0; i < records.size(); i++) {
            Map<String, Integer> headerMap = buildHeaderMap(records.get(i));

            if (CsvFieldMapper.hasAllRequiredFields(headerMap)) {
                LOG.infof("Found valid header at row %d", i + 1);
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

        setBasicFields(job, record, headerMap);
        setInferredFields(job);
        setInputSource(job, record, headerMap);
        setWorkflowSettings(job);

        validateRequiredFields(job, record.getRecordNumber());
        return job;
    }

    private void setBasicFields(JobCreationDto job, CSVRecord record, Map<String, Integer> headerMap) {
        job.setPackageNvr(CsvFieldMapper.getFieldValue(record, headerMap, "nvr"));
        job.setJiraLink(CsvFieldMapper.getFieldValue(record, headerMap, "jiraLink"));
        job.setHostname(CsvFieldMapper.getFieldValue(record, headerMap, "hostname"));
        job.setOshScanId(CsvFieldMapper.getFieldValue(record, headerMap, "oshScanId"));
    }

    private void setInferredFields(JobCreationDto job) {
        job.setProjectName(urlInferenceService.inferProjectName(job.getPackageNvr()));
        job.setProjectVersion(urlInferenceService.inferProjectVersion(job.getPackageNvr()));
        job.setPackageName(urlInferenceService.inferPackageName(job.getPackageNvr()));
        job.setPackageSourceCodeUrl(urlInferenceService.inferSourceCodeUrl(job.getPackageNvr()));
        job.setKnownFalsePositivesUrl(urlInferenceService.inferKnownFalsePositivesUrl(job.getPackageNvr()));
    }

    private void setInputSource(JobCreationDto job, CSVRecord record, Map<String, Integer> headerMap) {
        String googleSheetUrl = CsvFieldMapper.getFieldValue(record, headerMap, "googleSheetUrl");

        job.setInputSource(new InputSourceDto(
                InputSourceType.GOOGLE_SHEET,
                Optional.ofNullable(googleSheetUrl).orElse(job.getPackageSourceCodeUrl())));
    }

    private void setWorkflowSettings(JobCreationDto job) {
        WorkflowSettingsDto workflowSettings = new WorkflowSettingsDto();
        workflowSettings.setSecretName(ApplicationConstants.DEFAULT_SECRET_NAME);
        job.setWorkflowSettings(workflowSettings);
    }

    private void validateRequiredFields(JobCreationDto job, long recordNumber) {
        if (job.getPackageNvr() == null || job.getPackageNvr().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Record %d is missing required field 'nvr'", recordNumber));
        }

        // Verify that inference was successful
        if (job.getPackageName() == null || job.getProjectName() == null || job.getProjectVersion() == null) {
            throw new IllegalArgumentException(String.format(
                    "Record %d has invalid NVR '%s' - failed to infer parameters", recordNumber, job.getPackageNvr()));
        }
    }

    private boolean isRecordEmpty(CSVRecord record) {
        return record.stream().allMatch(field -> field == null || field.trim().isEmpty());
    }
}
