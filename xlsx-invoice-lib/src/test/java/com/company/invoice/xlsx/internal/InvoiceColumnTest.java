package com.company.invoice.xlsx.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class InvoiceColumnTest {
    @Test
    void normalizesHeadersAndAliases() {
        assertThat(InvoiceColumn.normalize(" Item-Name ")).isEqualTo("itemname");
        assertThat(InvoiceColumn.fromHeader("ITEM NAME", Map.of())).isEqualTo(InvoiceColumn.ITEM_NAME);
        assertThat(InvoiceColumn.fromHeader("qty", Map.of("qty", "quantity"))).isEqualTo(InvoiceColumn.QUANTITY);
        assertThat(InvoiceColumn.fromHeader("unknown", Map.of())).isNull();
        assertThat(InvoiceColumn.fromHeader(null, Map.of())).isNull();
    }
}
