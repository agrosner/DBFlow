package com.dbflow5.adapter

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.database.DatabaseWrapper

/**
 * Description: Represents a [query] string that can get compiled. Used by generated
 * code.
 */
@InternalDBFlowApi
@JvmInline
value class CompilableQuery(
    val query: String,
) {
    fun create(databaseWrapper: DatabaseWrapper) =
        databaseWrapper.compileStatement(query)
}
