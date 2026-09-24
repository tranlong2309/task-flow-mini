package com.company.invoice.xlsx;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("performance")
class LargeWorkbookPerformanceTest {

    @TempDir
    Path tempDir;

    @Test
    void testPerformanceScaling() throws Exception {
        Path file10k = tempDir.resolve("10k.xlsx");
        Path file100k = tempDir.resolve("100k.xlsx");
        
        LargeWorkbookFixtures.generate(file10k, 10_000, false);
        LargeWorkbookFixtures.generate(file100k, 100_000, false);

        InvoiceProcessor processor = InvoiceProcessor.create(InvoiceConfig.builder().build());

        System.out.println("Running warmup...");
        processor.process(file10k, tempDir.resolve("out-warmup.xlsx"));

        System.out.println("Running 10k test...");
        long start10k = System.nanoTime();
        long memBefore10k = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        processor.process(file10k, tempDir.resolve("out-10k.xlsx"));
        long memAfter10k = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long elapsed10k = (System.nanoTime() - start10k) / 1_000_000;
        
        System.out.println("Running 100k test...");
        long start100k = System.nanoTime();
        long memBefore100k = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        processor.process(file100k, tempDir.resolve("out-100k.xlsx"));
        long memAfter100k = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long elapsed100k = (System.nanoTime() - start100k) / 1_000_000;

        double ratio = (double) elapsed100k / elapsed10k;
        
        System.out.println("\nPerformance Summary:");
        System.out.printf("%-10s | %-10s | %-15s | %-20s%n", "Rows", "Elapsed ms", "ms / 1000 rows", "Peak Heap Delta (MB)");
        System.out.printf("%-10d | %-10d | %-15.2f | %-20.2f%n", 10_000, elapsed10k, elapsed10k / 10.0, (memAfter10k - memBefore10k) / 1024.0 / 1024.0);
        System.out.printf("%-10d | %-10d | %-15.2f | %-20.2f%n", 100_000, elapsed100k, elapsed100k / 100.0, (memAfter100k - memBefore100k) / 1024.0 / 1024.0);
        System.out.println("Scaling Ratio (100k / 10k): " + String.format("%.2fx", ratio));
        
        assertThat(ratio).isBetween(5.0, 15.0);
    }
}
