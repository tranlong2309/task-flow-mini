package com.company.invoice.xlsx.internal;

import java.util.Locale;
import java.util.Map;

/** Single source of truth for required input columns. */
public enum InvoiceColumn {
    ITEM_NAME("item_name"),
    QUANTITY("quantity"),
    UNIT_PRICE("unit_price"),
    VAT_RATE("vat_rate");

    private final String headerName;

    InvoiceColumn(String headerName) {
        this.headerName = headerName;
    }

    public String headerName() {
        return headerName;
    }

    public static InvoiceColumn fromHeader(String value, Map<String, String> aliases) {
        String normalized = normalize(value);
        for (InvoiceColumn column : values()) {
            if (normalize(column.headerName()).equals(normalized)) return column;
        }
        for (Map.Entry<String, String> alias : aliases.entrySet()) {
            if (normalize(alias.getKey()).equals(normalized)) {
                return fromHeader(alias.getValue(), Map.of());
            }
        }
        return null;
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace(" ", "").replace("-", "").replace("_", "");
    }
}
