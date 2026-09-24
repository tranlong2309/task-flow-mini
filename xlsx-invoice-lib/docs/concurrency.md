# Concurrency

`InvoiceProcessor` and `InvoiceConfig` are immutable and thread-safe. A processor can be shared by many callers. Calls do not share parser, writer, totals, errors, or temporary paths. The library creates no executor or background thread.

File processing memory is bounded by the SAX current row, POI package structures, the SXSSF row window (100), running totals, and `maxReportedErrors`. Temporary input/output and SXSSF files require disk space; provision space for the largest input plus generated output per concurrent job, with headroom.

A host application owns pool sizing:

```java
ExecutorService pool = Executors.newFixedThreadPool(8);
Future<ProcessingResult> future = pool.submit(() -> processor.process(input, output));
```

Size the pool from available CPU, disk throughput, and temporary-disk capacity rather than from the number of users. A 100,000-row smoke test should be repeated with the target heap and realistic concurrency.

The library does not change JVM-global POI settings such as `ZipSecureFile`, `IOUtils`, or `TempFile`. Consumers may tune those settings centrally, subject to their security policy. POI zip-bomb protections remain enabled.

Interruption stops processing, removes owned temporary files, restores the interrupt flag, and throws `InvoiceIoException`.
