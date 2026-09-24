package com.company.invoice.xlsx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BatchProcessingTest {

    @TempDir
    Path tempDir;

    private InvoiceProcessor processor;
    private Path goodFile;
    private Path badFile;

    @BeforeEach
    void setUp() throws Exception {
        processor = InvoiceProcessor.create(InvoiceConfig.builder().build());
        goodFile = tempDir.resolve("good.xlsx");
        badFile = tempDir.resolve("bad.xlsx");

        try (Workbook workbook = new XSSFWorkbook(); var output = Files.newOutputStream(goodFile)) {
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

        Files.writeString(badFile, "not a valid xlsx");
    }

    @Test
    void allSuccessBatch() {
        List<BatchJob> jobs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            jobs.add(BatchJob.of(goodFile, tempDir.resolve("out-" + i + ".xlsx"), JobContext.of("job" + i)));
        }

        BatchResult result = processor.processBatch(jobs);
        
        assertThat(result.hasFailures()).isFalse();
        assertThat(result.successCount()).isEqualTo(5);
        assertThat(result.items()).hasSize(5);
        for (int i = 0; i < 5; i++) {
            assertThat(result.items().get(i).job()).isEqualTo(jobs.get(i));
            assertThat(result.items().get(i).succeeded()).isTrue();
        }
    }

    @Test
    void mixedBatchIsolatesFailures() {
        List<BatchJob> jobs = List.of(
            BatchJob.of(goodFile, tempDir.resolve("mixed-out-0.xlsx")),
            BatchJob.of(badFile, tempDir.resolve("mixed-out-1.xlsx")),
            BatchJob.of(goodFile, tempDir.resolve("mixed-out-2.xlsx"))
        );

        BatchResult result = processor.processBatch(jobs);

        assertThat(result.hasFailures()).isTrue();
        assertThat(result.successCount()).isEqualTo(2);
        assertThat(result.failureCount()).isEqualTo(1);
        
        assertThat(result.items().get(0).succeeded()).isTrue();
        assertThat(result.items().get(1).succeeded()).isFalse();
        assertThat(result.items().get(1).error()).isPresent();
        assertThat(result.items().get(1).error().get()).isInstanceOf(ExcelFormatException.class);
        assertThat(result.items().get(2).succeeded()).isTrue();
    }

    @Test
    void stopOnFirstFailure() {
        List<BatchJob> jobs = new ArrayList<>();
        jobs.add(BatchJob.of(badFile, tempDir.resolve("stop-out-0.xlsx")));
        for (int i = 1; i < 10; i++) {
            jobs.add(BatchJob.of(goodFile, tempDir.resolve("stop-out-" + i + ".xlsx")));
        }

        BatchOptions options = BatchOptions.builder().stopOnFirstFailure(true).maxConcurrency(1).build();
        BatchResult result = processor.processBatch(jobs, options);

        assertThat(result.hasFailures()).isTrue();
        // The exact count depends on concurrency and scheduling, but since maxConcurrency=1, 
        // the first one fails and subsequent jobs shouldn't even execute or be in the result.
        // Wait, jobs array has 10 items, but the result should have 1 item because we skip the rest.
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).succeeded()).isFalse();
    }

    @Test
    void callerSuppliedExecutorIsNotShutdown() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            BatchOptions options = BatchOptions.builder().executor(executor).build();
            List<BatchJob> jobs = List.of(BatchJob.of(goodFile, tempDir.resolve("exec-out.xlsx")));
            processor.processBatch(jobs, options);
            assertThat(executor.isShutdown()).isFalse();
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void rejectsSameInputOutput() {
        assertThatThrownBy(() -> BatchJob.of(goodFile, goodFile))
            .isInstanceOf(InvoiceIoException.class)
            .hasMessageContaining("must not be the same");
    }

    @Test
    void concurrencyWithHighJobCount() {
        List<BatchJob> jobs = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            jobs.add(BatchJob.of(goodFile, tempDir.resolve("concurrent-out-" + i + ".xlsx")));
        }
        
        BatchOptions options = BatchOptions.builder().maxConcurrency(5).build();
        long start = System.nanoTime();
        BatchResult result = processor.processBatch(jobs, options);
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        
        assertThat(result.successCount()).isEqualTo(20);
        System.out.println("20 jobs with maxConcurrency 5 completed in " + elapsed + "ms");
    }
}
