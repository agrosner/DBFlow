package com.dbflow5.adapter2

import com.dbflow5.annotation.Table
import com.dbflow5.annotation.opts.InternalDBFlowApi

/**
 * Represents SQL operations for a [Table] used to perform standard table operations.
 */
@InternalDBFlowApi
data class TableSQL(
    val insert: CompilableQuery,
    val update: CompilableQuery,
    val delete: CompilableQuery,
    val save: CompilableQuery,
)

