package com.redhat.sast.api.util.input;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Utility class for converting raw data to CSV format.
 */
@ApplicationScoped
public class CsvConverter {

    /**
     * Converts a list of lists (raw sheet data) to CSV format.
     *
     * @param values Raw sheet data as List of List of Objects
     * @return CSV content as string
     */
    public String convert(List<List<Object>> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }

        return values.stream()
                .map(row -> row.stream()
                        .map(cell -> cell != null ? cell.toString() : "")
                        .map(this::escapeCsvValue)
                        .collect(Collectors.joining(",")))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Escapes CSV values by wrapping in quotes if they contain commas, quotes, or newlines.
     *
     * @param value The value to escape
     * @return Escaped CSV value
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
