package com.dbflow5.query

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.adapter.QueryOps
import com.dbflow5.config.readableTransaction
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.SQLiteException
import com.dbflow5.sql.Query

internal data class SelectResultFactory<Table : Any>(
    override val adapter: DBRepresentable<Table>
) :
    ResultFactory<SelectResult<Table>>,
    HasAdapter<Table, DBRepresentable<Table>> {
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
        adapter: QueryOps<OtherTable>
    ): OtherTable

    suspend fun <OtherTable : Any> singleOrNull(
        adapter: QueryOps<OtherTable>
    ): OtherTable?

    suspend fun <OtherTable : Any> list(
        adapter: QueryOps<OtherTable>
    ): List<OtherTable>

}

internal data class SelectResultImpl<Table : Any>(
    private val db: DatabaseWrapper,
    override val adapter: DBRepresentable<Table>,
    override val query: String,
) : SelectResult<Table>,
    HasAdapter<Table, DBRepresentable<Table>>, Query {

    override suspend fun singleOrNull(): Table? =
        adapter.run { db.single(this@SelectResultImpl) }

    override suspend fun single(): Table = adapter.run {
        db.single(this@SelectResultImpl)
    } ?: throw SQLiteException("Expected model result not found for Query: $query")


    override suspend fun list(): List<Table> =
        adapter.run { db.list(this@SelectResultImpl) }

    override suspend fun <OtherTable : Any> single(adapter: QueryOps<OtherTable>): OtherTable =
        adapter.run { db.single(this@SelectResultImpl) }
            ?: throw SQLiteException("Expected model result not found for Query: $query")

    override suspend fun <OtherTable : Any> singleOrNull(adapter: QueryOps<OtherTable>): OtherTable? =
        adapter.run { db.single(this@SelectResultImpl) }

    override suspend fun <OtherTable : Any> list(adapter: QueryOps<OtherTable>): List<OtherTable> =
        adapter.run { db.list(this@SelectResultImpl) }
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
    adapter: QueryOps<OtherTable>,
) =
    execute(db).single(adapter)

/**
 * Extension method enables skipping execute() and returns single result or null (if not found)
 * based on the [adapter] passed in.
 */
suspend fun <Table : Any, OtherTable : Any>
    ExecutableQuery<SelectResult<Table>>.singleOrNull(
    db: DatabaseWrapper,
    adapter: QueryOps<OtherTable>,
) =
    execute(db).singleOrNull(adapter)

/**
 * Extension method enables skipping execute() and returns list
 * based on the [adapter] passed in.
 */
suspend fun <Table : Any, OtherTable : Any>
    ExecutableQuery<SelectResult<Table>>.list(
    db: DatabaseWrapper,
    adapter: QueryOps<OtherTable>,
) =
    execute(db).list(adapter)


suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.cursor(
    db: DatabaseWrapper
) = db.generatedDatabase.readableTransaction {
    db.rawQuery(query, null)
}
