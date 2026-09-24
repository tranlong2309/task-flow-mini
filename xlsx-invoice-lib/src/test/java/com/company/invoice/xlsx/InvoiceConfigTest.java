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
                .tempDirectory(Path.of("C:/tmp"))
                .build();

        assertThat(config.scale()).isEqualTo(2);
        assertThat(config.roundingMode()).isEqualTo(RoundingMode.DOWN);
        assertThat(config.headerAliases()).containsEntry("qty", "quantity");
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
    }
}
