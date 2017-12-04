package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.sql.Query

/**
 * Description: The base for a [Where] statement.
 */
interface WhereBase<TModel> : Query, Actionable {

    /**
     * @return The table of this query.
     */
    val table: Class<TModel>

    /**
     * @return The base Query object.
     */
    val queryBuilderBase: Query

    val databaseWrapper: DatabaseWrapper

}
