package com.company.invoice.xlsx.error;

/** Indicates a file, stream, temporary-file, atomic-move, or interruption failure. */
public final class InvoiceIoException extends InvoiceException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an I/O exception.
     * @param message description of the I/O problem
     */
    public InvoiceIoException(String message) { super(message); }

    /**
     * Creates an I/O exception with its cause.
     * @param message description of the I/O problem
     * @param cause underlying I/O failure
     */
    public InvoiceIoException(String message, Throwable cause) { super(message, cause); }
}
