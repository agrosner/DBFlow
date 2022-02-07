package com.dbflow5.database.scope

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.Queriable
import com.dbflow5.query2.CountResultFactory
import com.dbflow5.query2.ExecutableQuery
import com.dbflow5.query2.SelectResult

/**
 * Description:
 */
interface ReadableQueriableScope {

    suspend fun Queriable.cursor(): FlowCursor?

    suspend fun Queriable.longValue(): Long

    suspend fun Queriable.stringValue(): String?

    suspend fun Queriable.hasData(): Boolean

    suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.singleOrNull(): Table?
    suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.single(): Table
    suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.list(): List<Table>
    suspend fun <Table : Any, OtherTable : Any> ExecutableQuery<SelectResult<Table>>.singleOrNull(
        adapter: RetrievalAdapter<OtherTable>,
    ): OtherTable?

    suspend fun <Table : Any, OtherTable : Any> ExecutableQuery<SelectResult<Table>>.single(
        adapter: RetrievalAdapter<OtherTable>,
    ): OtherTable?

    suspend fun <Table : Any, OtherTable : Any> ExecutableQuery<SelectResult<Table>>.list(
        adapter: RetrievalAdapter<OtherTable>,
    ): List<OtherTable>

    suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.cursor(): FlowCursor

    suspend fun ExecutableQuery<CountResultFactory.Count>.hasData(): Boolean
}

interface WritableQueriableScope : ReadableQueriableScope {

    suspend fun Queriable.compileStatement(): DatabaseStatement

    suspend fun Queriable.executeUpdateDelete(): Long

    suspend fun Queriable.executeInsert(): Long

    suspend fun Queriable.execute()

    suspend fun <Result> ExecutableQuery<Result>.execute(): Result

}