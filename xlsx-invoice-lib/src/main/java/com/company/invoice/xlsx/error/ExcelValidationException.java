package com.company.invoice.xlsx.error;

import java.util.List;
import com.company.invoice.xlsx.model.Invoice;
import com.company.invoice.xlsx.model.RowError;

/** Optional exception view of row errors collected during processing. */
public final class ExcelValidationException extends InvoiceException {
    private static final long serialVersionUID = 1L;

    /** The row errors carried by this exception. */
    private final List<RowError> errors;

    /**
     * Creates an exception containing collected row errors.
     * @param errors immutable row-error list
     */
    public ExcelValidationException(List<RowError> errors) {
        super("Invoice contains " + errors.size() + " reported row error(s)");
        this.errors = List.copyOf(errors);
    }

    /** @return immutable row-error list */
    public List<RowError> errors() { return errors; }
}
