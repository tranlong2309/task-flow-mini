package com.company.invoice.xlsx;

import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/** Immutable configuration for {@link InvoiceProcessor}. */
public final class InvoiceConfig {
    private static final int DEFAULT_SCALE = 2;
    private static final int DEFAULT_HEADER_SEARCH_LIMIT = 20;
    private static final int DEFAULT_MAX_REPORTED_ERRORS = 10_000;
    private static final long DEFAULT_MAX_INPUT_BYTES = 50L * 1024L * 1024L;

    private final int scale;
    private final RoundingMode roundingMode;
    private final String sheetName;
    private final Integer sheetIndex;
    private final int headerSearchLimit;
    private final Map<String, String> headerAliases;
    private final String outputSheetName;
    private final boolean writeErrorSheet;
    private final String errorSheetName;
    private final int maxReportedErrors;
    private final long maxInputBytes;
    private final Path tempDirectory;
    private final Path jobLogDirectory;
    private final boolean logTotals;
    private final long progressLogInterval;
    private final int logValueMaxLength;

    private InvoiceConfig(Builder builder) {
        this.scale = builder.scale;
        this.roundingMode = builder.roundingMode;
        this.sheetName = builder.sheetName;
        this.sheetIndex = builder.sheetIndex;
        this.headerSearchLimit = builder.headerSearchLimit;
        this.headerAliases = Map.copyOf(builder.headerAliases);
        this.outputSheetName = builder.outputSheetName;
        this.writeErrorSheet = builder.writeErrorSheet;
        this.errorSheetName = builder.errorSheetName;
        this.maxReportedErrors = builder.maxReportedErrors;
        this.maxInputBytes = builder.maxInputBytes;
        this.tempDirectory = builder.tempDirectory;
        this.jobLogDirectory = builder.jobLogDirectory;
        this.logTotals = builder.logTotals;
        this.progressLogInterval = builder.progressLogInterval;
        this.logValueMaxLength = builder.logValueMaxLength;
    }

    /**
     * Returns a builder initialized with the documented defaults.
     * @return mutable configuration builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** @return monetary scale */ public int scale() { return scale; }
    /** @return configured rounding mode */ public RoundingMode roundingMode() { return roundingMode; }
    /** @return selected sheet name, or null */ public String sheetName() { return sheetName; }
    /** @return selected zero-based sheet index, or null */ public Integer sheetIndex() { return sheetIndex; }
    /** @return header search limit */ public int headerSearchLimit() { return headerSearchLimit; }
    /** @return immutable header aliases */ public Map<String, String> headerAliases() { return headerAliases; }
    /** @return output sheet name */ public String outputSheetName() { return outputSheetName; }
    /** @return whether to write the Errors sheet */ public boolean writeErrorSheet() { return writeErrorSheet; }
    /** @return Errors sheet name */ public String errorSheetName() { return errorSheetName; }
    /** @return maximum retained row errors */ public int maxReportedErrors() { return maxReportedErrors; }
    /** @return maximum accepted input size in bytes */ public long maxInputBytes() { return maxInputBytes; }
    /** @return temporary directory, or null for the system default */ public Path tempDirectory() { return tempDirectory; }
    /** @return optional per-job log directory */ public Path jobLogDirectory() { return jobLogDirectory; }
    /** @return whether totals are included in diagnostic logs */ public boolean logTotals() { return logTotals; }
    /** @return progress event interval, or zero when disabled */ public long progressLogInterval() { return progressLogInterval; }
    /** @return maximum logged cell-value length */ public int logValueMaxLength() { return logValueMaxLength; }

    /** Mutable builder used only while constructing an immutable configuration. */
    public static final class Builder {
        private int scale = DEFAULT_SCALE;
        private RoundingMode roundingMode = RoundingMode.HALF_UP;
        private String sheetName;
        private Integer sheetIndex;
        private int headerSearchLimit = DEFAULT_HEADER_SEARCH_LIMIT;
        private Map<String, String> headerAliases = Map.of();
        private String outputSheetName = "Invoice";
        private boolean writeErrorSheet = true;
        private String errorSheetName = "Errors";
        private int maxReportedErrors = DEFAULT_MAX_REPORTED_ERRORS;
        private long maxInputBytes = DEFAULT_MAX_INPUT_BYTES;
        private Path tempDirectory;
        private Path jobLogDirectory;
        private boolean logTotals;
        private long progressLogInterval = 10_000;
        private int logValueMaxLength = 100;

