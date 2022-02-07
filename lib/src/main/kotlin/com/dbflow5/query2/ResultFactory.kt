package com.dbflow5.query2

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.longForQuery
import com.dbflow5.stringForQuery

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

object InsertResultFactory : ResultFactory<Long> {
    override fun DatabaseWrapper.createResult(query: String): Long {
        return compileStatement(query).use { it.executeInsert() }
    }
}

object CountResultFactory : ResultFactory<CountResultFactory.Count> {
    @JvmInline
    value class Count(val value: Long)

    override fun DatabaseWrapper.createResult(query: String): Count =
        Count(longForQuery(this, query))
}

suspend fun ExecutableQuery<CountResultFactory.Count>.hasData(
    db: DatabaseWrapper
): Boolean = execute(db).value > 0

object StringResultFactory : ResultFactory<StringResultFactory.StringResult> {
    @JvmInline
    value class StringResult(val value: String?)

    override fun DatabaseWrapper.createResult(query: String): StringResult =
        StringResult(stringForQuery(this, query))
}
