package com.company.invoice.xlsx;

import java.util.Optional;

/** 
 * Outcome of a single job within a batch. 
 * @param job the submitted job
 * @param result the successful processing result, if any
 * @param error the failure exception, if any
 */
public record BatchItemResult(BatchJob job, Optional<ProcessingResult> result, Optional<InvoiceException> error) {
    /** 
     * Validates that exactly one outcome is present. 
     * @param job the submitted job
     * @param result the successful processing result
     * @param error the failure exception
     */
    public BatchItemResult {
        if (result.isPresent() == error.isPresent()) {
            throw new IllegalArgumentException("Exactly one of result or error must be present");
        }
    }

    /** @return true if the job succeeded without throwing an exception */
    public boolean succeeded() {
        return result.isPresent();
    }
}
