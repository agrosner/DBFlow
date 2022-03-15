package com.dbflow5.database.scope

import com.dbflow5.adapter.MigrationAdapter
import com.dbflow5.database.DatabaseConnection
import com.dbflow5.database.FlowCursor
import com.dbflow5.mpp.use
import com.dbflow5.query.ExecutableQuery
import com.dbflow5.query.SelectResult
import com.dbflow5.stripQuotes

/**
 * Thin wrapper over migrations that run.
 */
internal class MigrationScopeImpl(
    private val db: DatabaseConnection,
) : MigrationScope {

    override suspend fun <T> ExecutableQuery<T>.execute(): T {
        return execute(db)
    }

    override suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.cursor(): FlowCursor {
        return db.rawQuery(query)
    }

    override suspend fun MigrationAdapter.hasColumn(columnName: String): Boolean =
        db.rawQuery("PRAGMA table_info(${this.name})")
            .use { cursor ->
                cursor.any { it.getString(1) == columnName.stripQuotes() }
            }
}
