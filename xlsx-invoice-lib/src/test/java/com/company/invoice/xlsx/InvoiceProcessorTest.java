package com.company.invoice.xlsx;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.company.invoice.xlsx.error.ErrorCode;
import com.company.invoice.xlsx.model.Invoice;
import com.company.invoice.xlsx.model.InvoiceItem;
import com.company.invoice.xlsx.model.InvoiceTotals;
import com.company.invoice.xlsx.model.RowError;

class InvoiceProcessorTest {
    /**
     * @Author: ThachLN Also check the case where the decimal part consists entirely of zeros; in that case, there is no need to return the `.00` part.
     */
    @Test
    void calculatesSpecificationExample() {
        InvoiceProcessor processor = InvoiceProcessor.create(InvoiceConfig.builder().build());

        Invoice invoice = processor.calculate(List.of(
                new InvoiceItem("Laptop Dell", new BigDecimal("2"), new BigDecimal("15000000"), new BigDecimal("10")),
                new InvoiceItem("Chuột, không dây", new BigDecimal("5"), new BigDecimal("200000"), new BigDecimal("8")),
                new InvoiceItem("Sách giáo khoa", new BigDecimal("10"), new BigDecimal("50000"), new BigDecimal("0"))));

        assertThat(invoice.lines()).hasSize(3);
        assertThat(invoice.totals()).isEqualTo(new InvoiceTotals(
                new BigDecimal("31500000.00"), new BigDecimal("3080000.00"), new BigDecimal("34580000.00")));

        assertThat(invoice.totals()).isEqualTo(new InvoiceTotals(
                new BigDecimal("31500000"), new BigDecimal("3080000"), new BigDecimal("34580000")));

        assertThat(invoice.errors()).isEmpty();
    }

    @Test
    void roundsHalfUpAtScaleTwoAndSupportsScaleZero() {
        InvoiceProcessor scaleTwo = InvoiceProcessor.create(InvoiceConfig.builder().scale(2).roundingMode(RoundingMode.HALF_UP).build());
        Invoice invoice = scaleTwo.calculate(List.of(new InvoiceItem("fraction", new BigDecimal("3"), new BigDecimal("0.005"), BigDecimal.ZERO)));
        assertThat(invoice.lines().get(0).amountBeforeVat()).isEqualByComparingTo("0.02");

        InvoiceProcessor scaleZero = InvoiceProcessor.create(InvoiceConfig.builder().scale(0).build());
        Invoice zeroScaleInvoice = scaleZero.calculate(List.of(new InvoiceItem("fraction", BigDecimal.ONE, new BigDecimal("0.5"), BigDecimal.ZERO)));
        assertThat(zeroScaleInvoice.totals().totalBeforeVat()).isEqualByComparingTo("1");
    }

    @Test
    void skipsInvalidRowsAndReportsAllProblems() {
        InvoiceProcessor processor = InvoiceProcessor.create(InvoiceConfig.builder().build());
        Invoice invoice = processor.calculate(List.of(new InvoiceItem("bad", BigDecimal.ZERO, new BigDecimal("-1"), new BigDecimal("101"))));

        assertThat(invoice.lines()).isEmpty();
        assertThat(invoice.errors()).extracting(RowError::errorCode)
                .containsExactly(ErrorCode.OUT_OF_RANGE, ErrorCode.OUT_OF_RANGE, ErrorCode.OUT_OF_RANGE);
    }
}
