package com.company.invoice.xlsx;

import com.company.invoice.xlsx.internal.WorkbookProcessor;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Thread-safe entry point for streaming workbook processing and in-memory calculation. */
public final class InvoiceProcessor {
    private final InvoiceConfig config;

    private InvoiceProcessor(InvoiceConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    /** Creates an immutable processor that can safely be shared by concurrent callers.
     * @param config immutable processing configuration
     * @return a thread-safe processor
     * @throws NullPointerException if {@code config} is null
     */
    public static InvoiceProcessor create(InvoiceConfig config) {
        return new InvoiceProcessor(config);
    }

    /** Processes one input workbook and atomically replaces the output workbook.
     * @param input source workbook
     * @param output destination workbook
     * @return bounded processing result
     * @throws InvoiceException if the workbook cannot be processed
     */
    public ProcessingResult process(Path input, Path output) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(output, "output");
        if (input.toAbsolutePath().normalize().equals(output.toAbsolutePath().normalize())) {
            throw new IllegalArgumentException("input and output paths must differ");
        }
        return new WorkbookProcessor(config).process(input, output);
    }

    /** Processes an input stream without closing either supplied stream.
     * @param input source stream
     * @param output destination stream
     * @return bounded processing result
     * @throws InvoiceException if the workbook cannot be processed
     */
    public ProcessingResult process(InputStream input, OutputStream output) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(output, "output");
        return new WorkbookProcessor(config).process(input, output);
    }

    /** Calculates an in-memory invoice using the same BigDecimal rules as workbook processing.
     * @param items immutable input items
     * @return calculated invoice and row errors
     * @throws NullPointerException if {@code items} is null
     */
    public Invoice calculate(List<InvoiceItem> items) {
        Objects.requireNonNull(items, "items");
        return WorkbookProcessor.calculate(config, items);
    }
}
