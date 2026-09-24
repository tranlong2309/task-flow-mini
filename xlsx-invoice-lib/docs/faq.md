# FAQ

## Should VAT be `10`, `0.1`, or `10%`?

Use numeric `10` for 10 percent. A numeric Excel cell formatted with Excel's Percent format may store `0.1`; the library multiplies it by 100. Text `10%` and text `0.1` are not percent notation exceptions; text values must follow the documented decimal rules and the resulting rate must be in the 0-100 range.

## How do I process VND?

Use `InvoiceConfig.builder().scale(0).build()`. Amounts are still numeric Excel cells and use the `#,##0` number format.

## Why are extra columns ignored?

Only the four required headers define the contract. Extra columns are not copied and their invalid-looking values cannot make an otherwise valid row fail.

## Are hidden rows and columns processed?

Yes. Visibility is presentation metadata; the selected sheet's physical rows and required cells are processed.

## Why are `.xls` and password-protected files rejected?

The library supports OOXML `.xlsx` only and uses POI's streaming OOXML reader. Convert legacy or encrypted files to a normal `.xlsx` file before processing.

## How do I handle Vietnamese text?

OOXML stores Unicode text. No encoding conversion is needed. POI log output can be routed or silenced through the host application's logging configuration.

## What happens to a bad row?

It is omitted from `Invoice`, reported as `RowError`, and optionally listed on `Errors`. Valid rows and totals continue processing.
