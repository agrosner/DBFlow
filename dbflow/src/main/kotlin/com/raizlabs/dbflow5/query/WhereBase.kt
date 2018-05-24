package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.sql.Query
import com.raizlabs.dbflow5.sql.QueryCloneable

/**
 * Description: The base for a [Where] statement.
 */
interface WhereBase<TModel> : Query, Actionable, QueryCloneable<WhereBase<TModel>> {

    /**
     * @return The table of this query.
     */
    val table: Class<TModel>

    /**
     * @return The base Query object.
     */
    val queryBuilderBase: Query

}
