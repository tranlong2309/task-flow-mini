package com.company.invoice.xlsx.internal;

import com.company.invoice.xlsx.ErrorCode;
import com.company.invoice.xlsx.InvoiceConfig;
import com.company.invoice.xlsx.InvoiceItem;
import com.company.invoice.xlsx.InvoiceLine;
import com.company.invoice.xlsx.InvoiceTotals;
import com.company.invoice.xlsx.RowError;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Performs all monetary arithmetic without floating-point arithmetic. */
public final class Calculator {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int MAX_EXCEL_SIGNIFICANT_DIGITS = 15;

    private Calculator() { }

    public static CalculationResult calculate(InvoiceConfig config, List<InvoiceItem> items, String sheetName) {
        List<InvoiceLine> lines = new ArrayList<>();
        List<RowError> errors = new ArrayList<>();
        BigDecimal beforeTotal = BigDecimal.ZERO.setScale(config.scale(), config.roundingMode());
        BigDecimal vatTotal = beforeTotal;
        BigDecimal afterTotal = beforeTotal;
        int lineNumber = 0;
        for (int index = 0; index < items.size(); index++) {
            InvoiceItem item = items.get(index);
            int rowNumber = index + 1;
            Optional<InvoiceLine> calculation = calculateSingleLine(config, item, sheetName, rowNumber, errors);
            if (calculation.isEmpty()) continue;
            InvoiceLine line = calculation.get();
            lineNumber++;
            lines.add(new InvoiceLine(lineNumber, line.itemName(), line.quantity(), line.unitPrice(), line.vatRate(), line.amountBeforeVat(), line.vatAmount(), line.amountAfterVat()));
            beforeTotal = beforeTotal.add(line.amountBeforeVat());
            vatTotal = vatTotal.add(line.vatAmount());
            afterTotal = afterTotal.add(line.amountAfterVat());
        }
        if (exceedsPrecision(beforeTotal) || exceedsPrecision(vatTotal) || exceedsPrecision(afterTotal)) {
            throw new com.company.invoice.xlsx.InvoiceException("invoice total exceeds Excel's 15 significant digit precision");
        }
        return new CalculationResult(lines, new InvoiceTotals(beforeTotal, vatTotal, afterTotal), errors);
    }

    public static Optional<InvoiceLine> calculateSingleLine(InvoiceConfig config, InvoiceItem item, String sheetName, int rowNumber, List<RowError> errors) {
        List<RowError> validationErrors = validateItem(item, sheetName, rowNumber);
        if (!validationErrors.isEmpty()) {
            errors.addAll(validationErrors);
            return Optional.empty();
        }
        BigDecimal before = item.quantity().multiply(item.unitPrice()).setScale(config.scale(), config.roundingMode());
        BigDecimal vat = before.multiply(item.vatRate()).divide(ONE_HUNDRED, config.scale(), config.roundingMode());
        BigDecimal after = before.add(vat);
        if (before.signum() < 0 || vat.signum() < 0 || after.signum() < 0) {
            errors.add(error(sheetName, rowNumber, null, "", ErrorCode.NEGATIVE_AMOUNT,
                    "calculated amount is negative"));
            return Optional.empty();
        }
        if (exceedsPrecision(before) || exceedsPrecision(vat) || exceedsPrecision(after)) {
            errors.add(error(sheetName, rowNumber, null, "", ErrorCode.PRECISION_EXCEEDED,
                    "calculated amount exceeds Excel's 15 significant digit precision"));
            return Optional.empty();
        }
        return Optional.of(new InvoiceLine(0, item.itemName(), item.quantity(), item.unitPrice(), item.vatRate(), before, vat, after));
    }

    private static List<RowError> validateItem(InvoiceItem item, String sheetName, int rowNumber) {
        List<RowError> errors = new ArrayList<>();
        if (item == null) {
            errors.add(error(sheetName, rowNumber, null, "", ErrorCode.MISSING_VALUE, "row is null"));
            return errors;
        }
        if (item.itemName() == null || item.itemName().trim().isEmpty()) {
            errors.add(error(sheetName, rowNumber, null, "", ErrorCode.MISSING_VALUE, "item_name is required"));
        } else if (item.itemName().trim().length() > 255) {
            errors.add(error(sheetName, rowNumber, null, item.itemName(), ErrorCode.TEXT_TOO_LONG, "item_name exceeds 255 characters"));
        }
        if (item.quantity() == null) errors.add(error(sheetName, rowNumber, null, "", ErrorCode.MISSING_VALUE, "quantity is required"));
        else if (item.quantity().signum() <= 0) errors.add(error(sheetName, rowNumber, null, item.quantity().toPlainString(), ErrorCode.OUT_OF_RANGE, "quantity must be greater than 0"));
        else if (item.quantity().scale() > 3) errors.add(error(sheetName, rowNumber, null, item.quantity().toPlainString(), ErrorCode.TOO_MANY_DECIMALS, "quantity has more than 3 decimal places"));
        if (item.unitPrice() == null) errors.add(error(sheetName, rowNumber, null, "", ErrorCode.MISSING_VALUE, "unit_price is required"));
        else if (item.unitPrice().signum() < 0) errors.add(error(sheetName, rowNumber, null, item.unitPrice().toPlainString(), ErrorCode.OUT_OF_RANGE, "unit_price must not be negative"));
        if (item.vatRate() == null) errors.add(error(sheetName, rowNumber, null, "", ErrorCode.MISSING_VALUE, "vat_rate is required"));
        else if (item.vatRate().signum() < 0 || item.vatRate().compareTo(ONE_HUNDRED) > 0) errors.add(error(sheetName, rowNumber, null, item.vatRate().toPlainString(), ErrorCode.OUT_OF_RANGE, "vat_rate must be between 0 and 100"));
        return errors;
    }

    private static boolean exceedsPrecision(BigDecimal value) {
        return value.precision() > MAX_EXCEL_SIGNIFICANT_DIGITS;
    }

    private static RowError error(String sheet, int row, String cell, String value, ErrorCode code, String message) {
        return new RowError(sheet, row, cell, "", value, code, message);
    }

    public record CalculationResult(List<InvoiceLine> lines, InvoiceTotals totals, List<RowError> errors) { }
}
