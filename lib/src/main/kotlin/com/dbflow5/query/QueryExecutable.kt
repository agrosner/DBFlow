package com.dbflow5.query

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.sql.Query

/**
 * Description: Marks a query as executable
 * by database.
 */
interface QueryExecutable<R> : Query {

    suspend fun execute(databaseWrapper: DatabaseWrapper): R
}

interface QueryExecutableIgnoreResult : QueryExecutable<Unit> {
    override suspend fun execute(databaseWrapper: DatabaseWrapper) =
        databaseWrapper.execSQL(this.query)
}
