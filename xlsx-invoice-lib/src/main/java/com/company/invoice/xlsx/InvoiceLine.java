package com.company.invoice.xlsx;

import java.math.BigDecimal;

/** Immutable calculated invoice line. */
public record InvoiceLine(int lineNumber, String itemName, BigDecimal quantity, BigDecimal unitPrice,
                          BigDecimal vatRate, BigDecimal amountBeforeVat, BigDecimal vatAmount,
                          BigDecimal amountAfterVat) { }
