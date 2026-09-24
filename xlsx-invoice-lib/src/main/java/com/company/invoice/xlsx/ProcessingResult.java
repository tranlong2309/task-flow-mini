package com.company.invoice.xlsx;

import java.util.List;

/** Bounded-memory result of a workbook processing call. */
public record ProcessingResult(InvoiceTotals totals, long dataRowCount, long processedRowCount,
                               long skippedRowCount, List<RowError> errors, boolean errorsTruncated,
                               String sheetName, int headerRowNumber, ColumnMapping columnMapping) {
    /**
     * Creates an immutable bounded processing result.
     * @param totals invoice totals
     * @param dataRowCount source data-row count
     * @param processedRowCount valid output-row count
     * @param skippedRowCount skipped source-row count
     * @param errors retained row errors
     * @param errorsTruncated whether additional errors were omitted
     * @param sheetName processed sheet name
     * @param headerRowNumber physical header row number
     * @param columnMapping detected required-column mapping
     */
    public ProcessingResult {
        errors = List.copyOf(errors);
    }

    /** @return true when at least one row error was reported or truncated */
    public boolean hasErrors() { return !errors.isEmpty() || errorsTruncated; }

    /**
     * Converts collected row errors to one exception after processing has completed.
     * @throws ExcelValidationException when this result has errors
     */
    public void throwIfHasErrors() {
        if (hasErrors()) throw new ExcelValidationException(errors);
    }
}
