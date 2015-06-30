package com.raizlabs.android.dbflow.sql.validation;

/**
 * Description: Thrown when an invalid column value is passed in a {@link ColumnValueValidator}.
 */
public class InvalidColumnValueException extends RuntimeException {

    public InvalidColumnValueException(String detailMessage) {
        super(detailMessage);
    }
}
