package com.raizlabs.android.dbflow.query

import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.database.DatabaseWrapper

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
