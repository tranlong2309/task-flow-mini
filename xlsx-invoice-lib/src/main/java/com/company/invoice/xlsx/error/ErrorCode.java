package com.company.invoice.xlsx.error;

/** Stable codes for row-level validation problems. */
public enum ErrorCode {
    /** A required cell has no value. */
    MISSING_VALUE,
    /** A numeric value cannot be parsed. */
    NOT_A_NUMBER,
    /** A numeric value uses an unsupported textual format. */
    INVALID_FORMAT,
    /** A value is outside its allowed range. */
    OUT_OF_RANGE,
    /** Quantity has more than three decimal places. */
    TOO_MANY_DECIMALS,
    /** Item text exceeds the maximum length. */
    TEXT_TOO_LONG,
    /** A required cell has an unsupported Excel cell type. */
    UNSUPPORTED_CELL_TYPE,
    /** A formula cell has no cached result. */
    FORMULA_NOT_CACHED,
    /** A calculated amount is negative. */
    NEGATIVE_AMOUNT,
    /** A value exceeds Excel's safe numeric precision. */
    PRECISION_EXCEEDED
}
