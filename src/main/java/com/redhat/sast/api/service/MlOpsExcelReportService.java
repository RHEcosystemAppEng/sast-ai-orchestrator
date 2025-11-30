package com.redhat.sast.api.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.model.MlOpsJobIssue;
import com.redhat.sast.api.repository.MlOpsJobIssueRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for downloading and parsing AI analysis Excel reports from S3.
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class MlOpsExcelReportService {

    private final MlOpsJobIssueRepository issueRepository;
    private final MlOpsJobService mlOpsJobService;
    private final S3ClientService s3ClientService;

    private static final String AI_REPORT_SHEET = "AI report";

    /**
     * Downloads Excel report from S3 and parses issues into database.
     *
     * @param jobId MLOps job ID
     * @param pipelineRunId Pipeline run ID
     */
    @Transactional
    public void fetchAndSaveExcelReport(Long jobId, String pipelineRunId) {
        try {
            MlOpsJob job = mlOpsJobService.getJobEntityById(jobId);
            if (job == null) {
                LOGGER.warn("MLOps job {} not found, cannot save Excel report", jobId);
                return;
            }

            // Construct S3 key for Excel file
            // Format: {package_name}_sast_ai_output.xlsx
            String packageName = job.getPackageName();
            if (packageName == null || packageName.isBlank()) {
                LOGGER.warn("Package name is null/empty for job {}, cannot construct Excel S3 key", jobId);
                return;
            }

            String s3Key = constructExcelS3Key(packageName, pipelineRunId);
            String s3FileUrl = s3Key; // Store the S3 path for reference

            LOGGER.info("Fetching Excel report from S3 for job {}: {}", jobId, s3Key);

            // Download Excel file from S3
            byte[] excelBytes = s3ClientService.downloadFileAsBytes(s3Key);
            if (excelBytes == null || excelBytes.length == 0) {
                LOGGER.info("No Excel report found in S3 for job {} - file may not have been uploaded yet", jobId);
                return;
            }

            // Parse Excel and save issues
            List<MlOpsJobIssue> issues = parseExcelReport(excelBytes, job, s3FileUrl);
            if (!issues.isEmpty()) {
                issueRepository.persist(issues);
                LOGGER.info("Successfully saved {} issues from Excel report for MLOps job {}", issues.size(), jobId);
            } else {
                LOGGER.info("No issues found in Excel report for job {}", jobId);
            }

        } catch (Exception e) {
            LOGGER.error("Failed to fetch and save Excel report for MLOps job {}: {}", jobId, e.getMessage(), e);
            // Don't rethrow - Excel report is optional, shouldn't break job completion
        }
    }

    /**
     * Constructs the S3 key for the Excel report file.
     * Format: {package_name}_sast_ai_output.xlsx
     */
    private String constructExcelS3Key(String packageName, String pipelineRunId) {
        // Based on the S3 structure we saw: bucket/pipeline-run-id/{package}_sast_ai_output.xlsx
        return String.format("%s/%s_sast_ai_output.xlsx", pipelineRunId, packageName);
    }

    /**
     * Parses Excel file and extracts issues from "AI report" sheet.
     *
     * Expected columns:
     * 0: Issue ID
     * 1: Issue Name
     * 2: Error
     * 3: Investigation Result
     * 4: Hint
     * 5: Justifications
     * 6: Recommendations
     * 7: Answer Relevancy
     * 8: Context
     */
    private List<MlOpsJobIssue> parseExcelReport(byte[] excelBytes, MlOpsJob job, String s3FileUrl) {
        List<MlOpsJobIssue> issues = new ArrayList<>();

        try (ByteArrayInputStream bis = new ByteArrayInputStream(excelBytes);
                Workbook workbook = new XSSFWorkbook(bis)) {

            Sheet sheet = workbook.getSheet(AI_REPORT_SHEET);
            if (sheet == null) {
                LOGGER.warn("Sheet '{}' not found in Excel report for job {}", AI_REPORT_SHEET, job.getId());
                return issues;
            }

            LOGGER.debug(
                    "Parsing '{}' sheet from Excel report for job {}: {} rows",
                    AI_REPORT_SHEET,
                    job.getId(),
                    sheet.getLastRowNum());

            // Skip header row (row 0)
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                MlOpsJobIssue issue = parseIssueRow(row, job, s3FileUrl);
                if (issue != null) {
                    issues.add(issue);
                }
            }

        } catch (Exception e) {
            LOGGER.error("Failed to parse Excel report for job {}: {}", job.getId(), e.getMessage(), e);
        }

        return issues;
    }

    /**
     * Parses a single row from the AI report sheet into an MlOpsJobIssue.
     * Validates string lengths against database constraints to prevent insertion failures.
     */
    private MlOpsJobIssue parseIssueRow(Row row, MlOpsJob job, String s3FileUrl) {
        try {
            String issueId = getCellValueAsString(row.getCell(0));
            if (issueId == null || issueId.isBlank()) {
                // Skip empty rows
                return null;
            }

            MlOpsJobIssue issue = new MlOpsJobIssue();
            issue.setMlOpsJob(job);
            issue.setIssueId(truncateString(issueId, 50, "issueId", row.getRowNum()));
            issue.setIssueName(truncateString(getCellValueAsString(row.getCell(1)), 100, "issueName", row.getRowNum()));
            // Skip column 2 (Error) - not stored per your request
            issue.setInvestigationResult(
                    truncateString(getCellValueAsString(row.getCell(3)), 50, "investigationResult", row.getRowNum()));
            issue.setHint(getCellValueAsString(row.getCell(4))); // TEXT column - no limit
            // Skip columns 5-6 (Justifications, Recommendations) - not stored per your request
            issue.setAnswerRelevancy(
                    truncateString(getCellValueAsString(row.getCell(7)), 20, "answerRelevancy", row.getRowNum()));
            // Skip column 8 (Context) - not stored per your request
            issue.setS3FileUrl(s3FileUrl); // TEXT column - no limit

            return issue;

        } catch (Exception e) {
            LOGGER.warn("Failed to parse issue row {} for job {}: {}", row.getRowNum(), job.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * Truncates a string to the specified maximum length.
     * Logs a warning if truncation occurs.
     *
     * @param value the string to truncate
     * @param maxLength the maximum allowed length
     * @param fieldName the field name for logging
     * @param rowNum the row number for logging
     * @return truncated string or null if input is null
     */
    private String truncateString(String value, int maxLength, String fieldName, int rowNum) {
        if (value == null) {
            return null;
        }
        if (value.length() > maxLength) {
            LOGGER.warn(
                    "Truncating {} from {} to {} characters at row {}: '{}'",
                    fieldName,
                    value.length(),
                    maxLength,
                    rowNum,
                    value.substring(0, Math.min(50, value.length())) + "...");
            return value.substring(0, maxLength);
        }
        return value;
    }

    /**
     * Safely extracts cell value as String.
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}
