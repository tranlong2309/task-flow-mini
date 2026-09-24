package com.company.invoice.xlsx.error;

/** Base unchecked exception for fatal invoice processing failures. */
public class InvoiceException extends RuntimeException {
        /** The optional job identifier associated with this failure. */
        private String jobId;

        /** 
         * Associates this exception with a processing job. 
         * @param value the job identifier
         * @return this exception
         */
        public InvoiceException withJobId(String value) {
            this.jobId = value;
            return this;
        }

        /** @return job id, or null when created outside processing */
        public String getJobId() { return jobId; }
    private static final long serialVersionUID = 1L;

    /**
     * Creates a fatal processing exception.
     * @param message actionable failure description
     */
    public InvoiceException(String message) { super(message); }

    /**
     * Creates a fatal processing exception with its cause.
     * @param message actionable failure description
     * @param cause underlying failure
     */
    public InvoiceException(String message, Throwable cause) { super(message, cause); }
}
