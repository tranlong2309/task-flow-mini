package com.company.invoice.xlsx.internal;

import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.DataFormatter;

/** Keeps numeric SAX values in plain decimal form while retaining percentage metadata. */
public final class NumericPreservingDataFormatter extends DataFormatter {
    private static final String PERCENT_MARKER = "__PERCENT__";

    @Override
    public String formatRawCellContents(double value, int formatIndex, String formatString) {
        BigDecimal decimalValue = BigDecimal.valueOf(value);
        if (formatString != null && formatString.indexOf('%') >= 0) {
            return PERCENT_MARKER + decimalValue.multiply(BigDecimal.valueOf(100)).toPlainString();
        }
        return decimalValue.toPlainString();
    }

    public static boolean isPercent(String value) {
        return value != null && value.startsWith(PERCENT_MARKER);
    }

    public static String removePercentMarker(String value) {
        return value.substring(PERCENT_MARKER.length());
    }
}
