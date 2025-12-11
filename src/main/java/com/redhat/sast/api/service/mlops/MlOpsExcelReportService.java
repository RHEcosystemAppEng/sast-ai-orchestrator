package com.redhat.sast.api.service.mlops;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.redhat.sast.api.model.MlOpsJob;
import com.redhat.sast.api.model.MlOpsJobFinding;
import com.redhat.sast.api.repository.mlops.MlOpsJobFindingRepository;
import com.redhat.sast.api.service.S3ClientService;

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

    private final MlOpsJobFindingRepository findingRepository;
    private final MlOpsJobService mlOpsJobService;
    private final S3ClientService s3ClientService;

    private static final String AI_REPORT_SHEET = "AI report";

    // Excel column indices for AI report sheet
    private static final int COL_FINDING_ID = 0;
    private static final int COL_FINDING_NAME = 1;
    private static final int COL_ERROR = 2; // Not currently stored
    private static final int COL_INVESTIGATION_RESULT = 3;
    private static final int COL_HINT = 4;
    private static final int COL_JUSTIFICATIONS = 5; // Not currently stored
    private static final int COL_RECOMMENDATIONS = 6; // Not currently stored
    private static final int COL_ANSWER_RELEVANCY = 7;
    private static final int COL_CONTEXT = 8; // Not currently stored

    /**
     * Downloads Excel report from S3 and parses findings into database.
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

            String s3FileUrl = constructExcelS3Key(packageName, pipelineRunId);

            LOGGER.debug("Fetching Excel report from S3 for job {}: {}", jobId, s3FileUrl);

            // Download Excel file from S3
            byte[] excelBytes = s3ClientService.downloadFileAsBytes(s3FileUrl);
            if (excelBytes == null || excelBytes.length == 0) {
                LOGGER.info("No Excel report found in S3 for job {} - file may not have been uploaded yet", jobId);
                return;
            }

            // Parse Excel and save findings
            List<MlOpsJobFinding> findings = parseExcelReport(excelBytes, job, s3FileUrl);
            if (findings != null && !findings.isEmpty()) {
                findingRepository.persist(findings);
                LOGGER.info(
                        "Successfully saved {} findings from Excel report for MLOps job {}", findings.size(), jobId);
            } else {
                LOGGER.info("No findings found in Excel report for job {}", jobId);
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
     * Parses Excel file and extracts findings from "AI report" sheet.
     * Expected columns:
     * 0: Finding ID
     * 1: Finding Name
     * 2: Error
     * 3: Investigation Result
     * 4: Hint
     * 5: Justifications
     * 6: Recommendations
     * 7: Answer Relevancy
     * 8: Context
     */
    private List<MlOpsJobFinding> parseExcelReport(byte[] excelBytes, MlOpsJob job, String s3FileUrl) {
        List<MlOpsJobFinding> findings = new ArrayList<>();

        try (ByteArrayInputStream bis = new ByteArrayInputStream(excelBytes);
                Workbook workbook = new XSSFWorkbook(bis)) {

            Sheet sheet = workbook.getSheet(AI_REPORT_SHEET);
            if (sheet == null) {
                LOGGER.warn("Sheet '{}' not found in Excel report for job {}", AI_REPORT_SHEET, job.getId());
                return findings;
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

                MlOpsJobFinding finding = parseFindingRow(row, job, s3FileUrl);
                if (finding != null) {
                    findings.add(finding);
                }
            }

        } catch (Exception e) {
            LOGGER.error("Failed to parse Excel report for job {}: {}", job.getId(), e.getMessage(), e);
        }

        return findings;
    }

    /**
     * Parses a single row from the AI report sheet into an MlOpsJobFinding.
     * Validates string lengths against database constraints to prevent insertion failures.
     * Uses column index constants to make the parsing more maintainable.
     */
    private MlOpsJobFinding parseFindingRow(Row row, MlOpsJob job, String s3FileUrl) {
        try {
            String findingId = getCellValueAsString(row.getCell(COL_FINDING_ID));
            if (findingId == null || findingId.isBlank()) {
                // Skip empty rows
                return null;
            }

            MlOpsJobFinding finding = new MlOpsJobFinding();
            finding.setMlOpsJob(job);
            finding.setFindingId(truncateString(findingId, 50, "findingId", row.getRowNum()));
            finding.setFindingName(truncateString(
                    getCellValueAsString(row.getCell(COL_FINDING_NAME)), 100, "findingName", row.getRowNum()));
            // Skip COL_ERROR - not stored
            finding.setInvestigationResult(truncateString(
                    getCellValueAsString(row.getCell(COL_INVESTIGATION_RESULT)),
                    50,
                    "investigationResult",
                    row.getRowNum()));
            finding.setHint(getCellValueAsString(row.getCell(COL_HINT))); // TEXT column - no limit
            // Skip COL_JUSTIFICATIONS, COL_RECOMMENDATIONS - not stored
            finding.setAnswerRelevancy(truncateString(
                    getCellValueAsString(row.getCell(COL_ANSWER_RELEVANCY)), 20, "answerRelevancy", row.getRowNum()));
            // Skip COL_CONTEXT - not stored
            finding.setS3FileUrl(s3FileUrl); // TEXT column - no limit

            return finding;

        } catch (Exception e) {
            LOGGER.warn("Failed to parse finding row {} for job {}: {}", row.getRowNum(), job.getId(), e.getMessage());
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
