package com.dbflow5.query2

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.database.SQLiteException
import com.dbflow5.database.scope.ReadableQueriableScope

internal data class SelectResultFactory<Table : Any>(
    override val adapter: SQLObjectAdapter<Table>
) :
    ResultFactory<SelectResult<Table>>,
    HasAdapter<Table, SQLObjectAdapter<Table>> {
    override fun <DB : DBFlowDatabase> DB.createResult(query: String): SelectResult<Table> {
        return SelectResultImpl(this, adapter, query)
    }
}

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

internal data class SelectResultImpl<Table : Any, DB : DBFlowDatabase>(
    private val db: DB,
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