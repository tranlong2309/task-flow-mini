package com.company.invoice.xlsx;

import java.util.Optional;

/** 
 * Optional caller correlation data for one processing job. 
 * @param correlationId the correlation identifier for this job, if any
 */
public record JobContext(Optional<String> correlationId) {
    /** Creates an empty job context. */
    public JobContext() {
        this(Optional.empty());
    }

    /** 
     * Creates a context from an optional correlation id. 
     * @param correlationId the optional correlation identifier
     */
    public JobContext {
        correlationId = correlationId == null ? Optional.empty() : correlationId;
    }

    /** 
     * Creates a context with one correlation id.
     * @param correlationId the correlation identifier
     * @return the created context
     */
    public static JobContext of(String correlationId) {
        return new JobContext(Optional.ofNullable(correlationId));
    }
}