package com.dbflow5.query

import com.dbflow5.database.DatabaseConnection
import com.dbflow5.sql.Query

/**
 * Runs the query with specified result in a coroutine.
 */
interface ExecutableQuery<Result> : Query {

    suspend fun execute(
        db: DatabaseConnection,
    ): Result
}
