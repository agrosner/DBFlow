@file:Suppress("FunctionName")

package com.dbflow5.database.scope

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.FlowLog
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query.Queriable
import com.dbflow5.query2.CountResultFactory
import com.dbflow5.query2.ExecutableQuery
import com.dbflow5.query2.SelectResult
import com.dbflow5.query2.cursor
import com.dbflow5.query2.hasData

/**
 * Description:
 */
class DatabaseScopeImpl<DB : DBFlowDatabase>
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

    override suspend fun <T : Any> ModelQueriable<T>.queryList(): List<T> {
        FlowLog.log(FlowLog.Level.I, "Thread: ${Thread.currentThread().name}")
        return queryList(db)
    }

    override suspend fun <T : Any> ModelQueriable<T>.querySingle(): T? {
        return querySingle(db)
    }

    override suspend fun <T : Any> ModelQueriable<T>.requireSingle(): T {
        return requireSingle(db)
    }

    override suspend fun <T : Any, R : Any> ModelQueriable<T>.customList(
        retrievalAdapter: RetrievalAdapter<R>
    ): List<R> {
        return queryCustomList(retrievalAdapter, db)
    }

    override suspend fun <T : Any, R : Any> ModelQueriable<T>.customSingle(
        retrievalAdapter: RetrievalAdapter<R>
    ): R? {
        return queryCustomSingle(retrievalAdapter, db)
    }

    override suspend fun <T : Any, R : Any> ModelQueriable<T>.requireCustomSingle(
        retrievalAdapter: RetrievalAdapter<R>
    ): R {
        return requireCustomSingle(retrievalAdapter, db)
    }

    override suspend fun Queriable.cursor(): FlowCursor? {
        return cursor(db)
    }

    override suspend fun Queriable.longValue(): Long {
        return longValue(db)
    }

    override suspend fun Queriable.stringValue(): String? {
        return stringValue(db)
    }

    override suspend fun Queriable.hasData(): Boolean {
        return hasData(db)
    }

    override suspend fun Queriable.compileStatement(): DatabaseStatement {
        return compileStatement(db)
    }

    override suspend fun Queriable.executeUpdateDelete(): Long {
        return executeUpdateDelete(db)
    }

    override suspend fun Queriable.executeInsert(): Long {
        return executeInsert(db)
    }

    override suspend fun Queriable.execute() {
        return execute(db)
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
    ): OtherTable? = execute(db).single(adapter)

    override suspend fun <Table : Any, OtherTable : Any> ExecutableQuery<SelectResult<Table>>.list(
        adapter: RetrievalAdapter<OtherTable>
    ): List<OtherTable> = execute(db).list(adapter)

    override suspend fun <Table : Any> ExecutableQuery<SelectResult<Table>>.cursor(): FlowCursor =
        cursor(db)

    override suspend fun ExecutableQuery<CountResultFactory.Count>.hasData(): Boolean =
        hasData(db)
}

fun <DB : DBFlowDatabase> WritableDatabaseScope(
    db: DB,
):
    WritableDatabaseScope<DB> = DatabaseScopeImpl(db)

fun <DB : DBFlowDatabase> ReadableDatabaseScope(
    db: DB,
): ReadableDatabaseScope<DB> = DatabaseScopeImpl(db)
