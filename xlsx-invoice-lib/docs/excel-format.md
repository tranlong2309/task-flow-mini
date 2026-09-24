# Excel Format

## Scope

The library accepts one OOXML `.xlsx` workbook per call. The file is detected by content and is read with POI's event API. Legacy `.xls`, `.xlsm`, `.xlsb`, `.ods`, CSV, encrypted, corrupt, and non-Excel files are rejected.

## Input Sheet

By default the first visible sheet is selected. `InvoiceConfig` can select a sheet by name or zero-based index. Hidden rows and columns are processed. Only one sheet is read.

The header is the first row within the first `headerSearchLimit` non-blank rows containing all required columns. The default limit is 20. Header matching trims whitespace, ignores case, and treats spaces, hyphens, and underscores as equivalent. Configured aliases may also match.

Required columns:

| Header | Accepted values | Rules |
| --- | --- | --- |
| `item_name` | Text; numeric cells become plain text | Required after trim; maximum 255 characters |
| `quantity` | Number or valid numeric text | Greater than zero; maximum 3 decimal places |
| `unit_price` | Number or valid numeric text | Greater than or equal to zero |
| `vat_rate` | Number or valid numeric text | Percentage value from 0 through 100 |

Additional columns are ignored completely. Their values and cell types do not create errors. Required-column duplicates, a missing header, an empty sheet, or a header with no rows are file-level format errors.

## Cell Rules

Text is trimmed. Numeric cells use the raw numeric value and are converted with `BigDecimal.valueOf(double)` before any arithmetic. Text numbers use `.` as the decimal separator and do not contain thousands separators, currency symbols, or `%`.

A numeric `vat_rate` cell with a percentage number format is interpreted as its stored value multiplied by 100. Thus General `10` and Percent `10%` (stored as `0.1`) both mean 10 percent. Text `10%` is invalid.

Formula cached results are accepted; formulas without cached results produce `FORMULA_NOT_CACHED`. Boolean, date/time, and error cells in required columns produce `UNSUPPORTED_CELL_TYPE`. In a merged range only the top-left cell has a value. Completely empty rows are skipped. A row containing only ignored-column values is invalid because required values are missing.

## Output Workbook

The output contains an `Invoice` sheet, or the configured output sheet name. The first row is bold and frozen. Columns are fixed:

`row_type | line_no | item_name | quantity | unit_price | vat_rate | amount_before_vat | vat_amount | amount_after_vat`

`ITEM` rows contain valid input rows in source order. `line_no` starts at 1 and counts valid output rows. One bold `TOTAL` row is always last. Its first five data fields are empty and its three amount fields contain the sums of item rows. If no input row is valid, the total is zero.

Numeric fields are written as numeric Excel cells, never text. Amount formats are `#,##0` for scale 0 or `#,##0.` followed by the configured number of zeroes. No formulas are written. Text beginning with `=`, `+`, `-`, or `@` is written as text to prevent formula injection. Output is UTF-8-compatible OOXML and does not use a BOM concept.

When enabled and row errors exist, an `Errors` sheet is written with text-only columns:

`row | column | cell | value | error_code | message`

Errors are listed in source order up to `maxReportedErrors`; a final note records omitted errors when truncation occurs.

## Valid Example

| item_name | quantity | unit_price | vat_rate |
| --- | ---: | ---: | ---: |
| Laptop Dell | 2 | 15000000 | 10 |
| Chuột, không dây | 5 | 200000 | 8 |
| Sách giáo khoa | 10 | 50000 | 0 |

## Invalid Examples

| Input | Code | Message pattern |
| --- | --- | --- |
| `quantity = 0` | `OUT_OF_RANGE` | `Sheet 'Items', row 8, cell G8 (column 'quantity'): value '0' is out of range (must be greater than 0)` |
| `unit_price = abc` | `NOT_A_NUMBER` | `Sheet 'Items', row 7, cell E7 (column 'unit_price'): 'abc' is not a valid number` |
| `vat_rate = 10%` as text | `INVALID_FORMAT` | `Sheet 'Items', row 9, cell B9 (column 'vat_rate'): '10%' has an invalid number format` |
| missing `item_name` | `MISSING_VALUE` | `Sheet 'Items', row 10, cell D10 (column 'item_name'): value is required` |

Header and file errors are thrown as `ExcelFormatException`; row errors are returned in `ProcessingResult` and never stop other rows.
