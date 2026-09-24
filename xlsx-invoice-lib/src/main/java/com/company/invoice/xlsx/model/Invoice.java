package com.company.invoice.xlsx.model;

import java.util.List;

/** Immutable in-memory invoice calculation result. */
public record Invoice(List<InvoiceLine> lines, InvoiceTotals totals, List<RowError> errors) {
    /**
     * Creates an immutable invoice result.
     * @param lines calculated invoice lines
     * @param totals invoice totals
     * @param errors row errors collected during calculation
     */
    public Invoice {
        lines = List.copyOf(lines);
        errors = List.copyOf(errors);
    }
}
