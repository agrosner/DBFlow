package com.raizlabs.android.dbflow.sql.language;

/**
 * Description: Base class for all kinds of {@link SQLCondition}
 */
abstract class BaseCondition implements SQLCondition {

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
    protected boolean isRaw = false;

    /**
     * If true, the value is set and we should append it. (to prevent false positive nulls)
     */
    protected boolean isValueSet = false;

    BaseCondition(NameAlias nameAlias) {
        if (nameAlias == null) {
            throw new IllegalArgumentException("Column cannot be null");
        }
        this.nameAlias = nameAlias;
    }

    /**
     * Internal constructor for combined conditions.
     */
    BaseCondition() {

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

}
