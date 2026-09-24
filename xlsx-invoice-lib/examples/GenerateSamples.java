import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import com.company.invoice.xlsx.InvoiceConfig;
import com.company.invoice.xlsx.InvoiceProcessor;
import com.company.invoice.xlsx.JobContext;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Regenerates the example input workbooks with Apache POI. */
public final class GenerateSamples {
    private GenerateSamples() { }

    public static void main(String[] args) throws Exception {
        Path directory = Path.of(".");
        create(directory.resolve("sample-input.xlsx"), false, false, true);
        create(directory.resolve("sample-input-minimal.xlsx"), false, false, false);
        create(directory.resolve("sample-input-percent.xlsx"), true, false, false);
        create(directory.resolve("sample-input-with-errors.xlsx"), false, true, true);
        generateJobLog(directory);
    }

    private static void generateJobLog(Path directory) throws Exception {
        Path logDir = directory.resolve("logs");
        InvoiceProcessor processor = InvoiceProcessor.create(InvoiceConfig.builder()
                .jobLogDirectory(logDir)
                .build());
        Path input = directory.resolve("sample-input-with-errors.xlsx");
        Path output = directory.resolve("sample-output-with-errors.xlsx");
        
        processor.process(input, output, JobContext.of("order-123"));
        
        try (Stream<Path> files = Files.list(logDir)) {
            Path logFile = files.findFirst().orElseThrow();
            String content = Files.readString(logFile);
            
            content = content.replaceAll("\\[[a-f0-9\\-]{36}\\]", "[7c1e5b9a-2f34-4a8e-9d10-3b6f0c8e1a55]");
            
            String[] lines = content.split("\n");
            StringBuilder normalized = new StringBuilder();
            long timeBase = 123;
            for (String line : lines) {
                if (line.isEmpty()) continue;
                String replacementTime = "2026-09-24T03:15:20." + String.format("%03d", timeBase) + "Z";
                line = line.replaceFirst("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+Z", replacementTime);
                if (line.contains("elapsedMs=")) {
                    line = line.replaceFirst("elapsedMs=\\d+", "elapsedMs=287");
                }
                normalized.append(line).append("\n");
                timeBase += 100; // arbitrary stable increments
            }
            Files.writeString(directory.resolve("sample-job.log"), normalized.toString());
            Files.delete(logFile);
            Files.delete(logDir);
        }
    }

    private static void create(Path path, boolean percent, boolean errors, boolean extraColumns) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Items");
            if (extraColumns) sheet.createRow(0).createCell(0).setCellValue("Purchase order PO-2026-001");
            int headerRow = extraColumns ? 2 : 0;
            Row header = sheet.createRow(headerRow);
            List<String> columns = extraColumns
                    ? List.of("id", "vat_rate", "note", "item_name", "unit_price", "supplier", "quantity")
                    : List.of("quantity", "item_name", "vat_rate", "unit_price");
            for (int index = 0; index < columns.size(); index++) header.createCell(index).setCellValue(columns.get(index));
            addRow(sheet, headerRow + 1, extraColumns, "Laptop Dell", "2", "15000000", "10", percent);
            addRow(sheet, headerRow + 2, extraColumns, "Chuột, không dây", "5", "200000", "8", percent);
            addRow(sheet, headerRow + 3, extraColumns, "Sách giáo khoa", "10", "50000", "0", percent);
            if (errors) {
                addRow(sheet, headerRow + 4, extraColumns, "Bút bi", "10", "abc", "8", false);
                addRow(sheet, headerRow + 5, extraColumns, "Túi", "0", "100", "10", false);
            }
            try (OutputStream output = Files.newOutputStream(path)) { workbook.write(output); }
        }
    }

    private static void addRow(org.apache.poi.ss.usermodel.Sheet sheet, int rowNumber, boolean extraColumns,
                               String itemName, String quantity, String unitPrice, String vatRate, boolean percent) {
        Row row = sheet.createRow(rowNumber);
        if (extraColumns) {
            row.createCell(0).setCellValue(rowNumber);
            row.createCell(1).setCellValue(Double.parseDouble(vatRate));
            row.createCell(2).setCellValue("note");
            row.createCell(3).setCellValue(itemName);
            setValue(row.createCell(4), unitPrice);
            row.createCell(5).setCellValue("supplier");
            setNumber(row.createCell(6), quantity);
            return;
        }
        setNumber(row.createCell(0), quantity);
        row.createCell(1).setCellValue(itemName);
        Cell vat = row.createCell(2);
        if (percent) {
            vat.setCellValue(Double.parseDouble(vatRate) / 100.0);
            CellStyle style = sheet.getWorkbook().createCellStyle();
            style.setDataFormat(sheet.getWorkbook().createDataFormat().getFormat("0%"));
            vat.setCellStyle(style);
        } else vat.setCellValue(Double.parseDouble(vatRate));
        setNumber(row.createCell(3), unitPrice);
    }

    private static void setNumber(Cell cell, String value) { cell.setCellValue(Double.parseDouble(value)); }

    private static void setValue(Cell cell, String value) {
        try {
            setNumber(cell, value);
        } catch (NumberFormatException exception) {
            cell.setCellValue(value);
        }
    }
}
