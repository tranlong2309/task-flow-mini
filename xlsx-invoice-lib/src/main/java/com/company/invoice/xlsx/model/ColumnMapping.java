package com.company.invoice.xlsx.model;

import java.util.Map;

/** Immutable mapping of required headers to zero-based columns and Excel letters. */
public record ColumnMapping(String sheetName, int headerRowNumber, Map<String, Integer> columnIndexes,
                            Map<String, String> columnLetters, java.util.List<String> ignoredColumns) {
    /**
     * Creates an immutable detected-column mapping.
     * @param sheetName processed sheet name
     * @param headerRowNumber physical header row number
     * @param columnIndexes required-header zero-based indexes
     * @param columnLetters required-header Excel letters
     * @param ignoredColumns additional header names
     */
    public ColumnMapping {
        columnIndexes = Map.copyOf(columnIndexes);
        columnLetters = Map.copyOf(columnLetters);
        ignoredColumns = java.util.List.copyOf(ignoredColumns);
    }
}
