package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.structure.BaseModel;

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

    BaseModel.Action getPrimaryAction();
}
