package com.dbflow5.query

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.sql.Query
import com.dbflow5.sql.QueryCloneable

/**
 * Description: The base for a [Where] statement.
 */
interface WhereBase<TModel : Any> : Query, Actionable, QueryCloneable<WhereBase<TModel>> {

    /**
     * @return The adapter for this query.
     */
    val adapter: RetrievalAdapter<TModel>

    /**
     * @return The base Query object.
     */
    val queryBuilderBase: Query

}
