package com.dbflow5.query2

import com.dbflow5.config.DBFlowDatabase

/**
 * Determines how results are created from a query.
 */
interface ResultFactory<Result> {

    fun <DB : DBFlowDatabase> DB.createResult(query: String): Result
}

object UpdateDeleteResultFactory : ResultFactory<Long> {
    override fun <DB : DBFlowDatabase> DB.createResult(query: String): Long {
        return compileStatement(query).use { it.executeUpdateDelete() }
    }
}