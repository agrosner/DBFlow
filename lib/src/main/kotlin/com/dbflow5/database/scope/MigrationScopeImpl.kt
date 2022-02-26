package com.dbflow5.database.scope

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.ExecutableQuery

/**
 * Thin wrapper over migrations that run.
 */
internal class MigrationScopeImpl(
    private val db: DatabaseWrapper,
) : MigrationScope {
    override suspend fun ExecutableQuery<Unit>.execute() {
        return execute(db)
    }
}
