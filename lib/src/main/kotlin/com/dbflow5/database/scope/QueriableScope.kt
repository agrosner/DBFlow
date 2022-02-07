package com.dbflow5.database.scope

import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.Queriable
import com.dbflow5.query2.ExecutableQuery

/**
 * Description:
 */
interface ReadableQueriableScope {

    suspend fun Queriable.cursor(): FlowCursor?

    suspend fun Queriable.longValue(): Long

    suspend fun Queriable.stringValue(): String?

    suspend fun Queriable.hasData(): Boolean
}

interface WritableQueriableScope : ReadableQueriableScope {

    suspend fun Queriable.compileStatement(): DatabaseStatement

    suspend fun Queriable.executeUpdateDelete(): Long

    suspend fun Queriable.executeInsert(): Long

    suspend fun Queriable.execute()

    suspend fun <Result> ExecutableQuery<Result>.execute(): Result
}