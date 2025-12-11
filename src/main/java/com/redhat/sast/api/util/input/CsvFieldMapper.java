package com.redhat.sast.api.util.input;

import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

import com.redhat.sast.api.common.constants.ApplicationConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for mapping CSV fields with their various naming variations.
 */
@Slf4j
public class CsvFieldMapper {

    // Field name variations mapped to their standardized names
    private static final Map<String, List<String>> FIELD_VARIATIONS = Map.of(
            "nvr", List.of("nvr"),
            "googleSheetUrl",
                    List.of("googlesheeturl", "gsheeturl", "google_sheet_url", "inputsourceurl", "input_source_url"),
            "jiraLink", List.of("jiralink", "jira_link"),
            "hostname", List.of("hostname"),
            "oshScanId", List.of("oshscanid", "osh_scan_id"));

    /**
     * Gets a field value from a CSV record using multiple possible field name variations.
     *
     * @param record the CSV record to extract from
     * @param headerMap the header-to-index mapping
     * @param fieldName the standardized field name
     * @return the field value or null if not found
     */
    public static String getFieldValue(CSVRecord record, Map<String, Integer> headerMap, String fieldName) {
        List<String> variations = FIELD_VARIATIONS.get(fieldName);
        if (variations == null) {
            LOGGER.warn("Unknown field name: {}", fieldName);
            return null;
        }

        for (String variation : variations) {
            Integer index = headerMap.get(variation);
            if (index != null && index < record.size()) {
                String value = record.get(index);
                if (ApplicationConstants.IS_NOT_NULL_AND_NOT_BLANK.test(value)) {
                    return value.trim();
                }
            }
        }

        return null;
    }

    /**
     * Gets all possible field variations for a given field name.
     *
     * @param fieldName the standardized field name
     * @return list of possible variations
     */
    public static List<String> getFieldVariations(String fieldName) {
        return FIELD_VARIATIONS.getOrDefault(fieldName, List.of());
    }

    /**
     * Gets all required header variations that must be present in CSV.
     *
     * @return list of required header variations
     */
    public static List<String> getRequiredHeaders() {
        return List.of("nvr");
    }

    /**
     * Gets the list of required field names (standardized names).
     *
     * @return list of required field names
     */
    public static List<String> getRequiredFieldNames() {
        return List.of("nvr", "googleSheetUrl");
    }

    /**
     * Gets the required field variations as a map for header validation.
     *
     * @return map of required field names to their variations
     */
    public static Map<String, List<String>> getRequiredFieldVariations() {
        return getRequiredFieldNames().stream()
                .collect(java.util.stream.Collectors.toMap(
                        fieldName -> fieldName, fieldName -> FIELD_VARIATIONS.get(fieldName)));
    }

    /**
     * Checks if all required fields have at least one variation present in the header map.
     *
     * @param headerMap the header-to-index mapping
     * @return true if all required fields have at least one variation present
     */
    public static boolean hasAllRequiredFields(Map<String, Integer> headerMap) {
        for (String fieldName : getRequiredFieldNames()) {
            if (!hasFieldVariation(headerMap, fieldName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if at least one variation of a field is present in the header map.
     *
     * @param headerMap the header-to-index mapping
     * @param fieldName the standardized field name
     * @return true if at least one variation is present
     */
    private static boolean hasFieldVariation(Map<String, Integer> headerMap, String fieldName) {
        List<String> variations = FIELD_VARIATIONS.get(fieldName);
        if (variations == null) {
            return false;
        }

        return variations.stream().anyMatch(headerMap::containsKey);
    }
}
