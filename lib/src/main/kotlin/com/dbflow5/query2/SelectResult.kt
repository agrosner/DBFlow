package com.dbflow5.query2

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.SQLiteException

internal data class SelectResultFactory<Table : Any>(
    override val adapter: SQLObjectAdapter<Table>
) :
    ResultFactory<SelectResult<Table>>,
    HasAdapter<Table, SQLObjectAdapter<Table>> {
    override fun DatabaseWrapper.createResult(query: String): SelectResult<Table> {
        return SelectResultImpl(this, adapter, query)
    }
}

/**
 * Result wrapper from building a [Select] query. The result
 * has not executed yet. Calling any of the methods on this
 * interface executes the query. Prefer calling the extension methods
 * at the bottom on [ExecutableQuery] instead to skip this result type.
 */
interface SelectResult<Table : Any> {

    suspend fun singleOrNull(): Table?

    suspend fun single(): Table

    suspend fun list(): List<Table>

    suspend fun <OtherTable : Any> single(
        adapter: RetrievalAdapter<OtherTable>
    ): OtherTable

    suspend fun <OtherTable : Any> singleOrNull(
        adapter: RetrievalAdapter<OtherTable>
    ): OtherTable?

    suspend fun <OtherTable : Any> list(
        adapter: RetrievalAdapter<OtherTable>
    ): List<OtherTable>

}

internal data class SelectResultImpl<Table : Any>(
    private val db: DatabaseWrapper,
    override val adapter: SQLObjectAdapter<Table>,
    private val query: String,
) : SelectResult<Table>,
    HasAdapter<Table, SQLObjectAdapter<Table>> {

    override suspend fun singleOrNull(): Table? =
        adapter.loadSingle(db, query)

    override suspend fun single(): Table = adapter.loadSingle(
        db, query
    ) ?: throw SQLiteException("Expected model result not found for Query: $query")


    override suspend fun list(): List<Table> = adapter.loadList(
        db, query
    )

    override suspend fun <OtherTable : Any> single(adapter: RetrievalAdapter<OtherTable>): OtherTable =
        adapter.loadSingle(db, query)
            ?: throw SQLiteException("Expected model result not found for Query: $query")

    override suspend fun <OtherTable : Any> singleOrNull(adapter: RetrievalAdapter<OtherTable>): OtherTable? =
        adapter.loadSingle(db, query)

    override suspend fun <OtherTable : Any> list(adapter: RetrievalAdapter<OtherTable>): List<OtherTable> =
        adapter.loadList(db, query)
}

/**
 * Extension method enables skipping execute() and returns single result.
 */
suspend fun <Table : Any>
    ExecutableQuery<SelectResult<Table>>.single(
    db: DatabaseWrapper
) =
    execute(db).single()

/**
 * Extension method enables skipping execute() and returns single result or null (if not found).
 */
suspend fun <Table : Any>
    ExecutableQuery<SelectResult<Table>>.singleOrNull(
    db: DatabaseWrapper
) =
    execute(db).singleOrNull()

/**
 * Extension method enables skipping execute() and returns list.
 */
suspend fun <Table : Any>
    ExecutableQuery<SelectResult<Table>>.list(
    db: DatabaseWrapper
) =
    execute(db).list()

/**
 * Extension method enables skipping execute() and returns single result
 * based on the [adapter] passed in.
 */
suspend fun <Table : Any, OtherTable : Any>
    ExecutableQuery<SelectResult<Table>>.single(
    db: DatabaseWrapper,
    adapter: RetrievalAdapter<OtherTable>,
) =
    execute(db).single(adapter)

/**
 * Extension method enables skipping execute() and returns single result or null (if not found)
 * based on the [adapter] passed in.
 */
suspend fun <Table : Any, OtherTable : Any>
    ExecutableQuery<SelectResult<Table>>.singleOrNull(
    db: DatabaseWrapper,
    adapter: RetrievalAdapter<OtherTable>,
) =
    execute(db).singleOrNull(adapter)

/**
 * Extension method enables skipping execute() and returns list
 * based on the [adapter] passed in.
 */
suspend fun <Table : Any, OtherTable : Any>
    ExecutableQuery<SelectResult<Table>>.list(
    db: DatabaseWrapper,
    adapter: RetrievalAdapter<OtherTable>,
) =
    execute(db).list(adapter)


suspend fun ExecutableQuery<SelectResult<*>>.cursor(
    db: DatabaseWrapper
) = db.compileStatement(query, null)
