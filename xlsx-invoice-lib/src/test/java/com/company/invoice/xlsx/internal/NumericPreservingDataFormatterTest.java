package com.company.invoice.xlsx.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NumericPreservingDataFormatterTest {
    @Test
    void preservesNumbersAndMarksPercentages() {
        NumericPreservingDataFormatter formatter = new NumericPreservingDataFormatter();

        assertThat(formatter.formatRawCellContents(0.1, 0, "General")).isEqualTo("0.1");
        String percent = formatter.formatRawCellContents(0.1, 0, "0%");
        assertThat(NumericPreservingDataFormatter.isPercent(percent)).isTrue();
        assertThat(NumericPreservingDataFormatter.removePercentMarker(percent)).isEqualTo("10.0");
        assertThat(NumericPreservingDataFormatter.isPercent("10%")).isFalse();
        assertThat(formatter.formatRawCellContents(1.0, 0, null)).isEqualTo("1.0");
    }
}
