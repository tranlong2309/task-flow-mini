package com.company.invoice.xlsx;

/** Base unchecked exception for fatal invoice processing failures. */
public class InvoiceException extends RuntimeException {
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
