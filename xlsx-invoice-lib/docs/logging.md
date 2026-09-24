# Logging

The library has two independent logging layers. Diagnostic events use `slf4j-api`; the consuming application chooses a provider and destination. An optional per-job log file is enabled with `InvoiceConfig.builder().jobLogDirectory(path).build()`.

## API Example

```java
JobContext context = JobContext.of("order-123");
ProcessingResult result = processor.process(input, output, context);
result.jobId();
result.jobLogFile().ifPresent(path -> support.attach(path));
```

The library creates one random `invoiceJobId` per call and restores the caller's MDC values after processing. It uses MDC keys `invoiceJobId` and `invoiceCorrelationId`. Host patterns can print them with `%X{invoiceJobId}` and `%X{invoiceCorrelationId}`.

## Event Catalog

| Level | Event | Meaning |
| --- | --- | --- |
| INFO | `job.start` | Processing begins |
| INFO | `header.detected` | Required header mapping was found |
| DEBUG | `job.progress` | Configured progress interval reached |
| DEBUG | `row.error` | One row problem was found |
| WARN | `job.failed` | Fatal failure is leaving the library |
| INFO | `job.end` | Processing completed or failed |

The default configuration never logs item names, prices, quantities, VAT rates, or amounts. Row error values are sanitized, quoted, escaped, and truncated to `logValueMaxLength`. Set `logTotals(true)` only when totals are acceptable in operational logs. The library never installs a logging implementation.

## Job Log Files

Job logs are off by default. When enabled, the library creates `invoice-job-<yyyyMMdd-HHmmss>-<jobId>.log` with UTF-8, `CREATE_NEW`, incremental writes, and owner-only POSIX permissions where supported. A job-log failure is isolated from invoice processing and reported through `joblog.write.failed`. Retention is the host's responsibility:

```text
find /var/log/invoice-jobs -type f -name 'invoice-job-*.log' -mtime +30 -delete
```

Each log ends with `job.end`, including fatal jobs. Never put full paths or business data in INFO logs.

## Routing Examples

Logback:

```xml
<configuration>
  <appender name="INVOICE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/invoice-library.log</file>
    <encoder><pattern>%d %-5level [%X{invoiceJobId}] %logger - %msg%n</pattern></encoder>
  </appender>
  <logger name="com.company.invoice.xlsx" level="INFO" additivity="false">
    <appender-ref ref="INVOICE"/>
  </logger>
</configuration>
```

Log4j2:

```xml
<Configuration status="WARN">
  <Appenders>
    <RollingFile name="INVOICE" fileName="logs/invoice-library.log" filePattern="logs/invoice-library-%i.log">
      <PatternLayout pattern="%d %-5level [%X{invoiceJobId}] %logger - %msg%n"/>
      <SizeBasedTriggeringPolicy size="10MB"/>
    </RollingFile>
  </Appenders>
  <Loggers>
    <Logger name="com.company.invoice.xlsx" level="INFO" additivity="false">
      <AppenderRef ref="INVOICE"/>
    </Logger>
    <Root level="WARN"/>
  </Loggers>
</Configuration>
```

Spring Boot `application.yml`:

```yaml
logging:
  file:
    name: logs/invoice-library.log
  level:
    com.company.invoice.xlsx: INFO
```

Log4j2 consumers can route POI's own events through `log4j-to-slf4j` according to the application's dependency policy.