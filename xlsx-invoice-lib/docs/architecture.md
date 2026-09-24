# Architecture

The library follows a narrow pipeline:

1. Validate the file signature and input size.
2. Open the OOXML package read-only with POI.
3. Select one sheet and parse rows with `XSSFSheetXMLHandler`.
4. Detect required headers once and ignore extra columns.
5. Convert and validate required cells.
6. Calculate each valid row with `BigDecimal`, then update running totals.
7. Write rows with `SXSSFWorkbook` to a unique temporary output.
8. Write the total and optional Errors sheet, dispose SXSSF files, and atomically move the result.

The public package is `com.company.invoice.xlsx`. POI-specific code is under `internal`. The processor stores only running totals, counters, the configured error cap, and the current SAX row during file processing. The separate `calculate(List<InvoiceItem>)` API intentionally returns in-memory lines.

Build with Java 17 and Maven:

```text
mvn clean verify
```

The artifact includes the normal jar, sources jar, and Javadoc jar. POI is kept as a normal compile dependency so Maven and Gradle consumers resolve it transitively.
