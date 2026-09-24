# Performance & Load Testing Guide

The `xlsx-invoice-lib` library uses a streaming SAX reader (`XSSFReader`) and a streaming writer (`SXSSFWorkbook`) to process Excel files without loading the entire workbook into memory.

## Running the Performance Benchmarks

Performance tests are excluded from the default test suite to ensure fast local builds. To run the performance and load tests manually:

```bash
# Using a local Maven installation
mvn test -Pperformance
```

This will run tests in the `com.company.invoice.xlsx` package tagged with `@Tag("performance")`.

## Interpreting the Results

### 1. Scaling Benchmark (10k vs 100k rows)

The scaling benchmark (`LargeWorkbookPerformanceTest`) generates large synthetic workbooks and measures elapsed time and memory allocation.

**Reference Machine Baseline:**
- **10,000 rows**: ~1.7 seconds (177 ms per 1000 rows)
- **100,000 rows**: ~16.7 seconds (167 ms per 1000 rows)
- **Scaling Ratio**: ~9.42x 

**Key Takeaways:**
- **Linear Scaling**: The processing time scales linearly with the number of rows (the 100k test takes roughly 10x the time of the 10k test, rather than exponentially more).
- **Memory Footprint**: The streaming architecture effectively bounds memory usage. Even for 100,000 rows, heap deltas remain completely stable (and even negative across GC cycles), confirming that large files do not cause OutOfMemoryErrors.
- **Double-Validation Overhead**: We measured the overhead of `WorkbookProcessor` performing pre-validation before calling `Calculator.calculate()` (which performs the same validation). Given the processing throughput is bottlenecked heavily by OOXML SAX parsing and string extraction, the CPU cost of these duplicate fast `if` checks (string length, `signum`) is negligible. We intentionally decided **not** to refactor `Calculator` to bypass validation for the streaming path, optimizing for code simplicity and safety over micro-optimizations.

### 2. Concurrency Load Test

The load test (`ConcurrentProcessingLoadTest`) shares a single `InvoiceProcessor` across a fixed thread pool and processes 5,000-row files concurrently.

**Reference Machine Baseline (5,000 rows per file):**
- **Concurrency Level 10** (10 concurrent jobs): ~17 seconds total wall time (avg 8.2s latency per call)
- **Concurrency Level 100** (100 concurrent jobs): ~72 seconds total wall time (avg 28.5s latency per call)
- **Concurrency Level 1000** (1000 concurrent jobs, capped thread pool): ~339 seconds total wall time (avg 53s latency per call)

*(Note: The test automatically caps the thread pool size to `availableProcessors * 20` to avoid crashing the JVM on constrained CI runners when testing 1000 concurrency).*

**Key Takeaways & Thread Pool Guidance:**
- `InvoiceProcessor` and `InvoiceConfig` are strictly thread-safe.
- Because SAX XML parsing is highly CPU-intensive, concurrent throughput scales well up to your CPU core count. 
- **Recommendation**: For production services accepting file uploads, bound your execution thread pool to `Runtime.getRuntime().availableProcessors() * 2`. Pushing hundreds of concurrent active parsing threads will cause heavy CPU contention and latency degradation, as seen in the 100 and 1000 concurrency tests. Using a queue (like a bounded executor or a message broker) with a small, fixed pool size will yield the highest throughput and most predictable latency.
