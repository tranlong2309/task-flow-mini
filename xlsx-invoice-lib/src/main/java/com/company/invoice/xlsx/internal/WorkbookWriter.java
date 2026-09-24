package com.company.invoice.xlsx.internal;

import com.company.invoice.xlsx.model.ColumnMapping;
import com.company.invoice.xlsx.InvoiceConfig;
import com.company.invoice.xlsx.model.InvoiceLine;
import com.company.invoice.xlsx.model.InvoiceTotals;
import com.company.invoice.xlsx.model.RowError;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/** Writes the fixed output contract with a bounded SXSSF row window. */
public final class WorkbookWriter implements AutoCloseable {
    private static final String[] HEADERS = {"row_type", "line_no", "item_name", "quantity", "unit_price", "vat_rate",
        "amount_before_vat", "vat_amount", "amount_after_vat"};
    private final SXSSFWorkbook workbook;
    private final Sheet invoiceSheet;
    private final CellStyle headerStyle;
    private final CellStyle totalStyle;
    private final CellStyle amountStyle;
    private int rowIndex;

    public WorkbookWriter(InvoiceConfig config) {
        workbook = new SXSSFWorkbook(100);
        workbook.setCompressTempFiles(true);
        invoiceSheet = workbook.createSheet(config.outputSheetName());
        invoiceSheet.createFreezePane(0, 1);
        headerStyle = boldStyle(workbook);
        totalStyle = boldStyle(workbook);
        amountStyle = workbook.createCellStyle();
        amountStyle.setDataFormat(workbook.createDataFormat().getFormat(amountFormat(config.scale())));
        writeHeaders();
        setWidths();
    }

    public void writeLine(InvoiceLine line, int scale) {
        Row row = invoiceSheet.createRow(rowIndex++);
        text(row, 0, "ITEM");
        number(row, 1, BigDecimal.valueOf(line.lineNumber()));
        text(row, 2, line.itemName());
        number(row, 3, line.quantity());
        number(row, 4, line.unitPrice());
        number(row, 5, line.vatRate());
        amount(row, 6, line.amountBeforeVat());
        amount(row, 7, line.vatAmount());
        amount(row, 8, line.amountAfterVat());
    }

    public void writeTotal(InvoiceTotals totals) {
        Row row = invoiceSheet.createRow(rowIndex++);
        text(row, 0, "TOTAL");
        row.getCell(0).setCellStyle(totalStyle);
        amount(row, 6, totals.totalBeforeVat());
        amount(row, 7, totals.totalVat());
        amount(row, 8, totals.totalPayable());
        for (int index = 6; index <= 8; index++) row.getCell(index).setCellStyle(totalStyle);
    }

    public void writeErrors(InvoiceConfig config, List<RowError> errors, long omitted) {
        if (!config.writeErrorSheet() || (errors.isEmpty() && omitted == 0)) return;
        Sheet sheet = workbook.createSheet(config.errorSheetName());
        sheet.createFreezePane(0, 1);
        String[] headers = {"row", "column", "cell", "value", "error_code", "message"};
        Row header = sheet.createRow(0);
        for (int index = 0; index < headers.length; index++) {
            Cell cell = header.createCell(index);
            cell.setCellValue(headers[index]);
            cell.setCellStyle(headerStyle);
        }
        int rowIndex = 1;
        for (RowError error : errors) {
            Row row = sheet.createRow(rowIndex++);
            text(row, 0, Integer.toString(error.rowNumber()));
            text(row, 1, error.columnName());
            text(row, 2, error.cellAddress() == null ? "" : error.cellAddress());
            text(row, 3, error.offendingValue());
            text(row, 4, error.errorCode().name());
            text(row, 5, error.message());
        }
        if (omitted > 0) {
            Row row = sheet.createRow(rowIndex);
            text(row, 0, "NOTE");
            text(row, 5, omitted + " error(s) were not listed because maxReportedErrors was reached");
        }
    }

    public void write(OutputStream output) throws IOException {
        workbook.write(output);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void close() {
        workbook.dispose();
        try {
            workbook.close();
        } catch (IOException ignored) {
            // dispose already removed SXSSF temporary files.
        }
    }

    private void writeHeaders() {
        Row row = invoiceSheet.createRow(rowIndex++);
        for (int index = 0; index < HEADERS.length; index++) {
            Cell cell = row.createCell(index);
            cell.setCellValue(HEADERS[index]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void setWidths() {
        int[] widths = {14, 10, 32, 14, 16, 14, 22, 18, 22};
        for (int index = 0; index < widths.length; index++) invoiceSheet.setColumnWidth(index, widths[index] * 256);
    }

    private static CellStyle boldStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static String amountFormat(int scale) {
        return scale == 0 ? "#,##0" : "#,##0." + "0".repeat(scale);
    }

    private static void text(Row row, int index, String value) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value == null ? "" : value);
    }

    private static void number(Row row, int index, BigDecimal value) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value.doubleValue());
    }

    private void amount(Row row, int index, BigDecimal value) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(amountStyle);
    }
}
