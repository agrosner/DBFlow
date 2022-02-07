package com.dbflow5.query2

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.sql.Query

/**
 * Runs the query with specified result in a coroutine.
 */
interface ExecutableQuery<Result> : Query {

    suspend fun execute(
        db: DatabaseWrapper,
    ): Result
}
