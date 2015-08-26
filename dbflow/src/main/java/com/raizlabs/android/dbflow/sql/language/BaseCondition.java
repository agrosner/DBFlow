package com.raizlabs.android.dbflow.sql.language;

import android.database.DatabaseUtils;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.converter.TypeConverter;

/**
 * Description: Base class for all kinds of {@link SQLCondition}
 */
abstract class BaseCondition implements SQLCondition {

    /**
     * Converts the given value for the column if it has a type converter. Then it turns that result into a string.
     *
     * @param value The value of the column in Model format.
     * @return Returns the result of converting and type converting the specified value.
     */
    @SuppressWarnings("unchecked")
    public static String convertValueToString(Object value) {
        String stringVal;
        if (value != null) {
            TypeConverter typeConverter = FlowManager.getTypeConverterForClass(value.getClass());
            if (typeConverter != null) {
                value = typeConverter.getDBValue(value);
            }
        }

        if (value instanceof Number) {
            stringVal = String.valueOf(value);
        } else {
            if (value instanceof Where) {
                stringVal = String.format("(%1s)", ((Where) value).getQuery().trim());
            } else if (value instanceof NameAlias) {
                stringVal = ((NameAlias) value).getQuery();
            } else {
                stringVal = String.valueOf(value);
                if (!stringVal.equals(Condition.Operation.EMPTY_PARAM)) {
                    stringVal = DatabaseUtils.sqlEscapeString(stringVal);
                }
            }
        }

        return stringVal;
    }

    /**
     * The operation such as "=", "&lt;", and more
     */
    protected String operation = "";

    /**
     * The value of the column we care about
     */
    protected Object value;

    /**
     * The column name
     */
    protected NameAlias nameAlias;

    /**
     * A custom SQL statement after the value of the Condition
     */
    protected String postArg;

    /**
     * An optional separator to use when chaining these together
     */
    protected String separator;

    /**
     * If it is a raw condition, we will not attempt to escape or convert the values.
     */
    protected boolean isRaw;

    /**
     * If true, the value is set and we should append it. (to prevent false positive nulls)
     */
    protected boolean isValueSet;

    BaseCondition(NameAlias nameAlias) {
        this.nameAlias = nameAlias;
    }

    /**
     * @return the value of the argument
     */
    @Override
    public Object value() {
        return value;
    }

    /**
     * @return the column name
     */
    @Override
    public String columnName() {
        return nameAlias.getQuery();
    }

    @Override
    public SQLCondition separator(String separator) {
        this.separator = separator;
        return this;
    }

    @Override
    public String separator() {
        return separator;
    }

    /**
     * @return true if has a separator defined for this condition.
     */
    @Override
    public boolean hasSeparator() {
        return separator != null && (separator.length() > 0);
    }

    /**
     * @return the operator such as "&lt;", "&gt;", or "="
     */
    public String operation() {
        return operation;
    }

    /**
     * @return An optional post argument for this condition
     */
    public String postArgument() {
        return postArg;
    }

    /**
     * @return internal alias used for subclasses.
     */
    NameAlias columnAlias() {
        return nameAlias;
    }

    public void setIsRaw(boolean isRaw) {
        this.isRaw = isRaw;
    }
}
