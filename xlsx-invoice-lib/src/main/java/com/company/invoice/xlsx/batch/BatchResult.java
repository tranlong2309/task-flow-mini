package com.company.invoice.xlsx.batch;

import java.util.List;

/** 
 * Outcome of a batch processing request. 
 * @param items individual job results in the order they were submitted
 */
public record BatchResult(List<BatchItemResult> items) {
    /** 
     * Creates an immutable batch result. 
     * @param items individual job results
     */
    public BatchResult {
        items = List.copyOf(items);
    }

    /** @return number of successfully completed jobs */
    public long successCount() {
        return items.stream().filter(BatchItemResult::succeeded).count();
    }

    /** @return number of failed jobs */
    public long failureCount() {
        return items.stream().filter(item -> !item.succeeded()).count();
    }

    /** @return true if at least one job failed */
    public boolean hasFailures() {
        return items.stream().anyMatch(item -> !item.succeeded());
    }

    /** @return list of only the failed job results */
    public List<BatchItemResult> failures() {
        return items.stream().filter(item -> !item.succeeded()).toList();
    }
}
