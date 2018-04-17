package com.raizlabs.dbflow5.query

import kotlin.reflect.KClass
import com.raizlabs.dbflow5.sql.Query
import com.raizlabs.dbflow5.sql.QueryCloneable

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
