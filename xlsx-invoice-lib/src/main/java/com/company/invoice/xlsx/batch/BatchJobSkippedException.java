package com.company.invoice.xlsx.batch;

import com.company.invoice.xlsx.error.InvoiceException;

/** Exception indicating a batch job was skipped because stopOnFirstFailure was true and a previous job failed. */
public class BatchJobSkippedException extends InvoiceException {
    /** 
     * Creates a new skipped exception.
     * @param message the detail message
     */
    public BatchJobSkippedException(String message) {
        super(message);
    }
}
