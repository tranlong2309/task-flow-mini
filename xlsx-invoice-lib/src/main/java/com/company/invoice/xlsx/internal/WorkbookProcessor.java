package com.company.invoice.xlsx.internal;

import com.company.invoice.xlsx.model.ColumnMapping;
import com.company.invoice.xlsx.error.ErrorCode;
import com.company.invoice.xlsx.error.ExcelFormatException;
import com.company.invoice.xlsx.model.Invoice;
import com.company.invoice.xlsx.InvoiceConfig;
import com.company.invoice.xlsx.error.InvoiceException;
import com.company.invoice.xlsx.model.InvoiceItem;
import com.company.invoice.xlsx.model.InvoiceLine;
import com.company.invoice.xlsx.model.InvoiceTotals;
import com.company.invoice.xlsx.error.InvoiceIoException;
import com.company.invoice.xlsx.ProcessingResult;
import com.company.invoice.xlsx.model.RowError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/** Coordinates content validation, SAX parsing, calculation, and atomic writing. */
public final class WorkbookProcessor {
    private final InvoiceConfig config;

    public WorkbookProcessor(InvoiceConfig config) {
        this.config = config;
    }

    public ProcessingResult process(Path input, Path output) {
        return process(input, output, new JobExecution(config, new com.company.invoice.xlsx.JobContext()));
    }

    public ProcessingResult process(Path input, Path output, JobExecution execution) {
        Path temporaryOutput = null;
        try {
            validateInputFile(input);
            Path outputDirectory = output.toAbsolutePath().normalize().getParent();
            if (outputDirectory == null) outputDirectory = Path.of(".").toAbsolutePath();
            Files.createDirectories(outputDirectory);
            temporaryOutput = outputDirectory.resolve(".xlsx-invoice-" + UUID.randomUUID() + ".tmp");
            ProcessingResult state = parse(input, temporaryOutput, execution);
            moveAtomically(temporaryOutput, output);
            temporaryOutput = null;
            return state;
        } catch (InvoiceException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new InvoiceIoException("invoice processing was interrupted", exception);
        } catch (Exception exception) {
            throw new InvoiceIoException("invoice processing failed", exception);
        } finally {
            deleteQuietly(temporaryOutput);
        }
    }

    public ProcessingResult process(InputStream input, OutputStream output) {
        return process(input, output, new JobExecution(config, new com.company.invoice.xlsx.JobContext()));
    }

