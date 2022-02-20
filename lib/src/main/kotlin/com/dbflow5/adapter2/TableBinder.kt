package com.dbflow5.adapter2

import com.dbflow5.annotation.opts.InternalDBFlowApi

/**
 * Represents how to bind table operations. Used by generated code.
 */
@InternalDBFlowApi
data class TableBinder<Table : Any>(
    val insert: SQLStatementBinder<Table>,
    val update: SQLStatementBinder<Table>,
    val delete: SQLStatementBinder<Table>,
    val save: SQLStatementBinder<Table> = insert,
)