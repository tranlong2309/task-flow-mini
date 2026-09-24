package com.company.invoice.xlsx;

import java.nio.file.Path;
import java.util.Objects;

/** 
 * One unit of work for batch processing.
 * @param input source workbook
 * @param output destination workbook
 * @param context context for diagnostic logging
 */
public record BatchJob(Path input, Path output, JobContext context) {
    /** 
     * Validates the input and output paths. 
     * @param input source workbook
     * @param output destination workbook
     * @param context context for diagnostic logging
     */
    public BatchJob {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(context, "context");
        if (input.toAbsolutePath().normalize().equals(output.toAbsolutePath().normalize())) {
            throw new InvoiceIoException("input and output paths must not be the same");
        }
    }

    /** 
     * Creates a batch job with an empty context. 
     * @param input source workbook
     * @param output destination workbook
     * @return the created batch job
     */
    public static BatchJob of(Path input, Path output) {
        return new BatchJob(input, output, new JobContext());
    }

    /** 
     * Creates a batch job with the provided context. 
     * @param input source workbook
     * @param output destination workbook
     * @param context context for diagnostic logging
     * @return the created batch job
     */
    public static BatchJob of(Path input, Path output, JobContext context) {
        return new BatchJob(input, output, context);
    }
}
