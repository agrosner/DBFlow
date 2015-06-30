package com.raizlabs.android.dbflow.sql.validation;

/**
 * Description: This class's sole purpose is to validate column values and
 * react to outliers or invalid data via SQL injection.
 */
public abstract class ColumnValueValidator {

    /**
     * @return true if the specified value is a valid value, false if not.
     */
    public abstract boolean isValid(Object value);

}
