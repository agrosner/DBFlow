package com.raizlabs.android.dbflow.sql.builder;

import com.raizlabs.android.dbflow.annotation.Collate;
import com.raizlabs.android.dbflow.sql.QueryBuilder;

/**
 * The class that contains a column name, operator, and value. The operator can be any Sqlite conditional
 * operator. The value is the {@link com.raizlabs.android.dbflow.structure.Model} value of the column and WILL be
 * converted into the database value when we run the query.
 */
public class Condition {

    public static class Between extends Condition {

        private Object mSecondValue;

        /**
         * Creates a new instance
         *
         * @param condition
         * @param value     The value of the first argument of the BETWEEN clause
         */
        private Between(Condition condition, Object value) {
            super(condition.columnName());
            this.mOperation = " BETWEEN ";
            this.mValue = value;
            this.mPostArgument = condition.postArgument();
        }

        public Between and(Object secondValue) {
            mSecondValue = secondValue;
            return this;
        }

        public Object secondValue() {
            return mSecondValue;
        }

        @Override
        public void appendConditionToQuery(ConditionQueryBuilder conditionQueryBuilder) {
            conditionQueryBuilder.append(columnName()).append(operation())
                    .append(conditionQueryBuilder.convertValueToString(value()))
                    .appendSpaceSeparated("AND")
                    .append(conditionQueryBuilder.convertValueToString(secondValue()))
                    .appendSpace().appendOptional(postArgument());
        }

        @Override
        public void appendConditionToRawQuery(QueryBuilder queryBuilder) {
            queryBuilder.append(columnName()).append(operation())
                    .append((value()))
                    .appendSpaceSeparated("AND")
                    .append(secondValue())
                    .appendSpace().appendOptional(postArgument());
        }
    }

    /**
     * The operation such as "=", "<", and more
     */
    protected String mOperation;

    /**
     * The value of the column we care about
     */
    protected Object mValue;

    /**
     * The column name
     */
    protected String mColumn;

    /**
     * A custom SQL statement after the value of the Condition
     */
    protected String mPostArgument;

    /**
     * An optional separator to use when chaining these together
     */
    protected String mSeparator;

    /**
     * Creates a new instance
     *
     * @param columnName The name of the column in the DB
     */
    private Condition(String columnName) {
        if(columnName == null) {
            throw new IllegalArgumentException("Column " + columnName + " cannot be null");
        }
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
     * Assigns the operation to "!="
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    public Condition isNot(Object value) {
        mOperation = "!=";
        return value(value);
    }

    /**
     * Uses the LIKE operation. Case insensitive comparisons.
     *
     * @param likeRegex Uses sqlite LIKE regex to match rows.
     *                  It must be a string to escape it properly.
     *                  There are two wildcards: % and _
     *                  % represents [0,many) numbers or characters.
     *                  The _ represents a single number or character.
     * @return This condition
     */
    public Condition like(String likeRegex) {
        mOperation = " LIKE ";
        return value(likeRegex);
    }

    /**
     * Uses the GLOB operation. Similar to LIKE except it uses case sensitive comparisons.
     *
     * @param globRegex Uses sqlite GLOB regex to match rows.
     *                  It must be a string to escape it properly.
     *                  There are two wildcards: * and ?
     *                  * represents [0,many) numbers or characters.
     *                  The ? represents a single number or character
     * @return This condition
     */
    public Condition glob(String globRegex) {
        mOperation = " GLOB ";
        return value(globRegex);
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
     *
     * @param collation The SQLite collate function
     * @return
     */
    public Condition collate(String collation) {
        mPostArgument = "COLLATE " + collation;
        return this;
    }

    /**
     * Adds a COLLATE to the end of this condition using the {@link com.raizlabs.android.dbflow.annotation.Collate} enum.
     *
     * @param collation The SQLite collate function
     * @return
     */
    public Condition collate(Collate collation) {
        if (collation.equals(Collate.NONE)) {
            mPostArgument = null;
        } else {
            collate(collation.name());
        }

        return this;
    }

    /**
     * Appends an optional SQL string to the end of this condition
     *
     * @param postfix
     * @return
     */
    public Condition postfix(String postfix) {
        mPostArgument = postfix;
        return this;
    }

    /**
     * Appends IS NULL to the end of this condition
     *
     * @return
     */
    public Condition isNull() {
        mOperation = " IS ";
        mValue = "NULL";
        return this;
    }

    /**
     * Appends IS NOT NULL to the end of this condition
     *
     * @return
     */
    public Condition isNotNull() {
        mOperation = " IS NOT ";
        mValue = "NULL";
        return this;
    }

    /**
     * Optional separator when chaining this Condition within a {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
     *
     * @param separator The separator to use
     * @return This instance
     */
    public Condition separator(String separator) {
        mSeparator = separator;
        return this;
    }

    /**
     * Turns this condition into a SQL BETWEEN operation
     *
     * @param value The value of the first argument of the BETWEEN clause
     * @return Between operator
     */
    public Between between(Object value) {
        return new Between(this, value);
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

    public String separator() {
        return mSeparator;
    }

    /**
     * @return true if has a separator defined for this condition.
     */
    public boolean hasSeparator() {
        return mSeparator != null && (mSeparator.length()>0);
    }

    /**
     * Appends the condition to the {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
     *
     * @param conditionQueryBuilder
     */
    public void appendConditionToQuery(ConditionQueryBuilder conditionQueryBuilder) {
        conditionQueryBuilder.append(columnName()).append(operation())
                .append(conditionQueryBuilder.convertValueToString(value()));
        if (postArgument() != null) {
            conditionQueryBuilder.appendSpace().append(postArgument());
        }
    }

    /**
     * Appends the condition to the {@link com.raizlabs.android.dbflow.sql.QueryBuilder} without converting values.
     *
     * @param queryBuilder
     */
    public void appendConditionToRawQuery(QueryBuilder queryBuilder) {
        queryBuilder.append(columnName()).append(operation())
                .append(value());
        if (postArgument() != null) {
            queryBuilder.appendSpace().append(postArgument());
        }
    }
}
