package com.company.invoice.xlsx.model;

import com.company.invoice.xlsx.error.ErrorCode;

/** Immutable diagnostic for one validation problem in an input row. */
public record RowError(
        String sheetName,
        int rowNumber,
        String cellAddress,
        String columnName,
        String offendingValue,
        ErrorCode errorCode,
        String message) {
        /**
         * Creates an immutable row diagnostic.
         * @param sheetName source sheet
         * @param rowNumber physical Excel row number
         * @param cellAddress source cell address, or null for row-level errors
         * @param columnName required column name
         * @param offendingValue source value
         * @param errorCode stable error code
         * @param message human-readable diagnostic
         */
    public RowError {
        if (sheetName == null || columnName == null || errorCode == null || message == null) {
            throw new NullPointerException("row error fields must not be null");
        }
    }
}
