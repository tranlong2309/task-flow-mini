package com.company.invoice.xlsx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.company.invoice.xlsx.error.ErrorCode;
import com.company.invoice.xlsx.error.ExcelFormatException;
import com.company.invoice.xlsx.model.Invoice;
import com.company.invoice.xlsx.model.RowError;

class WorkbookProcessingIntegrationTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void processesDynamicColumnsAndWritesNumericOutput() throws Exception {
        Path input = createWorkbook(false);
        Path output = temporaryDirectory.resolve("output.xlsx");

        ProcessingResult result = processor().process(input, output);

        assertThat(result.dataRowCount()).isEqualTo(2);
        assertThat(result.processedRowCount()).isEqualTo(2);
        assertThat(result.skippedRowCount()).isZero();
        assertThat(result.totals().totalPayable()).isEqualByComparingTo("34080000.00");
        assertThat(result.columnMapping().columnIndexes())
                .containsEntry("item_name", 1)
                .containsEntry("quantity", 3);
        assertOutput(output, false);
    }

    @Test
    void skipsInvalidRowsAndWritesErrorsSheet() throws Exception {
        Path input = createWorkbook(true);
        Path output = temporaryDirectory.resolve("errors.xlsx");

        ProcessingResult result = processor().process(input, output);

        assertThat(result.dataRowCount()).isEqualTo(3);
        assertThat(result.processedRowCount()).isEqualTo(2);
        assertThat(result.skippedRowCount()).isEqualTo(1);
        assertThat(result.errors()).singleElement().extracting(RowError::errorCode)
                .isEqualTo(ErrorCode.NOT_A_NUMBER);
        assertOutput(output, true);
    }

    @Test
    void processesInputAndOutputStreamsWithoutClosingThem() throws IOException {
        Path input = createWorkbook(false);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(input))) {
            ProcessingResult result = processor().process(inputStream, output);
            assertThat(result.processedRowCount()).isEqualTo(2);
        }

        assertThat(output.size()).isPositive();
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(output.toByteArray()))) {
            assertThat(workbook.getSheet("Invoice")).isNotNull();
        }
    }

    @Test
    void rejectsSamePathAndNonExcelInput() throws IOException {
        Path input = temporaryDirectory.resolve("not-excel.xlsx");
        Files.writeString(input, "not an OOXML workbook");

        assertThatThrownBy(() -> processor().process(input, input))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> processor().process(input, temporaryDirectory.resolve("out.xlsx")))
                .isInstanceOf(ExcelFormatException.class);
        assertThatThrownBy(() -> processor().process(temporaryDirectory.resolve("missing.xlsx"),
                temporaryDirectory.resolve("missing-out.xlsx")))
                .isInstanceOf(ExcelFormatException.class);
    }

    @Test
    void rejectsEmptyWorkbookContent() throws IOException {
        Path input = temporaryDirectory.resolve("empty.xlsx");
        try (Workbook workbook = new XSSFWorkbook(); var output = Files.newOutputStream(input)) {
            workbook.write(output);
        }

        assertThatThrownBy(() -> processor().process(input, temporaryDirectory.resolve("empty-out.xlsx")))
                .isInstanceOf(ExcelFormatException.class);
    }

    private InvoiceProcessor processor() {
        return InvoiceProcessor.create(InvoiceConfig.builder().tempDirectory(temporaryDirectory).build());
    }

    private Path createWorkbook(boolean includeInvalidRow) throws IOException {
        Path input = temporaryDirectory.resolve("input-" + (includeInvalidRow ? "invalid" : "valid") + ".xlsx");
        try (Workbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Items");
            sheet.createRow(0).createCell(0).setCellValue("Purchase order");
            var header = sheet.createRow(2);
            List<String> headers = List.of("id", "item_name", "unit_price", "quantity", "vat_rate", "note");
            for (int index = 0; index < headers.size(); index++) header.createCell(index).setCellValue(headers.get(index));
            addRow(sheet, 3, "Laptop Dell", 15_000_000, 2, 10);
            addRow(sheet, 4, "Chuột, không dây", 200_000, 5, 8);
            if (includeInvalidRow) {
                var row = sheet.createRow(5);
                row.createCell(1).setCellValue("Bad row");
                row.createCell(2).setCellValue("abc");
                row.createCell(3).setCellValue(1);
                row.createCell(4).setCellValue(10);
            }
            try (var output = Files.newOutputStream(input)) {
                workbook.write(output);
            }
        }
        return input;
    }

    private static void addRow(org.apache.poi.ss.usermodel.Sheet sheet, int rowNumber, String name,
                               double price, double quantity, double vat) {
        var row = sheet.createRow(rowNumber);
        row.createCell(0).setCellValue(rowNumber);
        row.createCell(1).setCellValue(name);
        row.createCell(2).setCellValue(price);
        row.createCell(3).setCellValue(quantity);
        row.createCell(4).setCellValue(vat);
        row.createCell(5).setCellValue("ignored");
    }

    private static void assertOutput(Path output, boolean expectErrors) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(output.toFile())) {
            var invoice = workbook.getSheet("Invoice");
            assertThat(invoice.getPhysicalNumberOfRows()).isEqualTo(expectErrors ? 4 : 4);
            assertThat(invoice.getRow(0).getCell(0).getStringCellValue()).isEqualTo("row_type");
            assertThat(invoice.getRow(1).getCell(6).getCellType()).isEqualTo(CellType.NUMERIC);
                assertThat(invoice.iterator()).toIterable()
                    .anySatisfy(row -> assertThat(row.getCell(0).getStringCellValue()).isEqualTo("TOTAL"));
            assertThat(workbook.getSheet("Errors") != null).isEqualTo(expectErrors);
        }
    }
}
