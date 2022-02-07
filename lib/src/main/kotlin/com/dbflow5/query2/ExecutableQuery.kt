package com.dbflow5.query2

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.database.scope.WritableDatabaseScope
import com.dbflow5.sql.Query

/**
 * Runs the query with specified result in a coroutine.
 */
interface ExecutableQuery<Result> : Query {

    suspend fun <DB : DBFlowDatabase> execute(
        db: WritableDatabaseScope<DB>
    ): Result
}
