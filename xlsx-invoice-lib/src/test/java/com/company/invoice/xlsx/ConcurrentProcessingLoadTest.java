package com.company.invoice.xlsx;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("performance")
class ConcurrentProcessingLoadTest {

    @TempDir
    Path tempDir;

    @ParameterizedTest
    @ValueSource(ints = {10, 100, 1000})
    void testConcurrency(int concurrencyLevel) throws Exception {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int poolSize = Math.min(concurrencyLevel, availableProcessors * 20);
        
        System.out.printf("Running concurrency test level %d (pool size %d)%n", concurrencyLevel, poolSize);
        
        Path input = tempDir.resolve("input-" + concurrencyLevel + ".xlsx");
        LargeWorkbookFixtures.generate(input, 5000, false);
        
        InvoiceProcessor processor = InvoiceProcessor.create(InvoiceConfig.builder().build());
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        List<Callable<Long>> tasks = new ArrayList<>();
        
        for (int i = 0; i < concurrencyLevel; i++) {
            final int index = i;
            tasks.add(() -> {
                long start = System.nanoTime();
                Path output = tempDir.resolve("output-" + concurrencyLevel + "-" + index + ".xlsx");
                ProcessingResult result = processor.process(input, output, JobContext.of("job-" + index));
                assertThat(result.processedRowCount()).isEqualTo(5000);
                return (System.nanoTime() - start) / 1_000_000; // latency in ms
            });
        }
        
        long totalStart = System.nanoTime();
        List<Future<Long>> futures = executor.invokeAll(tasks);
        long totalElapsed = (System.nanoTime() - totalStart) / 1_000_000;
        
        long totalLatency = 0;
        for (Future<Long> future : futures) {
            totalLatency += future.get();
        }
        
        executor.shutdown();
        
        System.out.printf("Level %d -> Total Wall Time: %d ms, Avg Latency per Call: %d ms%n%n", 
                concurrencyLevel, totalElapsed, totalLatency / concurrencyLevel);
    }
}
