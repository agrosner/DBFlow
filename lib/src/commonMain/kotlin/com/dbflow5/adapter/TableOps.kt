package com.dbflow5.adapter

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.config.DatabaseObjectLookup
import com.dbflow5.database.GeneratedDatabase
import com.dbflow5.database.writableTransaction
import com.dbflow5.database.DatabaseConnection
import com.dbflow5.mpp.use
import com.dbflow5.observing.notifications.ModelNotification
import com.dbflow5.structure.ChangeAction
import kotlin.reflect.KClass

interface TableOps<Table : Any> : QueryOps<Table> {

    suspend fun DatabaseConnection.save(model: Table): Table

    suspend fun DatabaseConnection.saveAll(models: Collection<Table>): Collection<Table>

    suspend fun DatabaseConnection.update(model: Table): Table

    suspend fun DatabaseConnection.updateAll(models: Collection<Table>): Collection<Table>

    suspend fun DatabaseConnection.insert(model: Table): Table

    suspend fun DatabaseConnection.insertAll(models: Collection<Table>): Collection<Table>

    suspend fun DatabaseConnection.delete(model: Table): Table

    suspend fun DatabaseConnection.deleteAll(models: Collection<Table>): Collection<Table>
}

/**
 * Runs operations and performs necessary notifications.
 */
@InternalDBFlowApi
data class TableOpsImpl<Table : Any>(
    private val table: KClass<Table>,
    private val queryOps: QueryOps<Table>,
    private val tableSQL: TableSQL,
    private val tableBinder: TableBinder<Table>,
    private val primaryModelClauseGetter: PrimaryModelClauseGetter<Table>,
    private val autoIncrementUpdater: AutoIncrementUpdater<Table>,
    /**
     * If true, we send model notifications.
     */
    private val notifyChanges: Boolean,
) : TableOps<Table>, QueryOps<Table> by queryOps {

    private val adapter by lazy { DatabaseObjectLookup.getDBRepresentable(table) }

    private fun DatabaseConnection.bind(
        model: Table,
        compilableQuery: CompilableQuery,
        binder: SQLStatementBinder<Table>
    ) = compilableQuery.create(this).apply { binder.bind(this, model) }

    private inline fun DatabaseConnection.notifyModelChange(changeAction: ChangeAction) =
        { model: Table ->
            if (notifyChanges) {
                generatedDatabase.modelNotifier.enqueueChange(
                    ModelNotification.ModelChange(
                        changedFields = primaryModelClauseGetter.get(model),
                        action = changeAction,
                        adapter = adapter,
                    )
                )
            }
        }

    private fun GeneratedDatabase.runUpdateDeleteOperation(
        model: Table,
        query: CompilableQuery,
        binder: SQLStatementBinder<Table>,
        action: ChangeAction,
    ): Table = bind(model, query, binder).use { statement ->
        val rows = statement.executeUpdateDelete()
        if (rows != 0L) {
            model
        } else {
            throw SaveOperationFailedException(action.name)
        }
    }

    private fun GeneratedDatabase.runInsertOperation(
        model: Table,
        query: CompilableQuery,
        binder: SQLStatementBinder<Table>,
        action: ChangeAction,
    ): Table = bind(model, query, binder).use { statement ->
        val id = statement.executeInsert()
        if (id > INSERT_FAILED) {
            autoIncrementUpdater.run { model.update(id) }
        } else throw SaveOperationFailedException(action.name)
    }

    override suspend fun DatabaseConnection.save(model: Table): Table =
        generatedDatabase.writableTransaction {
            runInsertOperation(model, tableSQL.save, tableBinder.save, ChangeAction.CHANGE)
        }.also(notifyModelChange(ChangeAction.CHANGE))

    override suspend fun DatabaseConnection.saveAll(models: Collection<Table>): Collection<Table> =
        generatedDatabase.writableTransaction {
            models.map { model ->
                runInsertOperation(model, tableSQL.save, tableBinder.save, ChangeAction.CHANGE)
            }
        }.onEach(notifyModelChange(ChangeAction.CHANGE))

    override suspend fun DatabaseConnection.update(model: Table): Table =
        generatedDatabase.writableTransaction {
            runUpdateDeleteOperation(
                model,
                tableSQL.update,
                tableBinder.update,
                ChangeAction.UPDATE
            )
        }.also(notifyModelChange(ChangeAction.UPDATE))

    override suspend fun DatabaseConnection.updateAll(models: Collection<Table>): Collection<Table> =
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

    override suspend fun DatabaseConnection.insert(model: Table): Table =
        generatedDatabase.writableTransaction {
            runInsertOperation(model, tableSQL.insert, tableBinder.insert, ChangeAction.INSERT)
        }.also(notifyModelChange(ChangeAction.INSERT))

    override suspend fun DatabaseConnection.insertAll(models: Collection<Table>): Collection<Table> =
        generatedDatabase.writableTransaction {
            models.map { model ->
                runInsertOperation(model, tableSQL.insert, tableBinder.insert, ChangeAction.INSERT)
            }
        }.onEach(notifyModelChange(ChangeAction.INSERT))

    override suspend fun DatabaseConnection.delete(model: Table): Table =
        generatedDatabase.writableTransaction {
            runUpdateDeleteOperation(
                model,
                tableSQL.delete,
                tableBinder.delete,
                ChangeAction.DELETE
            )
        }.also(notifyModelChange(ChangeAction.DELETE))

    override suspend fun DatabaseConnection.deleteAll(models: Collection<Table>): Collection<Table> =
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