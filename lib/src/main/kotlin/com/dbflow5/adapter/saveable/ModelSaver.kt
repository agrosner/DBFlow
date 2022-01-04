package com.dbflow5.adapter.saveable

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.runtime.NotifyDistributor
import com.dbflow5.structure.ChangeAction

/**
 * Description: Defines how models get saved into the DB. It will bind values to [DatabaseStatement]
 * for all CRUD operations as they are wildly faster and more efficient than ContentValues.
 */
open class ModelSaver<T : Any> {

    lateinit var modelAdapter: ModelAdapter<T>

    @Synchronized
    fun save(model: T, wrapper: DatabaseWrapper): Result<T> {
        val insertStatement = modelAdapter.getSaveStatement(wrapper)
        return insertStatement.use { save(model, insertStatement, wrapper) }
    }

    @Synchronized
    fun save(
        model: T,
        insertStatement: DatabaseStatement,
        wrapper: DatabaseWrapper
    ): Result<T> {
        var localModel = modelAdapter.saveForeignKeys(model, wrapper)
        modelAdapter.bindToInsertStatement(insertStatement, localModel)
        val id = insertStatement.executeInsert()
        val success = id > INSERT_FAILED
        if (success) {
            localModel = modelAdapter.updateAutoIncrement(model, id)
            NotifyDistributor().notifyModelChanged(localModel, modelAdapter, ChangeAction.CHANGE)
            return Result.success(localModel)
        }
        return Result.failure(SaveOperationFailedException("save"))
    }

    @Synchronized
    fun update(model: T, wrapper: DatabaseWrapper): Result<T> {
        val updateStatement = modelAdapter.getUpdateStatement(wrapper)
        return updateStatement.use { update(model, it, wrapper) }
    }

    @Synchronized
    fun update(
        model: T,
        databaseStatement: DatabaseStatement,
        wrapper: DatabaseWrapper
    ): Result<T> {
        val localModel = modelAdapter.saveForeignKeys(model, wrapper)
        modelAdapter.bindToUpdateStatement(databaseStatement, localModel)
        val successful = databaseStatement.executeUpdateDelete() != 0L
        if (successful) {
            NotifyDistributor().notifyModelChanged(localModel, modelAdapter, ChangeAction.UPDATE)
            return Result.success(localModel)
        }
        return Result.failure(SaveOperationFailedException("update"))
    }

    @Synchronized
    open fun insert(model: T, wrapper: DatabaseWrapper): Result<T> {
        val insertStatement = modelAdapter.getInsertStatement(wrapper)
        return insertStatement.use { insert(model, it, wrapper) }
    }

    @Synchronized
    open fun insert(
        model: T,
        insertStatement: DatabaseStatement,
        wrapper: DatabaseWrapper
    ): Result<T> {
        var localModel = modelAdapter.saveForeignKeys(model, wrapper)
        modelAdapter.bindToInsertStatement(insertStatement, localModel)
        val id = insertStatement.executeInsert()
        if (id > INSERT_FAILED) {
            localModel = modelAdapter.updateAutoIncrement(localModel, id)
            NotifyDistributor().notifyModelChanged(localModel, modelAdapter, ChangeAction.INSERT)
            return Result.success(localModel)
        }
        return Result.failure(SaveOperationFailedException("insert"))
    }

    @Synchronized
    fun delete(model: T, wrapper: DatabaseWrapper): Result<T> {
        val deleteStatement = modelAdapter.getDeleteStatement(wrapper)
        return deleteStatement.use { delete(model, it) }
    }

    @Synchronized
    fun delete(
        model: T,
        deleteStatement: DatabaseStatement
    ): Result<T> {
        modelAdapter.bindToDeleteStatement(deleteStatement, model)

        val success = deleteStatement.executeUpdateDelete() != 0L
        if (success) {
            NotifyDistributor().notifyModelChanged(model, modelAdapter, ChangeAction.DELETE)
            return Result.success(modelAdapter.updateAutoIncrement(model, 0))
        }
        return Result.failure(SaveOperationFailedException("delete"))
    }

    companion object {

        const val INSERT_FAILED = -1
    }
}

