package com.redhat.sast.api.util;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for common JSON parsing operations.
 * Provides defensive parsing methods for extracting values from JsonNode objects.
 */
@UtilityClass
@Slf4j
public class JsonParsingUtils {

    /**
     * Extracts BigDecimal from JSON node, handling null and invalid values.
     * Uses decimalValue() for precise decimal conversion without double precision loss.
     *
     * @param parentNode the parent JSON node
     * @param fieldName the field name to extract
     * @return the BigDecimal value, or null if missing/invalid
     */
    public static BigDecimal extractBigDecimal(JsonNode parentNode, String fieldName) {
        JsonNode fieldNode = parentNode.path(fieldName);
        if (fieldNode.isMissingNode() || fieldNode.isNull() || !fieldNode.isNumber()) {
            if (!fieldNode.isMissingNode() && !fieldNode.isNull() && !fieldNode.isNumber()) {
                LOGGER.warn(
                        "Invalid BigDecimal value for field '{}': {}. Defaulting to null.",
                        fieldName,
                        fieldNode.asText());
            }
            return null;
        }
        try {
            return fieldNode.decimalValue();
        } catch (NumberFormatException e) {
            LOGGER.warn("Failed to parse {} as BigDecimal: {}. Defaulting to null.", fieldName, e.getMessage());
            return null;
        }
    }
}
