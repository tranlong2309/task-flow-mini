package com.company.invoice.xlsx.batch;

import java.util.concurrent.ExecutorService;

/** Configuration for batch processing execution. */
public final class BatchOptions {
    private final int maxConcurrency;
    private final boolean stopOnFirstFailure;
    private final ExecutorService executor;

    private BatchOptions(Builder builder) {
        if (builder.maxConcurrency < 1) {
            throw new IllegalArgumentException("maxConcurrency must be >= 1");
        }
        this.maxConcurrency = builder.maxConcurrency;
        this.stopOnFirstFailure = builder.stopOnFirstFailure;
        this.executor = builder.executor;
    }

    /** @return maximum allowed concurrent jobs */
    public int maxConcurrency() { return maxConcurrency; }
    /** @return true if the batch should stop submitting new jobs after a failure */
    public boolean stopOnFirstFailure() { return stopOnFirstFailure; }
    /** @return the optional caller-supplied executor service */
    public ExecutorService executor() { return executor; }

    /** @return a new options builder */
    public static Builder builder() { return new Builder(); }

    /** Builder for BatchOptions. */
    public static final class Builder {
        private int maxConcurrency = Math.max(1, Runtime.getRuntime().availableProcessors());
        private boolean stopOnFirstFailure = false;
        private ExecutorService executor = null;

        private Builder() {}

        /** 
         * Sets the maximum concurrency level. Defaults to available processors.
         * For CPU-bound workloads, the default is optimal.
         * For I/O-bound workloads (e.g. slow network storage), it is recommended to 
         * increase this value significantly or use a Virtual Thread-based executor.
         * @param value concurrency level (>= 1)
         * @return this builder
         */
        public Builder maxConcurrency(int value) { this.maxConcurrency = value; return this; }
        
        /** 
         * Sets whether to abort submitting further jobs if one fails. Defaults to false.
         * @param value true to abort on first failure
         * @return this builder
         */
        public Builder stopOnFirstFailure(boolean value) { this.stopOnFirstFailure = value; return this; }
        
        /** 
         * Provides a custom executor service. 
         * If provided, the processor will not shut it down.
         * @param value caller-managed executor
         * @return this builder
         */
        public Builder executor(ExecutorService value) { this.executor = value; return this; }

        /** @return the immutable configuration */
        public BatchOptions build() { return new BatchOptions(this); }
    }
}
