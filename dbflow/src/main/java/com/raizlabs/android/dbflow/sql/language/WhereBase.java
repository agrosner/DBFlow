package com.raizlabs.android.dbflow.sql.language;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;

/**
 * Description: The base for a {@link Where} statement.
 */
public interface WhereBase<TModel> extends Query, Actionable {

    /**
     * @return The table of this query.
     */
    @NonNull
    Class<TModel> getTable();

    /**
     * @return The base Query object.
     */
    @NonNull
    Query getQueryBuilderBase();

}
