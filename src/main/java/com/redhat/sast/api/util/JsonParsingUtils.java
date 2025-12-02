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

    /**
     * Extracts a percentage value as BigDecimal from JSON node, with null-safe defaults.
     * Returns BigDecimal.ZERO for missing/invalid values, clamped to [0,1] range, scale 4.
     *
     * @param parentNode the parent JSON node
     * @param fieldName the field name to extract
     * @return the percentage value (0.0000-1.0000), never null
     */
    public static BigDecimal extractPercentageOrZero(JsonNode parentNode, String fieldName) {
        BigDecimal value = extractBigDecimal(parentNode, fieldName);
        if (value == null) {
            LOGGER.debug("Missing or invalid percentage for '{}', defaulting to 0.0000", fieldName);
            return BigDecimal.ZERO.setScale(4, java.math.RoundingMode.HALF_UP);
        }

        // Clamp to [0, 1] range
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            LOGGER.warn("Percentage '{}' is negative ({}), clamping to 0.0000", fieldName, value);
            return BigDecimal.ZERO.setScale(4, java.math.RoundingMode.HALF_UP);
        }
        if (value.compareTo(BigDecimal.ONE) > 0) {
            LOGGER.warn("Percentage '{}' exceeds 1.0 ({}), clamping to 1.0000", fieldName, value);
            return BigDecimal.ONE.setScale(4, java.math.RoundingMode.HALF_UP);
        }

        return value.setScale(4, java.math.RoundingMode.HALF_UP);
    }
}
