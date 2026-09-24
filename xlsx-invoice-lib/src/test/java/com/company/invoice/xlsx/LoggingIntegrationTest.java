package com.company.invoice.xlsx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.MDC;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.company.invoice.xlsx.internal.JobExecution;
import java.util.List;

class LoggingIntegrationTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void writesAJobLogAndRestoresMdc() throws Exception {
        Path input = createWorkbook();
        Path output = temporaryDirectory.resolve("output.xlsx");
        MDC.put("invoiceJobId", "caller-job");
        MDC.put("invoiceCorrelationId", "caller-correlation");

        ProcessingResult result = InvoiceProcessor.create(InvoiceConfig.builder()
                .jobLogDirectory(temporaryDirectory.resolve("logs"))
                .build())
                .process(input, output, JobContext.of("order-123"));

        assertThat(result.jobId()).isNotBlank();
        Path jobLog = result.jobLogFile().orElseThrow();
        String content = Files.readString(jobLog);
        assertThat(content).contains("job.start", "header.detected", "job.end", result.jobId(), "order-123");
        assertThat(MDC.get("invoiceJobId")).isEqualTo("caller-job");
        assertThat(MDC.get("invoiceCorrelationId")).isEqualTo("caller-correlation");
    }

    @Test
    void fatalFailureCarriesJobIdAndWritesFailedJobLog() throws Exception {
        Path input = temporaryDirectory.resolve("bad.xlsx");
        Files.writeString(input, "not-xlsx");
        Path logDirectory = temporaryDirectory.resolve("logs");

        assertThatThrownBy(() -> InvoiceProcessor.create(InvoiceConfig.builder().jobLogDirectory(logDirectory).build())
                .process(input, temporaryDirectory.resolve("output.xlsx"), JobContext.of("fatal-test")))
                .isInstanceOf(ExcelFormatException.class)
                .satisfies(exception -> {
                    assertThat(((InvoiceException) exception).getJobId()).isNotBlank();
                });

        assertThat(Files.list(logDirectory).findFirst()).isPresent();
        Path jobLog = Files.list(logDirectory).findFirst().orElseThrow();
        assertThat(Files.readString(jobLog)).contains("job.failed", "job.end");
    }

    private Path createWorkbook() throws Exception {
        Path input = temporaryDirectory.resolve("input.xlsx");
        try (Workbook workbook = new XSSFWorkbook(); var output = Files.newOutputStream(input)) {
            var sheet = workbook.createSheet("Items");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("item_name");
            header.createCell(1).setCellValue("quantity");
            header.createCell(2).setCellValue("unit_price");
            header.createCell(3).setCellValue("vat_rate");
            var row = sheet.createRow(1);
            row.createCell(0).setCellValue("Item");
            row.createCell(1).setCellValue(1);
            row.createCell(2).setCellValue(100);
            row.createCell(3).setCellValue(10);
            workbook.write(output);
        }
        return input;
    }

    @Test
    void capturesAndVerifiesDiagnosticLogs() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(JobExecution.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        try {
            Path input = createWorkbook();
            Path output = temporaryDirectory.resolve("output-diag.xlsx");
            
            InvoiceProcessor processor = InvoiceProcessor.create(InvoiceConfig.builder().build());
            ProcessingResult result = processor.process(input, output, JobContext.of("diag-123"));
            
            List<ILoggingEvent> events = listAppender.list;
            assertThat(events).isNotEmpty();
            
            assertThat(events).anySatisfy(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.INFO);
                assertThat(event.getFormattedMessage()).contains("job.start", "diag-123");
            });
            assertThat(events).anySatisfy(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.INFO);
                assertThat(event.getFormattedMessage()).contains("header.detected");
            });
            assertThat(events).anySatisfy(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.INFO);
                assertThat(event.getFormattedMessage()).contains("job.end", "COMPLETED");
            });
        } finally {
            logger.detachAppender(listAppender);
        }
    }

    @Test
    void rowErrorEscapingFix() throws Exception {
        Path input = temporaryDirectory.resolve("bad-escaping.xlsx");
        try (Workbook workbook = new XSSFWorkbook(); var output = Files.newOutputStream(input)) {
            var sheet = workbook.createSheet("Items");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("item_name");
            header.createCell(1).setCellValue("quantity");
            header.createCell(2).setCellValue("unit_price");
            header.createCell(3).setCellValue("vat_rate");
            var row = sheet.createRow(1);
            row.createCell(0).setCellValue("Item");
            row.createCell(1).setCellValue("bad\\\"value");
            row.createCell(2).setCellValue(100);
            row.createCell(3).setCellValue(10);
            workbook.write(output);
        }

        Path logDirectory = temporaryDirectory.resolve("logs-esc");
        ProcessingResult result = InvoiceProcessor.create(InvoiceConfig.builder().jobLogDirectory(logDirectory).build())
                .process(input, temporaryDirectory.resolve("output-esc.xlsx"), JobContext.of("esc-123"));
        
        Path jobLog = result.jobLogFile().orElseThrow();
        String content = Files.readString(jobLog);
        
        // bad\"value should be serialized as "bad\\\"value"
        assertThat(content).contains("value=\"bad\\\\\\\"value\"");
    }

    @Test
    void fatalFailureEscapingFix() throws Exception {
        Path input = temporaryDirectory.resolve("bad-escaping-fatal.xlsx");
        try (Workbook workbook = new XSSFWorkbook(); var output = Files.newOutputStream(input)) {
            workbook.createSheet("Items");
            workbook.write(output);
        }

        Path logDirectory = temporaryDirectory.resolve("logs-esc-fatal");
        
        assertThatThrownBy(() -> InvoiceProcessor.create(InvoiceConfig.builder().jobLogDirectory(logDirectory).sheetName("bad\\\"sheet").build())
                .process(input, temporaryDirectory.resolve("output-esc-fatal.xlsx"), JobContext.of("esc-fatal")))
                .isInstanceOf(ExcelFormatException.class);

        Path jobLog = Files.list(logDirectory).findFirst().orElseThrow();
        String content = Files.readString(jobLog);
        
        // The exception message contains: sheet 'bad\"sheet' was not found
        // Sanitized and quoted: "sheet 'bad\\\"sheet' was not found; available sheets: [Items]"
        assertThat(content).contains("message=\"sheet 'bad\\\\\\\"sheet' was not found; available sheets: [Items]\"");
    }
}
