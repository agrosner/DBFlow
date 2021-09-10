package com.raizlabs.android.dbflow.sql.language;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    void appendConditionToQuery(@NonNull QueryBuilder queryBuilder);

    /**
     * The name of the column.
     *
     * @return The column name.
     */
    @NonNull
    String columnName();

    /**
     * The separator for this condition when paired with a {@link OperatorGroup}
     *
     * @return The separator, an AND, OR, or other kinds.
     */
    @Nullable
    String separator();

    /**
     * Sets the separator for this condition
     *
     * @param separator The string AND, OR, or something else.
     * @return This instance.
     */
    @NonNull
    SQLOperator separator(@NonNull String separator);

    /**
     * @return true if it has a separator, false if not.
     */
    boolean hasSeparator();

    /**
     * @return the operation that is used.
     */
    @NonNull
    String operation();

    /**
     * @return The raw value of the condition.
     */
    @Nullable
    Object value();

}
