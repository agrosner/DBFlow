package com.dbflow5.database.scope

import com.dbflow5.query.ExecutableQuery

/**
 * Provides a set of allowed operations within the migration scope.
 */
interface MigrationScope {

    suspend fun ExecutableQuery<Unit>.execute()
}