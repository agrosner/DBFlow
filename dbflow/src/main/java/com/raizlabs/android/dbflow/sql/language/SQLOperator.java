package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.QueryBuilder;

/**
 * Description: Basic interface for all of the Operator classes.
 */
public interface SQLOperator {

    /**
     * Appends itself to the {@link QueryBuilder}
     *
     * @param queryBuilder The builder to append to.
     */
    void appendConditionToQuery(QueryBuilder queryBuilder);

    /**
     * The name of the column.
     *
     * @return The column name.
     */
    String columnName();

    /**
     * The separator for this condition when paired with a {@link OperatorGroup}
     *
     * @return The separator, an AND, OR, or other kinds.
     */
    String separator();

    /**
     * Sets the separator for this condition
     *
     * @param separator The string AND, OR, or something else.
     * @return This instance.
     */
    SQLOperator separator(String separator);

    /**
     * @return true if it has a separator, false if not.
     */
    boolean hasSeparator();

    /**
     * @return the operation that is used.
     */
    String operation();

    /**
     * @return The raw value of the condition.
     */
    Object value();

}
