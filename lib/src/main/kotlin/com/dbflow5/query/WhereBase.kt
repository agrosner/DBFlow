package com.dbflow5.query

import com.dbflow5.sql.Query
import com.dbflow5.sql.QueryCloneable
import kotlin.reflect.KClass

/**
 * Description: The base for a [Where] statement.
 */
interface WhereBase<TModel : Any> : Query, Actionable, QueryCloneable<WhereBase<TModel>> {

    /**
     * @return The table of this query.
     */
    val table: KClass<TModel>

    /**
     * @return The base Query object.
     */
    val queryBuilderBase: Query

}
