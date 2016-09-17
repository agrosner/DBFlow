package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;

/**
 * Description: The base for a {@link Where} statement.
 */
public interface WhereBase<TModel> extends Query {

    /**
     * @return The table of this query.
     */
    Class<TModel> getTable();

    /**
     * @return The base Query object.
     */
    Query getQueryBuilderBase();
}
