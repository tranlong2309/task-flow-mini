package com.company.invoice.xlsx;

import com.company.invoice.xlsx.internal.WorkbookProcessor;
import com.company.invoice.xlsx.internal.JobExecution;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

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
        return process(input, output, new JobContext());
    }

    /** 
     * Processes a workbook with an optional correlation context.
     * @param input source workbook
     * @param output destination workbook
     * @param context optional correlation context
     * @return bounded processing result
     * @throws InvoiceException if the workbook cannot be processed
     */
    public ProcessingResult process(Path input, Path output, JobContext context) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(output, "output");
        if (input.toAbsolutePath().normalize().equals(output.toAbsolutePath().normalize())) {
            throw new IllegalArgumentException("input and output paths must differ");
        }
        try (JobExecution execution = new JobExecution(config, context)) {
            execution.start(input.getFileName().toString(), Files.exists(input) ? Files.size(input) : -1,
                    output.getFileName().toString());
            try {
                ProcessingResult result = new WorkbookProcessor(config).process(input, output, execution);
                execution.complete(result, output.getFileName().toString());
                return result;
            } catch (InvoiceException exception) {
                execution.failed(exception);
                throw exception.withJobId(execution.jobId());
            }
        } catch (IOException exception) {
            throw new InvoiceIoException("invoice processing failed", exception);
        }
    }

    /** Processes an input stream without closing either supplied stream.
     * @param input source stream
     * @param output destination stream
     * @return bounded processing result
     * @throws InvoiceException if the workbook cannot be processed
     */
    public ProcessingResult process(InputStream input, OutputStream output) {
        return process(input, output, new JobContext());
    }

    /** 
     * Processes streams with an optional correlation context.
     * @param input source stream
     * @param output destination stream
     * @param context optional correlation context
     * @return bounded processing result
     * @throws InvoiceException if the workbook cannot be processed
     */
    public ProcessingResult process(InputStream input, OutputStream output, JobContext context) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(output, "output");
        try (JobExecution execution = new JobExecution(config, context)) {
            execution.start("stream", -1, "stream");
            try {
                ProcessingResult result = new WorkbookProcessor(config).process(input, output, execution);
                execution.complete(result, "stream");
                return result;
            } catch (InvoiceException exception) {
                execution.failed(exception);
                throw exception.withJobId(execution.jobId());
            }
        }
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

    /** 
     * Processes a batch of jobs using default options.
     * @param jobs list of batch jobs to process
     * @return the batch result containing all job outcomes
     */
    public BatchResult processBatch(List<BatchJob> jobs) {
        return processBatch(jobs, BatchOptions.builder().build());
    }

    /** 
     * Processes a batch of jobs with the specified options.
     * @param jobs list of batch jobs to process
     * @param options batch processing configuration
     * @return the batch result containing all job outcomes
     */
    public BatchResult processBatch(List<BatchJob> jobs, BatchOptions options) {
        Objects.requireNonNull(jobs, "jobs");
        Objects.requireNonNull(options, "options");
        
        ExecutorService executor = options.executor();
        boolean internalExecutor = false;
        if (executor == null) {
            executor = Executors.newFixedThreadPool(options.maxConcurrency());
            internalExecutor = true;
        }

        try {
            AtomicBoolean abort = new AtomicBoolean(false);
            List<Future<BatchItemResult>> futures = new ArrayList<>();
            
            for (BatchJob job : jobs) {
                futures.add(executor.submit(() -> {
                    if (options.stopOnFirstFailure() && abort.get()) {
                        return null; // Skipped
                    }
                    try {
                        ProcessingResult result = process(job.input(), job.output(), job.context());
                        return new BatchItemResult(job, Optional.of(result), Optional.empty());
                    } catch (InvoiceException exception) {
                        if (options.stopOnFirstFailure()) abort.set(true);
                        return new BatchItemResult(job, Optional.empty(), Optional.of(exception));
                    } catch (Throwable throwable) {
                        if (options.stopOnFirstFailure()) abort.set(true);
                        return new BatchItemResult(job, Optional.empty(), Optional.of(new InvoiceIoException("batch item failed", throwable)));
                    }
                }));
            }
            
            List<BatchItemResult> results = new ArrayList<>();
            for (Future<BatchItemResult> future : futures) {
                try {
                    BatchItemResult result = future.get();
                    if (result != null) results.add(result);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new InvoiceIoException("batch processing was interrupted", exception);
                } catch (Exception exception) {
                    // Should not happen as Callable catches Throwable
                    throw new InvoiceIoException("unexpected error collecting batch results", exception);
                }
            }
            return new BatchResult(results);
        } finally {
            if (internalExecutor) {
                executor.shutdownNow();
            }
        }
    }
}
