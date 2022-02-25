package com.dbflow5.database.scope

import com.dbflow5.adapter2.ModelAdapter
import com.dbflow5.adapter2.QueryRepresentable
import com.dbflow5.annotation.opts.DelicateDBFlowApi
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.readableTransaction
import com.dbflow5.database.FlowCursor
import com.dbflow5.database.SQLiteException
import com.dbflow5.query.CountResultFactory
import com.dbflow5.query.ExecutableQuery
import com.dbflow5.query.SelectResult
import com.dbflow5.query.selectCountOf

/**
 * A read-only scope to perform database operations. Use the created [DBFlowDatabase]
 * [readableTransaction] method to gain access to this scope.
 */
interface ReadableQueryScope {

    /**
     * Retrieves a single [Table] object or null (if does not exist)
     */
    suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.singleOrNull(): Table?

    /**
     * Retrieves a single [Table] object or throws a [SQLiteException] if not found.
     */
    suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.single(): Table

    /**
     * Expects to retrieve a list of [Table] based on the query. This does not throw if an empty
     * list is returned.
     */
    suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.list(): List<Table>

    /**
     * Using the query from another adapter type [Table], map to the [OtherTable] object adapter.
     * This is used for [Query] models. Same behavior as [singleOrNull]
     */
    suspend fun <Table : Any, OtherTable : Any> ExecutableQuery<SelectResult<Table>>.singleOrNull(
        adapter: QueryRepresentable<OtherTable>,
    ): OtherTable?

    /**
     * Using the query from another adapter type [Table], map to the [OtherTable] object adapter.
     * This is used for [Query] models. Same behavior as [single]
     */
    suspend fun <Table : Any, OtherTable : Any> ExecutableQuery<SelectResult<Table>>.single(
        adapter: QueryRepresentable<OtherTable>,
    ): OtherTable

    /**
     * Using the query from another adapter type [Table], map to the [OtherTable] object adapter
     * retrieving a list of [OtherTable].
     * This is used for [Query] models. Same behavior as [single]
     */
    suspend fun <Table : Any, OtherTable : Any> ExecutableQuery<SelectResult<Table>>.list(
        adapter: QueryRepresentable<OtherTable>,
    ): List<OtherTable>

    /**
     * Query the raw [FlowCursor] to manually handle the query. This should only be used in
     * exceptional situations and don't forget to close the cursor!
     */
    @DelicateDBFlowApi
    suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.cursor(): FlowCursor

    /**
     * Runs a count query to check if the query has data in the DB (more than 0 rows).
     * You must use a [selectCountOf] query or provide the [CountResultFactory]
     * as the select resultFactory type.
     */
    suspend fun ExecutableQuery<CountResultFactory.Count>.hasData(): Boolean

    /**
     * Runs a [selectCountOf] query using the primary keys of the provided [model].
     */
    suspend fun <Table : Any> ModelAdapter<Table>.exists(model: Table): Boolean
}

interface WritableQueryScope : ReadableQueryScope {
    /**
     * Runs the query, returning the raw result expected from the query.
     * In the situation of [SelectResult] / Select queries, avoid this method, and call the provided methods
     * such as [single], [list] instead. The [SelectResult] will not run the query when calling this
     * method.
     */
    suspend fun <Result> ExecutableQuery<Result>.execute(): Result
}