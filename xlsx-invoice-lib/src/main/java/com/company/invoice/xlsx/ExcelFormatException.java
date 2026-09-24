package com.company.invoice.xlsx;

/** Indicates an unsupported, corrupt, encrypted, or structurally invalid workbook. */
public final class ExcelFormatException extends InvoiceException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a workbook format exception.
     * @param message description of the format problem
     */
    public ExcelFormatException(String message) { super(message); }

    /**
     * Creates a workbook format exception with its cause.
     * @param message description of the format problem
     * @param cause underlying format failure
     */
    public ExcelFormatException(String message, Throwable cause) { super(message, cause); }
}
