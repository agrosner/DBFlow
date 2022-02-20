package com.dbflow5.adapter2

import com.dbflow5.adapter.saveable.SaveOperationFailedException
import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.config.writableTransaction
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.scope.WritableDatabaseScope
import com.dbflow5.runtime.ModelNotification
import com.dbflow5.runtime.NotifyDistributor
import com.dbflow5.structure.ChangeAction

interface TableOps<Table> {

    suspend fun DatabaseWrapper.save(model: Table): Table

    suspend fun DatabaseWrapper.saveAll(models: Collection<Table>): Collection<Table>

    suspend fun DatabaseWrapper.update(model: Table): Table

    suspend fun DatabaseWrapper.updateAll(models: Collection<Table>): Collection<Table>

    suspend fun DatabaseWrapper.insert(model: Table): Table

    suspend fun DatabaseWrapper.insertAll(models: Collection<Table>): Collection<Table>

    suspend fun DatabaseWrapper.delete(model: Table): Table

    suspend fun DatabaseWrapper.deleteAll(models: Collection<Table>): Collection<Table>
}

/**
 * Runs operations and performs necessary notifications.
 */
@InternalDBFlowApi
data class TableOpsImpl<Table : Any>(
    private val tableSQL: TableSQL,
    private val tableBinder: TableBinder<Table>,
    private val notifyDistributor: NotifyDistributor,
    private val primaryModelClauseGetter: PrimaryModelClauseGetter<Table>,
    private val autoIncrementUpdater: AutoIncrementUpdater<Table>,
    /**
     * If true, we send model notifications.
     */
    private val notifyChanges: Boolean,
) : TableOps<Table> {

    private fun DatabaseWrapper.bind(
        model: Table,
        compilableQuery: CompilableQuery,
        binder: SQLStatementBinder<Table>
    ) = compilableQuery.create(this).apply { binder.bind(this, model) }

    private fun notifyModelChange(changeAction: ChangeAction) = { model: Table ->
        if (notifyChanges) {
            notifyDistributor.onChange(
                ModelNotification.ModelChange(
                    changedFields = primaryModelClauseGetter.get(model),
                    action = changeAction,
                    table = model::class,
                )
            )
        }
    }

    private fun WritableDatabaseScope<GeneratedDatabase>.runUpdateDeleteOperation(
        model: Table,
        query: CompilableQuery,
        binder: SQLStatementBinder<Table>,
        action: ChangeAction,
    ): Table = db.bind(model, query, binder).use { statement ->
        val rows = statement.executeUpdateDelete()
        if (rows != 0L) {
            model
        } else {
            throw SaveOperationFailedException(action.name)
        }
    }

    private fun WritableDatabaseScope<GeneratedDatabase>.runInsertOperation(
        model: Table,
        query: CompilableQuery,
        binder: SQLStatementBinder<Table>,
        action: ChangeAction,
    ): Table = db.bind(model, query, binder).use { statement ->
        val id = statement.executeInsert()
        if (id > INSERT_FAILED) {
            autoIncrementUpdater.run { model.update(id) }
        } else throw SaveOperationFailedException(action.name)
    }

    override suspend fun DatabaseWrapper.save(model: Table): Table =
        generatedDatabase.writableTransaction {
            runInsertOperation(model, tableSQL.save, tableBinder.save, ChangeAction.CHANGE)
        }.also(notifyModelChange(ChangeAction.CHANGE))

    override suspend fun DatabaseWrapper.saveAll(models: Collection<Table>): Collection<Table> =
        generatedDatabase.writableTransaction {
            models.map { model ->
                runInsertOperation(model, tableSQL.save, tableBinder.save, ChangeAction.CHANGE)
            }
        }.onEach(notifyModelChange(ChangeAction.CHANGE))

    override suspend fun DatabaseWrapper.update(model: Table): Table =
        generatedDatabase.writableTransaction {
            runUpdateDeleteOperation(
                model,
                tableSQL.update,
                tableBinder.update,
                ChangeAction.UPDATE
            )
        }.also(notifyModelChange(ChangeAction.UPDATE))

    override suspend fun DatabaseWrapper.updateAll(models: Collection<Table>): Collection<Table> =
        generatedDatabase.writableTransaction {
            models.map { model ->
                runUpdateDeleteOperation(
                    model,
                    tableSQL.update,
                    tableBinder.update,
                    ChangeAction.UPDATE
                )
            }
        }.onEach(notifyModelChange(ChangeAction.UPDATE))

    override suspend fun DatabaseWrapper.insert(model: Table): Table =
        generatedDatabase.writableTransaction {
            runInsertOperation(model, tableSQL.insert, tableBinder.insert, ChangeAction.INSERT)
        }.also(notifyModelChange(ChangeAction.INSERT))

    override suspend fun DatabaseWrapper.insertAll(models: Collection<Table>): Collection<Table> =
        generatedDatabase.writableTransaction {
            models.map { model ->
                runInsertOperation(model, tableSQL.insert, tableBinder.insert, ChangeAction.INSERT)
            }
        }.onEach(notifyModelChange(ChangeAction.INSERT))

    override suspend fun DatabaseWrapper.delete(model: Table): Table =
        generatedDatabase.writableTransaction {
            runUpdateDeleteOperation(
                model,
                tableSQL.delete,
                tableBinder.delete,
                ChangeAction.DELETE
            )
        }.also(notifyModelChange(ChangeAction.DELETE))

    override suspend fun DatabaseWrapper.deleteAll(models: Collection<Table>): Collection<Table> =
        generatedDatabase.writableTransaction {
            models.map { model ->
                runUpdateDeleteOperation(
                    model,
                    tableSQL.delete,
                    tableBinder.delete,
                    ChangeAction.DELETE
                )
            }
        }.onEach(notifyModelChange(ChangeAction.DELETE))

    private companion object {
        const val INSERT_FAILED = -1
    }
}