package com.company.invoice.xlsx.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.invoice.xlsx.ErrorCode;
import com.company.invoice.xlsx.InvoiceConfig;
import com.company.invoice.xlsx.InvoiceItem;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class CalculatorTest {
    @Test
    void reportsMissingAndInvalidFieldsTogether() {
        Calculator.CalculationResult result = Calculator.calculate(InvoiceConfig.builder().build(),
                List.of(new InvoiceItem(null, null, null, null)), "Items");

        assertThat(result.lines()).isEmpty();
        assertThat(result.errors()).extracting(error -> error.errorCode())
                .containsExactly(ErrorCode.MISSING_VALUE, ErrorCode.MISSING_VALUE, ErrorCode.MISSING_VALUE, ErrorCode.MISSING_VALUE);
    }

    @Test
    void reportsLongNamesAndTooManyQuantityDecimals() {
        Calculator.CalculationResult result = Calculator.calculate(InvoiceConfig.builder().build(),
                List.of(new InvoiceItem("x".repeat(256), new BigDecimal("1.2345"), BigDecimal.ONE, BigDecimal.ZERO)), "Items");

        assertThat(result.errors()).extracting(error -> error.errorCode())
                .containsExactly(ErrorCode.TEXT_TOO_LONG, ErrorCode.TOO_MANY_DECIMALS);
    }

    @Test
    void rejectsNullItemsAndTotalPrecisionOverflow() {
        Calculator.CalculationResult nullResult = Calculator.calculate(InvoiceConfig.builder().build(), Arrays.asList((InvoiceItem) null), "Items");
        assertThat(nullResult.errors()).extracting(error -> error.errorCode()).containsExactly(ErrorCode.MISSING_VALUE);

        List<InvoiceItem> largeInvoice = new ArrayList<>();
        for (int index = 0; index < 1_000; index++) {
            largeInvoice.add(new InvoiceItem("large-" + index, BigDecimal.ONE,
                    new BigDecimal("9000000000000"), BigDecimal.ZERO));
        }
        assertThatThrownBy(() -> Calculator.calculate(InvoiceConfig.builder().build(), largeInvoice, "Items"))
                .isInstanceOf(com.company.invoice.xlsx.InvoiceException.class);
    }
}
