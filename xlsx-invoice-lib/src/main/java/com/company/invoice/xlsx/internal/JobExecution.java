package com.company.invoice.xlsx.internal;

import com.company.invoice.xlsx.InvoiceConfig;
import com.company.invoice.xlsx.InvoiceException;
import com.company.invoice.xlsx.JobContext;
import com.company.invoice.xlsx.ProcessingResult;
import com.company.invoice.xlsx.RowError;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/** Owns per-call correlation, diagnostic events, and optional job-log output. */
public final class JobExecution implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(JobExecution.class);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);
    private final InvoiceConfig config;
    private final JobContext context;
    private final String jobId = UUID.randomUUID().toString();
    private final String previousJobId;
    private final String previousCorrelationId;
    private final long startedAt = System.nanoTime();
    private BufferedWriter jobWriter;
    private Path jobLogFile;
    private boolean ended;

    public JobExecution(InvoiceConfig config, JobContext context) {
        this.config = config;
        this.context = context;
        previousJobId = MDC.get("invoiceJobId");
        previousCorrelationId = MDC.get("invoiceCorrelationId");
        MDC.put("invoiceJobId", jobId);
        if (context.correlationId().isPresent()) {
            MDC.put("invoiceCorrelationId", context.correlationId().orElseThrow());
        } else {
            MDC.remove("invoiceCorrelationId");
        }
        openJobLog();
    }

    public String jobId() { return jobId; }
    public Optional<Path> jobLogFile() { return Optional.ofNullable(jobLogFile); }

    public void start(String inputName, long inputBytes, String outputName) {
        sameEvent("INFO", "job.start", "correlationId=" + quote(context.correlationId().orElse("-")),
                "input=" + quote(inputName), "inputBytes=" + inputBytes, "output=" + quote(outputName),
                "scale=" + config.scale(), "roundingMode=" + config.roundingMode());
    }

    public void header(String sheet, int row, String mapping, int ignored) {
        sameEvent("INFO", "header.detected", "sheet=" + quote(sheet), "headerRow=" + row,
                "mapping=" + quote(mapping), "ignoredColumns=" + ignored);
    }

    public void progress(long dataRows, long processedRows, long skippedRows) {
        if (config.progressLogInterval() > 0 && dataRows % config.progressLogInterval() == 0) {
            sameEvent("DEBUG", "job.progress", "dataRows=" + dataRows, "processedRows=" + processedRows,
                    "skippedRows=" + skippedRows);
        }
    }

    public void rowError(RowError error) {
        event("DEBUG", "WARN", "row.error", "row=" + error.rowNumber(), "cell=" + quote(error.cellAddress()),
                "column=" + quote(error.columnName()), "code=" + error.errorCode(),
                "value=" + quote(sanitize(error.offendingValue())), "message=" + quote(sanitize(error.message())));
    }

    public void complete(ProcessingResult result, String outputName) {
        if (result.hasErrors()) {
            Map<String, Long> counts = result.errors().stream()
                .collect(Collectors.groupingBy(error -> error.errorCode().name(), java.util.TreeMap::new, Collectors.counting()));
            sameEvent("WARN", "job.errors.summary", "skippedRows=" + result.skippedRowCount(),
                "errors=" + result.errors().size(), "counts=" + quote(counts.toString()),
                "errorsTruncated=" + result.errorsTruncated());
        }
        String status = result.hasErrors() ? "COMPLETED_WITH_ERRORS" : "COMPLETED";
        String totals = config.logTotals() ? " totalBeforeVat=" + result.totals().totalBeforeVat()
            + " totalVat=" + result.totals().totalVat() + " totalPayable=" + result.totals().totalPayable() : "";
        sameEvent("INFO", "job.end", "status=" + status, "dataRows=" + result.dataRowCount(),
                "processedRows=" + result.processedRowCount(), "skippedRows=" + result.skippedRowCount(),
            "elapsedMs=" + elapsedMillis(), "output=" + quote(outputName) + totals);
        ended = true;
    }

    public void failed(Throwable throwable) {
        sameEvent("WARN", "job.failed", "exceptionType=" + throwable.getClass().getSimpleName(),
                "message=" + quote(sanitize(throwable.getMessage())));
        LOG.debug("job.failed jobId={} exceptionType={}", jobId, throwable.getClass().getSimpleName(), throwable);
    }

    public void interrupted() {
        sameEvent("WARN", "job.interrupted", "jobId=" + jobId);
    }

    private void sameEvent(String level, String event, String... fields) {
        event(level, level, event, fields);
    }

    private void event(String diagnosticLevel, String jobLevel, String event, String... fields) {
        String message = event + " " + String.join(" ", fields);
        try {
            if ("INFO".equals(diagnosticLevel)) LOG.info(message);
            else if ("WARN".equals(diagnosticLevel)) LOG.warn(message);
            else LOG.debug(message);
        } catch (RuntimeException ignored) {
            // Logging must never alter processing.
        }
        writeJobLine(jobLevel, event, fields);
    }

    private void openJobLog() {
        Path directory = config.jobLogDirectory();
        if (directory == null) return;
        try {
            Files.createDirectories(directory);
            jobLogFile = directory.resolve("invoice-job-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
                    .withZone(ZoneOffset.UTC).format(Instant.now()) + "-" + jobId + ".log");
            jobWriter = Files.newBufferedWriter(jobLogFile, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE_NEW, java.nio.file.StandardOpenOption.WRITE);
            trySetOwnerOnlyPermissions(jobLogFile);
        } catch (IOException exception) {
            LOG.warn("joblog.write.failed directory={} reason={}", directory.getFileName(), exception.getClass().getSimpleName());
            jobLogFile = null;
            jobWriter = null;
        }
    }

    private void writeJobLine(String level, String event, String... fields) {
        if (jobWriter == null) return;
        try {
            jobWriter.write(TIMESTAMP.format(Instant.now()) + " " + String.format("%-5s", level) + " [" + jobId + "] " + event);
            for (String field : fields) jobWriter.write(" " + field);
            jobWriter.newLine();
            jobWriter.flush();
        } catch (IOException exception) {
            closeWriter();
            LOG.warn("joblog.write.failed directory={} reason={}",
                    config.jobLogDirectory().getFileName(), exception.getClass().getSimpleName());
        }
    }

    private String sanitize(String value) {
        if (value == null) return "";
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < value.length() && result.length() < config.logValueMaxLength(); index++) {
            char character = value.charAt(index);
            if (character == '\r') result.append("\\r");
            else if (character == '\n') result.append("\\n");
            else if (character == '\t') result.append("\\t");
            else if (Character.isISOControl(character)) result.append(String.format("\\u%04x", (int) character));
            else if (character == '"' || character == '\\') result.append('\\').append(character);
            else result.append(character);
        }
        if (value.length() > config.logValueMaxLength()) result.append("...");
        return result.toString();
    }

    private String quote(String value) { return "\"" + sanitize(value) + "\""; }
    private long elapsedMillis() { return (System.nanoTime() - startedAt) / 1_000_000L; }

    private void closeWriter() {
        if (jobWriter != null) {
            try { jobWriter.close(); } catch (IOException ignored) { }
            jobWriter = null;
        }
    }

    private static void trySetOwnerOnlyPermissions(Path path) {
        try {
            Files.setPosixFilePermissions(path, EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException | IOException ignored) {
            // Windows and filesystems without POSIX permissions are allowed.
        }
    }

    @Override
    public void close() {
        if (!ended) sameEvent("INFO", "job.end", "status=FAILED", "elapsedMs=" + elapsedMillis());
        closeWriter();
        restoreMdc("invoiceJobId", previousJobId);
        restoreMdc("invoiceCorrelationId", previousCorrelationId);
    }

    private static void restoreMdc(String key, String value) {
        if (value == null) MDC.remove(key); else MDC.put(key, value);
    }
}