        /**
         * Sets the monetary scale.
         * @param value scale from 0 through 15
         * @return this builder
         */
        public Builder scale(int value) { this.scale = value; return this; }
        /**
         * Sets the monetary rounding mode.
         * @param value rounding mode
         * @return this builder
         */
        public Builder roundingMode(RoundingMode value) { this.roundingMode = value; return this; }
        /**
         * Selects a sheet by name.
         * @param value sheet name
         * @return this builder
         */
        public Builder sheetName(String value) { this.sheetName = value; this.sheetIndex = null; return this; }
        /**
         * Selects a sheet by zero-based index.
         * @param value zero-based sheet index
         * @return this builder
         */
        public Builder sheetIndex(Integer value) { this.sheetIndex = value; this.sheetName = null; return this; }
        /**
         * Sets the number of non-blank rows searched for the header.
         * @param value non-blank-row search limit
         * @return this builder
         */
        public Builder headerSearchLimit(int value) { this.headerSearchLimit = value; return this; }
        /**
         * Sets header aliases.
         * @param value alias-to-required-header map
         * @return this builder
         */
        public Builder headerAliases(Map<String, String> value) { this.headerAliases = Map.copyOf(value); return this; }
        /**
         * Sets the output invoice sheet name.
         * @param value output sheet name
         * @return this builder
         */
        public Builder outputSheetName(String value) { this.outputSheetName = value; return this; }
        /**
         * Enables or disables the Errors sheet.
         * @param value whether to write Errors
         * @return this builder
         */
        public Builder writeErrorSheet(boolean value) { this.writeErrorSheet = value; return this; }
        /**
         * Sets the Errors sheet name.
         * @param value Errors sheet name
         * @return this builder
         */
        public Builder errorSheetName(String value) { this.errorSheetName = value; return this; }
        /**
         * Sets the maximum number of retained row errors.
         * @param value retained error limit
         * @return this builder
         */
        public Builder maxReportedErrors(int value) { this.maxReportedErrors = value; return this; }
        /**
         * Sets the maximum input size.
         * @param value input byte limit
         * @return this builder
         */
        public Builder maxInputBytes(long value) { this.maxInputBytes = value; return this; }
        /**
         * Sets the temporary directory.
         * @param value absolute temporary directory
         * @return this builder
         */
        public Builder tempDirectory(Path value) { this.tempDirectory = value; return this; }
        /** 
         * Sets the job-log directory.
         * @param value job-log directory, or null to disable job logs 
         * @return this builder 
         */
        public Builder jobLogDirectory(Path value) { this.jobLogDirectory = value; return this; }
        
        /** 
         * Sets whether to include totals in diagnostic logs.
         * @param value whether to include totals in logs 
         * @return this builder 
         */
        public Builder logTotals(boolean value) { this.logTotals = value; return this; }
        
        /** 
         * Sets the progress event interval.
         * @param value progress interval, zero disables progress 
         * @return this builder 
         */
        public Builder progressLogInterval(long value) { this.progressLogInterval = value; return this; }
        
        /** 
         * Sets the maximum logged cell-value length.
         * @param value maximum logged value length 
         * @return this builder 
         */
        public Builder logValueMaxLength(int value) { this.logValueMaxLength = value; return this; }

        /**
         * Builds and validates the immutable configuration.
         * @return immutable configuration
         * @throws IllegalArgumentException if a setting is invalid
         */
        public InvoiceConfig build() {
            if (scale < 0 || scale > 15) throw new IllegalArgumentException("scale must be between 0 and 15");
            if (roundingMode == null) throw new IllegalArgumentException("roundingMode is required");
            if (headerSearchLimit < 1) throw new IllegalArgumentException("headerSearchLimit must be positive");
            if (maxReportedErrors < 1) throw new IllegalArgumentException("maxReportedErrors must be positive");
            if (maxInputBytes < 1) throw new IllegalArgumentException("maxInputBytes must be positive");
            if (progressLogInterval < 0) throw new IllegalArgumentException("progressLogInterval must not be negative");
            if (logValueMaxLength < 1) throw new IllegalArgumentException("logValueMaxLength must be positive");
            requireName(outputSheetName, "outputSheetName");
            requireName(errorSheetName, "errorSheetName");
            if (outputSheetName.equals(errorSheetName)) throw new IllegalArgumentException("sheet names must differ");
            if (tempDirectory != null && !tempDirectory.isAbsolute()) {
                throw new IllegalArgumentException("tempDirectory must be absolute");
            }
            return new InvoiceConfig(this);
        }

        private static void requireName(String value, String fieldName) {
            if (Objects.requireNonNullElse(value, "").isBlank()) throw new IllegalArgumentException(fieldName + " is required");
        }
    }
}
