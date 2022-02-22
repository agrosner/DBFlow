package com.dbflow5.database.scope

import com.dbflow5.adapter2.ModelAdapter
import com.dbflow5.adapter2.QueryRepresentable
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.CountResultFactory
import com.dbflow5.query.ExecutableQuery
import com.dbflow5.query.SelectResult

/**
 * Description:
 */
interface ReadableQueriableScope {

    suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.singleOrNull(): Table?
    suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.single(): Table
    suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.list(): List<Table>
    suspend fun <Table : Any, OtherTable : Any> ExecutableQuery<SelectResult<Table>>.singleOrNull(
        adapter: QueryRepresentable<OtherTable>,
    ): OtherTable?

    suspend fun <Table : Any, OtherTable : Any> ExecutableQuery<SelectResult<Table>>.single(
        adapter: QueryRepresentable<OtherTable>,
    ): OtherTable

    suspend fun <Table : Any, OtherTable : Any> ExecutableQuery<SelectResult<Table>>.list(
        adapter: QueryRepresentable<OtherTable>,
    ): List<OtherTable>

    suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.cursor(): FlowCursor

    suspend fun ExecutableQuery<CountResultFactory.Count>.hasData(): Boolean

    suspend fun <Result> ExecutableQuery<Result>.execute(): Result

    suspend fun <Table : Any> ModelAdapter<Table>.exists(model: Table): Boolean
}

interface WritableQueriableScope : ReadableQueriableScope