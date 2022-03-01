package com.dbflow5.adapter

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.sql.Query
import kotlin.jvm.JvmInline

/**
 * Description: Represents a [query] string that can get compiled. Used by generated
 * code.
 */
@InternalDBFlowApi
@JvmInline
value class CompilableQuery(
    override val query: String,
) : Query {
    fun create(databaseWrapper: DatabaseWrapper) =
        databaseWrapper.compileStatement(query)
}
