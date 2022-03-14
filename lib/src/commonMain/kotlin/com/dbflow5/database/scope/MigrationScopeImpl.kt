package com.dbflow5.database.scope

import com.dbflow5.database.DatabaseConnection
import com.dbflow5.query.ExecutableQuery

/**
 * Thin wrapper over migrations that run.
 */
internal class MigrationScopeImpl(
    private val db: DatabaseConnection,
) : MigrationScope {
    override suspend fun ExecutableQuery<Unit>.execute() {
        return execute(db)
    }
}
