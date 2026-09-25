package com.company.invoice.xlsx.model;

import java.math.BigDecimal;

/** Immutable invoice totals. */
public record InvoiceTotals(BigDecimal totalBeforeVat, BigDecimal totalVat, BigDecimal totalPayable) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvoiceTotals that)) return false;
        return (totalBeforeVat == null ? that.totalBeforeVat == null : totalBeforeVat.compareTo(that.totalBeforeVat) == 0) &&
               (totalVat == null ? that.totalVat == null : totalVat.compareTo(that.totalVat) == 0) &&
               (totalPayable == null ? that.totalPayable == null : totalPayable.compareTo(that.totalPayable) == 0);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
            totalBeforeVat == null ? null : totalBeforeVat.stripTrailingZeros(),
            totalVat == null ? null : totalVat.stripTrailingZeros(),
            totalPayable == null ? null : totalPayable.stripTrailingZeros()
        );
    }
}
