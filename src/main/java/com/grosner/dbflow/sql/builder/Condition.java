package com.grosner.dbflow.sql.builder;

/**
 * The class that contains a column name, operator, and value. The operator can be any Sqlite conditional
 * operator. The value is the {@link com.grosner.dbflow.structure.Model} value of the column and WILL be
 * converted into the database value when we run the query.
 */
public class Condition {

    /**
     * The operation such as "=", "<", and more
     */
    String mOperation;

    /**
     * The value of the column we care about
     */
    Object mValue;

    /**
     * The column name
     */
    String mColumn;

    public static Condition column(String columnName) {
        return new Condition(columnName);
    }

    /**
     * Creates a new instance
     *
     * @param columnName The name of the column in the DB
     */
    Condition(String columnName) {
        mColumn = columnName;
    }

    /**
     * Assigns the operation to "="
     *
     * @param value The value of the column in the DB in String value
     * @return
     */
    public Condition is(Object value) {
        mOperation = "=";
        return value(value);
    }

    /**
     * Assigns operation to ">"
     *
     * @param value The value of the column in the DB in String value
     * @return
     */
    public Condition greaterThan(Object value) {
        mOperation = ">";
        return value(value);
    }

    /**
     * Assigns operation to "<"
     *
     * @param value The value of the column in the DB in String value
     * @return
     */
    public Condition lessThan(Object value) {
        mOperation = "<";
        return value(value);
    }

    /**
     * Add a custom operation to this argument
     *
     * @param operation
     * @return
     */
    public Condition operation(String operation) {
        mOperation = operation;
        return this;
    }

    /**
     * The string value of the parameter
     *
     * @param value
     * @return
     */
    public Condition value(Object value) {
        mValue = value;
        return this;
    }

    /**
     * Returns the operation of it
     *
     * @return
     */
    public String operation() {
        return mOperation;
    }

    /**
     * Returns the value of the arg
     *
     * @return
     */
    public Object value() {
        return mValue;
    }

    /**
     * Returns the column name
     *
     * @return
     */
    public String columnName() {
        return mColumn;
    }
}
