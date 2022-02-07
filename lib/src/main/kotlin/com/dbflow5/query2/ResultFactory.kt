package com.dbflow5.query2

import com.dbflow5.database.DatabaseWrapper

/**
 * Determines how results are created from a query.
 */
interface ResultFactory<Result> {

    fun DatabaseWrapper.createResult(query: String): Result
}

object UpdateDeleteResultFactory : ResultFactory<Long> {
    override fun DatabaseWrapper.createResult(query: String): Long {
        return compileStatement(query).use { it.executeUpdateDelete() }
    }
}