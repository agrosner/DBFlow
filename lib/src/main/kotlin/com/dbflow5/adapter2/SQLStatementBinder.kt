package com.dbflow5.adapter2

import com.dbflow5.database.DatabaseStatement

/**
 * Defines how a [TableSQL] binds to a [Table]
 */
fun interface SQLStatementBinder<Table : Any> {

    fun bind(databaseStatement: DatabaseStatement, model: Table)
}