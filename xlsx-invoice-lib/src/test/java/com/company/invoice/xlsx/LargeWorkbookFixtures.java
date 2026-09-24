package com.company.invoice.xlsx;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class LargeWorkbookFixtures {

    public static void generate(Path output, int rowCount, boolean forceErrors) throws Exception {
        Random random = new Random(42);
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100); OutputStream stream = Files.newOutputStream(output)) {
            Sheet sheet = workbook.createSheet("Data");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("item_name");
            header.createCell(1).setCellValue("quantity");
            header.createCell(2).setCellValue("unit_price");
            header.createCell(3).setCellValue("vat_rate");

            for (int i = 1; i <= rowCount; i++) {
                Row row = sheet.createRow(i);
                if (forceErrors) {
                    row.createCell(0).setCellValue("");
                    row.createCell(1).setCellValue(-1);
                    row.createCell(2).setCellValue(-100);
                    row.createCell(3).setCellValue(200);
                } else {
                    row.createCell(0).setCellValue("Item " + i);
                    row.createCell(1).setCellValue(1 + random.nextInt(10));
                    row.createCell(2).setCellValue(10.0 + random.nextDouble() * 90.0);
                    row.createCell(3).setCellValue(10);
                }
            }
            workbook.write(stream);
            workbook.dispose();
        }
    }
}
