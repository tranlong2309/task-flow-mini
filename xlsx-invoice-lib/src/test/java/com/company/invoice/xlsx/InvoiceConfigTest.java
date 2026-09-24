package com.company.invoice.xlsx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InvoiceConfigTest {
    @Test
    void exposesDefaultsAndCopiesAliases() {
        InvoiceConfig config = InvoiceConfig.builder()
                .roundingMode(RoundingMode.DOWN)
                .headerAliases(Map.of("qty", "quantity"))
                .tempDirectory(Path.of(System.getProperty("java.io.tmpdir")))
                .build();

        assertThat(config.scale()).isEqualTo(2);
        assertThat(config.roundingMode()).isEqualTo(RoundingMode.DOWN);
        assertThat(config.headerAliases()).containsEntry("qty", "quantity");
        assertThat(config.sxssfRowAccessWindowSize()).isEqualTo(100);

        InvoiceConfig customConfig = InvoiceConfig.builder().sxssfRowAccessWindowSize(500).build();
        assertThat(customConfig.sxssfRowAccessWindowSize()).isEqualTo(500);
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThatIllegalArgumentException().isThrownBy(() -> InvoiceConfig.builder().scale(-1).build());
        assertThatIllegalArgumentException().isThrownBy(() -> InvoiceConfig.builder().headerSearchLimit(0).build());
        assertThatIllegalArgumentException().isThrownBy(() -> InvoiceConfig.builder().maxReportedErrors(0).build());
        assertThatIllegalArgumentException().isThrownBy(() -> InvoiceConfig.builder().maxInputBytes(0).build());
        assertThatIllegalArgumentException().isThrownBy(() -> InvoiceConfig.builder().outputSheetName(" ").build());
        assertThatIllegalArgumentException().isThrownBy(() -> InvoiceConfig.builder().outputSheetName("Same").errorSheetName("Same").build());
        assertThatIllegalArgumentException().isThrownBy(() -> InvoiceConfig.builder().tempDirectory(Path.of("relative")).build());
        assertThatIllegalArgumentException().isThrownBy(() -> InvoiceConfig.builder().sxssfRowAccessWindowSize(0).build());
        assertThatIllegalArgumentException().isThrownBy(() -> InvoiceConfig.builder().sxssfRowAccessWindowSize(-2).build());
    }
}
