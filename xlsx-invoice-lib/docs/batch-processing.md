# Batch Processing

The `xlsx-invoice-lib` library provides robust, thread-safe support for processing multiple workbooks concurrently through a simple additive batch API. 

## Overview

The batch API uses the existing thread-safe `InvoiceProcessor` to run multiple `BatchJob` instances concurrently. A failed job will not abort or corrupt the rest of the batch by default.

## Basic Usage

```java
import com.company.invoice.xlsx.*;
import java.nio.file.Path;
import java.util.List;

public class BatchExample {
    public static void main(String[] args) {
        InvoiceProcessor processor = InvoiceProcessor.create(InvoiceConfig.builder().build());

        // Create jobs
        List<BatchJob> jobs = List.of(
            BatchJob.of(Path.of("invoices_Q1.xlsx"), Path.of("out_Q1.xlsx"), JobContext.of("job-q1")),
            BatchJob.of(Path.of("invoices_Q2.xlsx"), Path.of("out_Q2.xlsx"), JobContext.of("job-q2"))
        );

        // Process batch with default concurrency (CPU cores)
        BatchResult result = processor.processBatch(jobs);

        System.out.println("Succeeded: " + result.successCount());
        System.out.println("Failed: " + result.failureCount());

        if (result.hasFailures()) {
            for (BatchItemResult failure : result.failures()) {
                System.err.println("Job failed: " + failure.job().input());
                failure.error().ifPresent(Throwable::printStackTrace);
            }
        }
    }
}
```

## Batch Options

You can configure batch execution by providing `BatchOptions`:

```java
BatchOptions options = BatchOptions.builder()
    .maxConcurrency(4)                 // Limit concurrent processing to 4 threads
    .stopOnFirstFailure(true)          // Stop submitting new jobs if one fails
    .build();

BatchResult result = processor.processBatch(jobs, options);
```

### Options Overview:
- `maxConcurrency`: Defaults to your machine's available processors. Controls the size of the internal thread pool. Due to the CPU-intensive nature of SAX XML parsing, it is recommended **not** to exceed your available logical CPU cores.
- `stopOnFirstFailure`: Defaults to `false`. When enabled, the library will abort processing the remainder of the batch if any job throws an `InvoiceException`. Already running jobs will complete, but pending jobs are represented as failed `BatchItemResult`s containing a `BatchJobSkippedException`. The `result.items().size()` always equals the submitted job count regardless of `stopOnFirstFailure`.
- `executor`: You can provide a custom `ExecutorService` if you want to integrate with an existing application thread pool. The processor will use it to submit jobs but will **not** shut it down.

## Partial Failures

By default, the batch runner guarantees isolation. A malformed input file throws an `InvoiceException`, which is caught and wrapped in a `BatchItemResult` with `error()` present, while `result()` remains empty. Other jobs in the same batch will continue processing and complete successfully.
