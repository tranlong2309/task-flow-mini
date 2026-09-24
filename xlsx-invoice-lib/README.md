# xlsx-invoice-lib

`xlsx-invoice-lib` is a Java 17 library that reads a streaming `.xlsx` invoice sheet, validates line items, calculates VAT with `BigDecimal`, and writes a fixed-format output workbook. It uses Apache POI's SAX reader and SXSSF writer so file processing memory stays bounded by configuration and the error cap. The immutable `InvoiceProcessor` is safe to share between threads.

## Dependency

Maven:

```xml
<dependency>
  <groupId>com.company.invoice</groupId>
  <artifactId>xlsx-invoice-lib</artifactId>
  <version>1.0.0</version>
</dependency>
```

Gradle Kotlin DSL:

```kotlin
dependencies {
    implementation("com.company.invoice:xlsx-invoice-lib:1.0.0")
}
```

Gradle Groovy:

```groovy
dependencies {
    implementation 'com.company.invoice:xlsx-invoice-lib:1.0.0'
}
```

POI is a normal transitive dependency and is not shaded. The library does not install a logging implementation. Route or silence POI's `log4j-api` according to the consuming service's logging policy.

## Quick Start

```java
InvoiceProcessor processor = InvoiceProcessor.create(InvoiceConfig.builder().build());
ProcessingResult result = processor.process(Path.of("input.xlsx"), Path.of("output.xlsx"));
System.out.println(result.totals().totalPayable());
```

The processor skips invalid rows, returns their capped diagnostics, and writes valid rows plus a total. Call `result.throwIfHasErrors()` when the application wants to convert collected row errors into `ExcelValidationException` after processing.

## API Styles

File to file is the lowest-memory option:

```java
ProcessingResult result = processor.process(input, output);
```

For web uploads, streams are not closed by the library; input is spooled to a temporary file:

```java
processor.process(uploadStream, downloadStream);
```

For an in-memory calculation:

```java
Invoice invoice = processor.calculate(List.of(
    new InvoiceItem("Laptop", new BigDecimal("2"), new BigDecimal("15000000"), new BigDecimal("10"))));
```

For batch processing multiple files:

```java
List<BatchJob> jobs = List.of(BatchJob.of(Path.of("in1.xlsx"), Path.of("out1.xlsx")));
BatchResult result = processor.processBatch(jobs);
```

## Configuration

| Option | Default |
| --- | --- |
| `scale` | `2` |
| `roundingMode` | `HALF_UP` |
| `sheetName` / `sheetIndex` | first sheet |
| `headerSearchLimit` | `20` non-blank rows |
| `headerAliases` | empty |
| `outputSheetName` | `Invoice` |
| `writeErrorSheet` / `errorSheetName` | `true` / `Errors` |
| `maxReportedErrors` | `10000` |
| `maxInputBytes` | `50 MB` |
| `tempDirectory` | system temp directory |

Use `scale(0)` for currencies without minor units such as VND. VAT `10` means 10 percent. A numeric Excel cell formatted as `10%` stores `0.1` and is converted to the same semantic rate.

## Concurrent Usage

Create one processor per configuration and share it across tasks. Each call uses unique temporary paths, isolated state, and atomic output publication. The library creates no threads; the host controls its executor size. See [docs/concurrency.md](docs/concurrency.md).

## Logging

Diagnostic events use SLF4J 2.x and never install a logging provider. Use `JobContext.of("order-123")` for request correlation, and enable per-job files with `jobLogDirectory(...)`. See [docs/logging.md](docs/logging.md).

## Running Tests

Prerequisites: Java 17 and Maven 3.9 or newer. Run these commands from the `xlsx-invoice-lib` directory.

### Windows PowerShell

```powershell
mvn test
mvn clean verify
```

If Maven is installed locally but is not in `PATH`:

```powershell
$maven = Get-ChildItem "$env:USERPROFILE\.m2\wrapper\dists" -Filter mvn.cmd -Recurse |
    Select-Object -First 1 -ExpandProperty FullName
& $maven clean verify
```

### Linux

```bash
mvn test
mvn clean verify
```

### macOS

```bash
mvn test
mvn clean verify
```

Run one test class on any platform:

```text
mvn -Dtest=InvoiceProcessorTest test
```

Generated reports:

- Test results: `target/surefire-reports/`
- JaCoCo coverage: `target/site/jacoco/index.html`
- HTML Javadoc: `target/reports/apidocs/index.html`

## Documentation

## Exporting Javadoc

From the library directory, run:

```text
mvn clean package
```

This creates:

- `target/xlsx-invoice-lib-1.0.0-javadoc.jar` for Maven/Gradle dependency resolution.
- `target/reports/apidocs/index.html` for local HTML browsing.
- `target/xlsx-invoice-lib-1.0.0-sources.jar` with source attachments.

To generate only HTML Javadoc, run `mvn javadoc:javadoc`.

- [Excel format](docs/excel-format.md)
- [Calculation rules](docs/calculation-rules.md)
- [Error handling](docs/error-handling.md)
- [Architecture](docs/architecture.md)
- [Concurrency](docs/concurrency.md)
- [FAQ](docs/faq.md)
