@file:Suppress("FunctionName")

package com.dbflow5.database.scope

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.CountResultFactory
import com.dbflow5.query.ExecutableQuery
import com.dbflow5.query.SelectResult
import com.dbflow5.query.cursor
import com.dbflow5.query.hasData

/**
 * Description:
 */
class DatabaseScopeImpl<DB : GeneratedDatabase>
internal constructor(
    override val db: DB
) :
    WritableDatabaseScope<DB> {

    override suspend fun <T : Any> ModelAdapter<T>.save(model: T): Result<T> {
        return save(model, db)
    }

    override suspend fun <T : Any> ModelAdapter<T>.saveAll(models: Collection<T>): Result<Collection<T>> {
        return saveAll(models, db)
    }

    override suspend fun <T : Any> ModelAdapter<T>.insert(model: T): Result<T> {
        return insert(model, db)
    }

    override suspend fun <T : Any> ModelAdapter<T>.insertAll(models: Collection<T>): Result<Collection<T>> {
        return insertAll(models, db)
    }

    override suspend fun <T : Any> ModelAdapter<T>.update(model: T): Result<T> {
        return update(model, db)
    }

    override suspend fun <T : Any> ModelAdapter<T>.updateAll(models: Collection<T>): Result<Collection<T>> {
        return updateAll(models, db)
    }

    override suspend fun <T : Any> ModelAdapter<T>.delete(model: T): Result<T> {
        return delete(model, db)
    }

    override suspend fun <T : Any> ModelAdapter<T>.deleteAll(models: Collection<T>): Result<Collection<T>> {
        return deleteAll(models, db)
    }

    override suspend fun <T : Any> ModelAdapter<T>.exists(model: T): Boolean {
        return exists(model, db)
    }

    override suspend fun <T : Any> ModelAdapter<T>.load(model: T): T? {
        return loadSingle(model, db)
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
        adapter: RetrievalAdapter<OtherTable>
    ): OtherTable? = execute(db).singleOrNull(adapter)

    override suspend fun <Table : Any, OtherTable : Any> ExecutableQuery<SelectResult<Table>>.single(
        adapter: RetrievalAdapter<OtherTable>
    ): OtherTable = execute(db).single(adapter)

    override suspend fun <Table : Any, OtherTable : Any> ExecutableQuery<SelectResult<Table>>.list(
        adapter: RetrievalAdapter<OtherTable>
    ): List<OtherTable> = execute(db).list(adapter)

    override suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.cursor(): FlowCursor =
        cursor(db)

    override suspend fun ExecutableQuery<CountResultFactory.Count>.hasData(): Boolean =
        hasData(db)
}

fun <DB : GeneratedDatabase> WritableDatabaseScope(
    db: DB,
):
    WritableDatabaseScope<DB> = DatabaseScopeImpl(db)

fun <DB : GeneratedDatabase> ReadableDatabaseScope(
    db: DB,
): ReadableDatabaseScope<DB> = DatabaseScopeImpl(db)
