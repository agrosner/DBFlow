@file:Suppress("FunctionName")

package com.dbflow5.database.scope

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.QueryRepresentable
import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.CountResultFactory
import com.dbflow5.query.ExecutableQuery
import com.dbflow5.query.SelectResult
import com.dbflow5.query.cursor
import com.dbflow5.query.hasData

/**
 * Delegates operations to the calling target in each method. This can be thought of
 * as a lightweight abstraction wrapper that makes calling operations much simpler.
 */
internal class DatabaseScopeImpl<DB : GeneratedDatabase>(
    override val db: DB
) :
    WritableDatabaseScope<DB> {

    override suspend fun <T : Any> ModelAdapter<T>.save(model: T): T {
        return db.save(model)
    }

    override suspend fun <T : Any> ModelAdapter<T>.saveAll(models: Collection<T>): Collection<T> {
        return db.saveAll(models)
    }

    override suspend fun <T : Any> ModelAdapter<T>.insert(model: T): T {
        return db.insert(model)
    }

    override suspend fun <T : Any> ModelAdapter<T>.insertAll(models: Collection<T>): Collection<T> {
        return db.insertAll(models)
    }

    override suspend fun <T : Any> ModelAdapter<T>.update(model: T): T {
        return db.update(model)
    }

    override suspend fun <T : Any> ModelAdapter<T>.updateAll(models: Collection<T>): Collection<T> {
        return db.updateAll(models)
    }

    override suspend fun <T : Any> ModelAdapter<T>.delete(model: T): T {
        return db.delete(model)
    }

    override suspend fun <T : Any> ModelAdapter<T>.deleteAll(models: Collection<T>): Collection<T> {
        return db.deleteAll(models)
    }

    override suspend fun <T : Any> ModelAdapter<T>.exists(model: T): Boolean {
        return db.exists(model)
    }

    override suspend fun <Result> ExecutableQuery<Result>.execute(): Result {
        return execute(db)
    }

    override suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.singleOrNull(): Table? =
        execute(db).singleOrNull()

    override suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.single(): Table =
        execute(db).single()

    override suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.list(): List<Table> =
        execute(db).list()

    override suspend fun <Table : Any, OtherTable : Any> ExecutableQuery<SelectResult<Table>>.singleOrNull(
        adapter: QueryRepresentable<OtherTable>
    ): OtherTable? = execute(db).singleOrNull(adapter)

    override suspend fun <Table : Any, OtherTable : Any> ExecutableQuery<SelectResult<Table>>.single(
        adapter: QueryRepresentable<OtherTable>
    ): OtherTable = execute(db).single(adapter)

    override suspend fun <Table : Any, OtherTable : Any> ExecutableQuery<SelectResult<Table>>.list(
        adapter: QueryRepresentable<OtherTable>
    ): List<OtherTable> = execute(db).list(adapter)

    override suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.cursor(): FlowCursor =
        cursor(db)

    override suspend fun ExecutableQuery<CountResultFactory.Count>.hasData(): Boolean =
        hasData(db)
}

/**
 * Creates a new [WritableDatabaseScope] which allows writes as well as reads on the DB.
 */
fun <DB : GeneratedDatabase> WritableDatabaseScope(
    db: DB,
):
    WritableDatabaseScope<DB> = DatabaseScopeImpl(db)

/**
 * Creates a new [ReadableDatabaseScope] which allows for
 */
fun <DB : GeneratedDatabase> ReadableDatabaseScope(
    db: DB,
): ReadableDatabaseScope<DB> = DatabaseScopeImpl(db)