    public ProcessingResult process(InputStream input, OutputStream output, JobExecution execution) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(output, "output");
        Path spool = null;
        Path temporaryOutput = null;
        try {
            Path directory = config.tempDirectory() == null ? Path.of(System.getProperty("java.io.tmpdir")) : config.tempDirectory();
            Files.createDirectories(directory);
            spool = directory.resolve("xlsx-invoice-input-" + UUID.randomUUID() + ".xlsx");
            copyBounded(input, spool, config.maxInputBytes());
            Path destination = directory.resolve("xlsx-invoice-output-" + UUID.randomUUID() + ".xlsx");
            temporaryOutput = destination;
            ProcessingResult state = parse(spool, temporaryOutput, execution);
            try (InputStream generated = Files.newInputStream(temporaryOutput)) {
                generated.transferTo(output);
            }
            return state;
        } catch (InvoiceException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new InvoiceIoException("invoice processing was interrupted", exception);
        } catch (Exception exception) {
            throw new InvoiceIoException("invoice processing failed", exception);
        } finally {
            deleteQuietly(spool);
            deleteQuietly(temporaryOutput);
        }
    }

    public static Invoice calculate(InvoiceConfig config, List<InvoiceItem> items) {
        Calculator.CalculationResult result = Calculator.calculate(config, items, "memory");
        return new Invoice(result.lines(), result.totals(), result.errors());
    }

    @SuppressWarnings("deprecation")
    private ProcessingResult parse(Path input, Path temporaryOutput, JobExecution execution) throws Exception {
        try (OPCPackage packageHandle = OPCPackage.open(input.toFile(), PackageAccess.READ)) {
            XSSFReader reader = new XSSFReader(packageHandle);
            ReadOnlySharedStringsTable sharedStrings = new ReadOnlySharedStringsTable(packageHandle);
            StylesTable styles = reader.getStylesTable();
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();
            int selectedIndex = selectSheet(reader, sheets);
            sheets = (XSSFReader.SheetIterator) reader.getSheetsData();
            String sheetName = null;
            InputStream sheetStream = null;
            int currentIndex = 0;
            while (sheets.hasNext()) {
                InputStream candidate = sheets.next();
                String candidateName = sheets.getSheetName();
                if (currentIndex == selectedIndex || (config.sheetName() != null && config.sheetName().equals(candidateName))) {
                    sheetName = candidateName;
                    sheetStream = candidate;
                    break;
                }
                candidate.close();
                currentIndex++;
            }
            if (sheetStream == null) throw new ExcelFormatException("selected sheet was not found");
            InputStream selectedSheet = sheetStream;
            try (selectedSheet) {
                ProcessingHandler handler = new ProcessingHandler(config, sheetName, temporaryOutput, execution);
                XMLReader xmlReader = XMLReaderFactory.createXMLReader();
                xmlReader.setContentHandler(new XSSFSheetXMLHandler(styles, null, sharedStrings, handler,
                    new NumericPreservingDataFormatter(), false));
                xmlReader.parse(new InputSource(selectedSheet));
                return handler.result();
            }
        } catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException exception) {
            throw new ExcelFormatException("input is not a valid OOXML workbook", exception);
        }
    }

    private int selectSheet(XSSFReader reader, XSSFReader.SheetIterator sheets) throws IOException {
        List<String> names = new ArrayList<>();
        while (sheets.hasNext()) {
            InputStream sheetStream = sheets.next();
            try {
                names.add(sheets.getSheetName());
            } finally {
                sheetStream.close();
            }
        }
        if (config.sheetName() != null) {
            int index = names.indexOf(config.sheetName());
            if (index < 0) throw new ExcelFormatException("sheet '" + config.sheetName() + "' was not found; available sheets: " + names);
            return index;
        }
        if (config.sheetIndex() != null) {
            if (config.sheetIndex() < 0 || config.sheetIndex() >= names.size()) {
                throw new ExcelFormatException("sheet index " + config.sheetIndex() + " was not found; available sheets: " + names);
            }
            return config.sheetIndex();
        }
        if (names.isEmpty()) throw new ExcelFormatException("workbook contains no sheets");
        return 0;
    }

    private void validateInputFile(Path input) throws IOException {
        if (!Files.isRegularFile(input)) throw new ExcelFormatException("input workbook does not exist");
        if (Files.size(input) > config.maxInputBytes()) throw new ExcelFormatException("input workbook exceeds maxInputBytes");
        try (InputStream stream = Files.newInputStream(input)) {
            byte[] signature = stream.readNBytes(4);
            if (signature.length != 4 || signature[0] != 'P' || signature[1] != 'K') {
                throw new ExcelFormatException("input is not an OOXML workbook");
            }
        }
    }

    private static void copyBounded(InputStream input, Path target, long maxInputBytes) throws IOException {
        byte[] buffer = new byte[8192];
        try (OutputStream output = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
            long copied = 0;
            int read;
            while ((read = input.read(buffer)) >= 0) {
                copied += read;
                if (copied > maxInputBytes) throw new ExcelFormatException("input stream exceeds maxInputBytes");
                output.write(buffer, 0, read);
                if (Thread.interrupted()) throw new InterruptedException();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new InvoiceIoException("invoice processing was interrupted", exception);
        }
    }

    private static void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteQuietly(Path path) {
        if (path != null) {
            try { Files.deleteIfExists(path); } catch (IOException ignored) { }
        }
    }

    private final class ProcessingHandler implements XSSFSheetXMLHandler.SheetContentsHandler {
        private final InvoiceConfig handlerConfig;
        private final String sheetName;
        private final Path temporaryOutput;
        private final ErrorCollector errors;
        private final JobExecution execution;
        private final Map<Integer, String> cells = new HashMap<>();
        private final List<String> candidateHeaders = new ArrayList<>();
        private WorkbookWriter writer;
        private Map<InvoiceColumn, Integer> mapping;
        private ColumnMapping columnMapping;
        private int currentRow;
        private int nonBlankRows;
        private long dataRowCount;
        private long processedRowCount;
        private long skippedRowCount;
        private int outputLineNumber;
        private BigDecimal beforeTotal = BigDecimal.ZERO;
        private BigDecimal vatTotal = BigDecimal.ZERO;
        private BigDecimal afterTotal = BigDecimal.ZERO;
        private boolean sawDataRow;

        private ProcessingHandler(InvoiceConfig config, String sheetName, Path temporaryOutput, JobExecution execution) {
            this.handlerConfig = config;
            this.sheetName = sheetName;
            this.temporaryOutput = temporaryOutput;
            this.errors = new ErrorCollector(config.maxReportedErrors());
            this.execution = execution;
        }

        @Override public void startRow(int row) { currentRow = row + 1; cells.clear(); }

        @Override public void endRow(int rowNumber) {
            if (cells.values().stream().allMatch(String::isBlank)) return;
            if (mapping == null) {
                nonBlankRows++;
                candidateHeaders.clear();
                for (int index = 0; index <= cells.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1); index++) {
                    candidateHeaders.add(cells.getOrDefault(index, ""));
                }
                Map<InvoiceColumn, Integer> detected = detectMapping(candidateHeaders);
                if (detected.size() == InvoiceColumn.values().length) {
                    mapping = detected;
                    columnMapping = createColumnMapping(candidateHeaders);
                    writer = new WorkbookWriter(handlerConfig);
                    execution.header(sheetName, currentRow, mapping.toString(), columnMapping.ignoredColumns().size());
                } else if (nonBlankRows >= handlerConfig.headerSearchLimit()) {
                    throw new ExcelFormatException("Header row not found in sheet '" + sheetName + "' within first " + handlerConfig.headerSearchLimit() + " non-blank rows");
                }
                return;
            }
            sawDataRow = true;
            dataRowCount++;
            processDataRow();
        }

        @Override public void cell(String cellReference, String formattedValue, org.apache.poi.xssf.usermodel.XSSFComment comment) {
            int index = columnIndex(cellReference);
            cells.put(index, formattedValue == null ? "" : formattedValue.trim());
        }

        private Map<InvoiceColumn, Integer> detectMapping(List<String> headers) {
            Map<InvoiceColumn, Integer> detected = new LinkedHashMap<>();
            for (int index = 0; index < headers.size(); index++) {
                InvoiceColumn column = InvoiceColumn.fromHeader(headers.get(index), handlerConfig.headerAliases());
                if (column != null && detected.putIfAbsent(column, index) != null) {
                    throw new ExcelFormatException("duplicate required column '" + column.headerName() + "' in header row " + currentRow);
                }
            }
            return detected;
        }

        private ColumnMapping createColumnMapping(List<String> headers) {
            Map<String, Integer> indexes = new LinkedHashMap<>();
            Map<String, String> letters = new LinkedHashMap<>();
            List<String> ignored = new ArrayList<>();
            for (int index = 0; index < headers.size(); index++) {
                InvoiceColumn column = InvoiceColumn.fromHeader(headers.get(index), handlerConfig.headerAliases());
                if (column == null) ignored.add(headers.get(index));
                else { indexes.put(column.headerName(), index); letters.put(column.headerName(), excelColumn(index)); }
            }
            return new ColumnMapping(sheetName, currentRow, indexes, letters, ignored);
        }

        private void processDataRow() {
            String itemName = value(InvoiceColumn.ITEM_NAME);
            String quantityText = value(InvoiceColumn.QUANTITY);
            String unitPriceText = value(InvoiceColumn.UNIT_PRICE);
            String vatRateText = value(InvoiceColumn.VAT_RATE);
            List<RowError> rowErrors = new ArrayList<>();
            BigDecimal quantity = parseNumber(quantityText, InvoiceColumn.QUANTITY, rowErrors);
            BigDecimal unitPrice = parseNumber(unitPriceText, InvoiceColumn.UNIT_PRICE, rowErrors);
            BigDecimal vatRate = parseNumber(vatRateText, InvoiceColumn.VAT_RATE, rowErrors);
            if (itemName.isBlank()) rowErrors.add(error(InvoiceColumn.ITEM_NAME, itemName, ErrorCode.MISSING_VALUE, "value is required"));
            if (itemName.length() > 255) rowErrors.add(error(InvoiceColumn.ITEM_NAME, itemName, ErrorCode.TEXT_TOO_LONG, "item_name exceeds 255 characters"));
            if (quantity != null && quantity.signum() <= 0) rowErrors.add(error(InvoiceColumn.QUANTITY, quantityText, ErrorCode.OUT_OF_RANGE, "value is out of range (must be greater than 0)"));
            if (quantity != null && quantity.signum() > 0 && quantity.scale() > 3) rowErrors.add(error(InvoiceColumn.QUANTITY, quantityText, ErrorCode.TOO_MANY_DECIMALS, "quantity has more than 3 decimal places"));
            if (unitPrice != null && unitPrice.signum() < 0) rowErrors.add(error(InvoiceColumn.UNIT_PRICE, unitPriceText, ErrorCode.OUT_OF_RANGE, "value must not be negative"));
            if (vatRate != null && (vatRate.signum() < 0 || vatRate.compareTo(BigDecimal.valueOf(100)) > 0)) rowErrors.add(error(InvoiceColumn.VAT_RATE, vatRateText, ErrorCode.OUT_OF_RANGE, "value must be between 0 and 100"));
            if (!rowErrors.isEmpty()) {
                addErrors(rowErrors);
                skippedRowCount++;
                execution.progress(dataRowCount, processedRowCount, skippedRowCount);
                return;
            }
            Calculator.CalculationResult calculation = Calculator.calculate(handlerConfig, List.of(new InvoiceItem(itemName, quantity, unitPrice, vatRate)), sheetName);
            if (!calculation.errors().isEmpty()) {
                addErrors(calculation.errors());
                skippedRowCount++;
                execution.progress(dataRowCount, processedRowCount, skippedRowCount);
                return;
            }
            InvoiceLine line = calculation.lines().get(0);
            line = new InvoiceLine(++outputLineNumber, line.itemName(), line.quantity(), line.unitPrice(), line.vatRate(), line.amountBeforeVat(), line.vatAmount(), line.amountAfterVat());
            writer.writeLine(line, handlerConfig.scale());
            processedRowCount++;
            beforeTotal = beforeTotal.add(line.amountBeforeVat());
            vatTotal = vatTotal.add(line.vatAmount());
            afterTotal = afterTotal.add(line.amountAfterVat());
            execution.progress(dataRowCount, processedRowCount, skippedRowCount);
        }

        private BigDecimal parseNumber(String value, InvoiceColumn column, List<RowError> rowErrors) {
            if (value.isBlank()) { rowErrors.add(error(column, value, ErrorCode.MISSING_VALUE, "value is required")); return null; }
            if (NumericPreservingDataFormatter.isPercent(value) && column != InvoiceColumn.VAT_RATE) {
                rowErrors.add(error(column, value, ErrorCode.INVALID_FORMAT, "percentage format is allowed only for vat_rate"));
                return null;
            }
            if (NumericPreservingDataFormatter.isPercent(value)) value = NumericPreservingDataFormatter.removePercentMarker(value);
            if (value.contains(",") || value.contains("%")) { rowErrors.add(error(column, value, ErrorCode.INVALID_FORMAT, "value has an invalid number format")); return null; }
            try { return new BigDecimal(value); }
            catch (NumberFormatException exception) { rowErrors.add(error(column, value, ErrorCode.NOT_A_NUMBER, "'" + value + "' is not a valid number")); return null; }
        }

        private String value(InvoiceColumn column) { return cells.getOrDefault(mapping.get(column), ""); }
        private RowError error(InvoiceColumn column, String value, ErrorCode code, String reason) {
            int index = mapping.get(column);
            String cell = excelColumn(index) + currentRow;
            return new RowError(sheetName, currentRow, cell, column.headerName(), value, code,
                    "Sheet '" + sheetName + "', row " + currentRow + ", cell " + cell + " (column '" + column.headerName() + "'): " + reason);
        }
        private void addErrors(List<RowError> rowErrors) {
            rowErrors.forEach(error -> {
                errors.add(error);
                execution.rowError(error);
            });
        }

        private ProcessingResult result() {
            if (mapping == null) throw new ExcelFormatException("Header row not found in sheet '" + sheetName + "'");
            if (!sawDataRow) throw new ExcelFormatException("sheet '" + sheetName + "' has a header but no data rows");
            InvoiceTotals totals = new InvoiceTotals(beforeTotal.setScale(handlerConfig.scale(), handlerConfig.roundingMode()),
                    vatTotal.setScale(handlerConfig.scale(), handlerConfig.roundingMode()), afterTotal.setScale(handlerConfig.scale(), handlerConfig.roundingMode()));
            try (OutputStream output = Files.newOutputStream(temporaryOutput)) {
                writer.writeTotal(totals);
                writer.writeErrors(handlerConfig, errors.values(), errors.omitted());
                writer.write(output);
            } catch (IOException exception) {
                throw new InvoiceIoException("could not write output workbook", exception);
            } finally { writer.close(); }
                return new ProcessingResult(totals, dataRowCount, processedRowCount, skippedRowCount, errors.values(), errors.omitted() > 0,
                    sheetName, columnMapping.headerRowNumber(), columnMapping, execution.jobId(), execution.jobLogFile());
        }
    }

    private static int columnIndex(String reference) {
        int result = 0;
        for (int index = 0; index < reference.length() && Character.isLetter(reference.charAt(index)); index++) {
            result = result * 26 + Character.toUpperCase(reference.charAt(index)) - 'A' + 1;
        }
        return result - 1;
    }

    private static String excelColumn(int index) {
        StringBuilder result = new StringBuilder();
        int value = index + 1;
        while (value > 0) { int remainder = (value - 1) % 26; result.insert(0, (char) ('A' + remainder)); value = (value - 1) / 26; }
        return result.toString();
    }

    private static final class ErrorCollector {
        private final int limit;
        private final List<RowError> values = new ArrayList<>();
        private long omitted;
        private ErrorCollector(int limit) { this.limit = limit; }
        private void add(RowError error) { if (values.size() < limit) values.add(error); else omitted++; }
        private List<RowError> values() { return List.copyOf(values); }
        private long omitted() { return omitted; }
    }
}
