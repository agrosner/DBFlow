package com.dbflow5.database.scope

import com.dbflow5.adapter.MigrationAdapter
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.ExecutableQuery
import com.dbflow5.query.SelectResult

/**
 * Provides a set of allowed operations within the migration scope.
 */
interface MigrationScope {

    suspend fun <T> ExecutableQuery<T>.execute(): T

    suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.cursor(): FlowCursor

    suspend infix fun MigrationAdapter.hasColumn(columnName: String): Boolean

}
