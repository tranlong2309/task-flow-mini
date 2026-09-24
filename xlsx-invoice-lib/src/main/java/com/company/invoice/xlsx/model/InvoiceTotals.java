package com.company.invoice.xlsx.model;

import java.math.BigDecimal;

/** Immutable invoice totals. */
public record InvoiceTotals(BigDecimal totalBeforeVat, BigDecimal totalVat, BigDecimal totalPayable) { }
