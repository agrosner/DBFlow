package com.raizlabs.android.dbflow.sql.builder;

import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Basic interface for all of the Condition classes.
 */
public interface SQLCondition {

    /**
     * Appends itself to the {@link ConditionQueryBuilder}
     *
     * @param conditionQueryBuilder The builder to append to.
     * @param <ModelClass>          The class that implements {@link Model}
     */
    <ModelClass extends Model> void appendConditionToQuery(ConditionQueryBuilder<ModelClass> conditionQueryBuilder);

    /**
     * Appends to a {@link QueryBuilder} without converting any values.
     *
     * @param queryBuilder The builder to append to
     */
    void appendConditionToRawQuery(QueryBuilder queryBuilder);

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
     * @return The raw value of the condition.
     */
    Object value();
}
