# Calculation Rules

For every valid input row, using `BigDecimal` only:

```text
amount_before_vat = quantity * unit_price
vat_amount = amount_before_vat * vat_rate / 100
amount_after_vat = amount_before_vat + vat_amount
```

`amount_before_vat` and `vat_amount` are rounded independently to the configured `scale` and `roundingMode`. `amount_after_vat` is their already-rounded sum. VAT is a percentage and `10` means 10 percent; it is not a fraction and is never extracted from the unit price.

## Example

For quantity `2`, unit price `15000000`, and VAT rate `10` at scale 2:

```text
before = 2 * 15000000 = 30000000.00
vat = 30000000.00 * 10 / 100 = 3000000.00
after = 30000000.00 + 3000000.00 = 33000000.00
```

For quantity `3`, unit price `0.005`, and VAT rate `0` at scale 2 with `HALF_UP`:

```text
before = 3 * 0.005 = 0.015 -> 0.02
vat = 0.00
after = 0.02
```

## Totals

The processor sums the rounded values from each valid item. It never sums unrounded values and rounds only at the end:

```text
TOTAL.before = sum(item.before)
TOTAL.vat = sum(item.vat)
TOTAL.after = sum(item.after)
```

This preserves the amounts that appear on individual invoice lines and makes the total exactly reconcile with the displayed lines. The invariant `TOTAL.after = TOTAL.before + TOTAL.vat` is enforced.

Every calculated amount must be non-negative. A negative calculated amount is reported as `NEGATIVE_AMOUNT`, the line is skipped, and it does not contribute to totals. Zero is valid. Values exceeding Excel's safe 15-significant-digit numeric precision are reported as `PRECISION_EXCEEDED` for a row; an overflowing total is a fatal `InvoiceException`.

Scale `0` is suitable for currencies without minor units such as VND. Scale `2` is the default.
