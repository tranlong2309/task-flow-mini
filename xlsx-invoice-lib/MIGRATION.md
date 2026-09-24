# Migration Guide (v1 to v2)

This library has been refactored to better organize its classes by role. As a result, many classes have been moved into subpackages (`model`, `error`, and `batch`), which requires updating import statements in projects that use this library.

This is a **breaking change** because of the changed package paths. The major version (semver) of the library has been incremented.

## Changes Overview

The core entry points (`InvoiceProcessor`, `InvoiceConfig`, `JobContext`, `ProcessingResult`) remain in the root package (`com.company.invoice.xlsx`).

All other classes have been organized into the following subpackages:

### `com.company.invoice.xlsx.model`
Data models and immutable objects.
- `ColumnMapping`
- `Invoice`
- `InvoiceItem`
- `InvoiceLine`
- `InvoiceTotals`
- `RowError`

### `com.company.invoice.xlsx.error`
Exceptions and error definitions.
- `ErrorCode`
- `ExcelFormatException`
- `ExcelValidationException`
- `InvoiceException`
- `InvoiceIoException`

### `com.company.invoice.xlsx.batch`
Batch processing related classes.
- `BatchItemResult`
- `BatchJob`
- `BatchJobSkippedException`
- `BatchOptions`
- `BatchResult`

## Updating Imports

To migrate your code, you will need to update the import statements for any of the above classes to their new package location.

### Examples

**Old Imports:**
```java
import com.company.invoice.xlsx.Invoice;
import com.company.invoice.xlsx.RowError;
import com.company.invoice.xlsx.InvoiceException;
import com.company.invoice.xlsx.BatchJob;
```

**New Imports:**
```java
import com.company.invoice.xlsx.model.Invoice;
import com.company.invoice.xlsx.model.RowError;
import com.company.invoice.xlsx.error.InvoiceException;
import com.company.invoice.xlsx.batch.BatchJob;
```

Simply update your imports to match the new package structure as listed above. The internal logic and behaviors of all classes remain unchanged.
