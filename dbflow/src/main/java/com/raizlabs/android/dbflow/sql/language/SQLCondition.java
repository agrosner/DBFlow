package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Basic interface for all of the Condition classes.
 */
public interface SQLCondition {

    /**
     * Appends itself to the {@link QueryBuilder}
     *
     * @param <ModelClass> The class that implements {@link Model}
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
     * The separator for this condition when paired with a {@link ConditionQueryBuilder}
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
    SQLCondition separator(String separator);

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
