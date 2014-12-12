package com.raizlabs.android.dbflow.sql.builder;

/**
 * The class that contains a column name, operator, and value. The operator can be any Sqlite conditional
 * operator. The value is the {@link com.raizlabs.android.dbflow.structure.Model} value of the column and WILL be
 * converted into the database value when we run the query.
 */
public class Condition {

    /**
     * The operation such as "=", "<", and more
     */
    private String mOperation;

    /**
     * The value of the column we care about
     */
    private Object mValue;

    /**
     * The column name
     */
    private String mColumn;

    /**
     * A custom SQL statement after the value of the Condition
     */
    private String mPostArgument;

    /**
     * Creates a new instance
     *
     * @param columnName The name of the column in the DB
     */
    private Condition(String columnName) {
        mColumn = columnName;
    }

    public static Condition column(String columnName) {
        return new Condition(columnName);
    }

    /**
     * Assigns the operation to "="
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    public Condition is(Object value) {
        mOperation = "=";
        return value(value);
    }

    /**
     * The value of the parameter
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    public Condition value(Object value) {
        mValue = value;
        return this;
    }

    /**
     * Assigns operation to ">"
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    public Condition greaterThan(Object value) {
        mOperation = ">";
        return value(value);
    }

    /**
     * Assigns operation to "<"
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    public Condition lessThan(Object value) {
        mOperation = "<";
        return value(value);
    }

    /**
     * Add a custom operation to this argument
     *
     * @param operation The SQLite operator
     * @return This condition
     */
    public Condition operation(String operation) {
        mOperation = operation;
        return this;
    }

    /**
     * Adds a COLLATE to the end of this condition
     * @param collation
     * @return
     */
    public Condition collate(String collation) {
        mOperation = "COLLATE " + collation;
        return this;
    }

    /**
     * Appends an optional SQL string to the end of this condition
     * @param postfix
     * @return
     */
    public Condition postfix(String postfix) {
        mPostArgument = postfix;
        return this;
    }

    /**
     * @return the operator such as "<", "<", or "="
     */
    public String operation() {
        return mOperation;
    }

    /**
     * @return the value of the argument
     */
    public Object value() {
        return mValue;
    }

    /**
     * @return the column name
     */
    public String columnName() {
        return mColumn;
    }

    /**
     * @return An optional post argument for this condition
     */
    public String postArgument() {
        return mPostArgument;
    }
}
