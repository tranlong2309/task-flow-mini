package com.company.invoice.xlsx.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.invoice.xlsx.error.ErrorCode;
import com.company.invoice.xlsx.InvoiceConfig;
import com.company.invoice.xlsx.model.InvoiceItem;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CalculatorValidationBranchesTest {
    @Test
    void validatesEachNumericBoundary() {
        List<InvoiceItem> items = List.of(
                new InvoiceItem("quantity", new BigDecimal("-1"), BigDecimal.ONE, BigDecimal.ZERO),
                new InvoiceItem("price", BigDecimal.ONE, new BigDecimal("-1"), BigDecimal.ZERO),
                new InvoiceItem("vat", BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("101")),
                new InvoiceItem("decimals", new BigDecimal("1.0001"), BigDecimal.ONE, BigDecimal.ZERO));

        Calculator.CalculationResult result = Calculator.calculate(InvoiceConfig.builder().build(), items, "Items");

        assertThat(result.errors()).extracting(error -> error.errorCode())
                .containsExactly(ErrorCode.OUT_OF_RANGE, ErrorCode.OUT_OF_RANGE, ErrorCode.OUT_OF_RANGE,
                        ErrorCode.TOO_MANY_DECIMALS);
    }

        @Test
        void acceptsInclusiveBoundariesAndValidDecimalScale() {
                Calculator.CalculationResult result = Calculator.calculate(InvoiceConfig.builder().build(),
                                List.of(new InvoiceItem("x".repeat(255), new BigDecimal("1.000"), BigDecimal.ZERO,
                                                new BigDecimal("100"))), "Items");

                assertThat(result.errors()).isEmpty();
                assertThat(result.lines()).hasSize(1);
        }
}
