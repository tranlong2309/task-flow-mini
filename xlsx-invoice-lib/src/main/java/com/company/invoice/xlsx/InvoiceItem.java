package com.company.invoice.xlsx;

import java.math.BigDecimal;

/** Immutable in-memory input line. */
public record InvoiceItem(String itemName, BigDecimal quantity, BigDecimal unitPrice, BigDecimal vatRate) { }
