# Error Handling

## Exception Hierarchy

File-level failures are fatal and prevent publication of output:

- `InvoiceException`: base unchecked exception for fatal processing failures.
- `ExcelFormatException`: unsupported, corrupt, encrypted, oversized, empty, or structurally invalid workbook; missing sheet; invalid header.
- `InvoiceIoException`: read, write, temporary-file, atomic-move, or interruption failure.
- `ExcelValidationException`: optional caller-side conversion of returned row errors into one exception.

Public exception messages are actionable but do not expose stack traces, secrets, or unnecessary filesystem details.

## Row Errors

A bad data row never aborts processing. It is skipped, valid rows continue, and each detected problem is returned as a `RowError` containing sheet name, physical Excel row, cell address, column, offending value, stable `ErrorCode`, and a human-readable message.

| Code | Meaning | Example | Fix |
| --- | --- | --- | --- |
| `MISSING_VALUE` | Required cell is empty | `item_name` is empty | Supply a value |
| `NOT_A_NUMBER` | Numeric value cannot be parsed | `abc` | Use a decimal number |
| `INVALID_FORMAT` | Numeric text uses an unsupported format | `1,5` or `10%` | Use `1.5` or `10` |
| `OUT_OF_RANGE` | Value violates a range rule | quantity `0`; VAT `101` | Use an allowed value |
| `TOO_MANY_DECIMALS` | Quantity has more than 3 decimals | `1.2345` | Reduce precision |
| `TEXT_TOO_LONG` | Item name exceeds 255 characters | 256-character name | Shorten the name |
| `UNSUPPORTED_CELL_TYPE` | Required cell is boolean, date, or Excel error | `#N/A` | Replace with a supported value |
| `FORMULA_NOT_CACHED` | Formula has no cached result | uncached formula | Save/recalculate the workbook |
| `NEGATIVE_AMOUNT` | Calculation produced a negative amount | negative price input | Correct the source value |
| `PRECISION_EXCEEDED` | Amount exceeds Excel's 15-digit safe precision | very large amount | Reduce magnitude or split the line |

Messages identify the location, for example:

```text
Sheet 'Items', row 7, cell E7 (column 'unit_price'): 'abc' is not a valid number
```

`maxReportedErrors` caps retained and written errors. Additional errors are counted, `errorsTruncated()` becomes true, and the Errors sheet receives a final omission note. `skippedRowCount()` counts rows, not individual problems.

## Errors Sheet

When `writeErrorSheet` is true and at least one row error exists, the output contains an `Errors` sheet with text-only columns:

`row | column | cell | value | error_code | message`

Rows appear in source order. The sheet is optional and never contains formulas.

## Caller Choice

Normal processing returns errors as data:

```java
ProcessingResult result = processor.process(input, output);
if (result.hasErrors()) {
    result.errors().forEach(error -> logger.warn(error.message()));
}
```

Call `result.throwIfHasErrors()` when the caller wants an `ExcelValidationException` after all rows have been processed. This does not change the already-written valid output.
