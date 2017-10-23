package com.raizlabs.android.dbflow.sql.saveable

import android.content.ContentValues

import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.runtime.NotifyDistributor
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.ModelAdapter
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: Defines how models get saved into the DB. It will bind values to [DatabaseStatement]
 * for all CRUD operations as they are wildly faster and more efficient than [ContentValues].
 */
open class ModelSaver<TModel> {

    lateinit var modelAdapter: ModelAdapter<TModel>

    protected val writableDatabase: DatabaseWrapper
        get() = FlowManager.getDatabaseForTable(modelAdapter.modelClass).writableDatabase

    @Synchronized
    fun save(model: TModel): Boolean {
        return save(model, writableDatabase, modelAdapter.insertStatement,
                modelAdapter.updateStatement)
    }

    @Synchronized
    fun save(model: TModel,
             wrapper: DatabaseWrapper): Boolean {
        return save(model, wrapper, modelAdapter.getInsertStatement(wrapper),
                modelAdapter.getUpdateStatement(wrapper))
    }

    @Synchronized
    fun save(model: TModel,
             wrapper: DatabaseWrapper,
             insertStatement: DatabaseStatement,
             updateStatement: DatabaseStatement): Boolean {
        var exists = modelAdapter.exists(model, wrapper)

        if (exists) {
            exists = update(model, wrapper, updateStatement)
        }

        if (!exists) {
            exists = insert(model, insertStatement, wrapper) > INSERT_FAILED
        }

        if (exists) {
            NotifyDistributor.get().notifyModelChanged(model, modelAdapter, BaseModel.Action.SAVE)
        }

        // return successful store into db.
        return exists
    }

    @Synchronized
    fun update(model: TModel): Boolean {
        return update(model, writableDatabase, modelAdapter.updateStatement)
    }

    @Synchronized
    fun update(model: TModel, wrapper: DatabaseWrapper): Boolean {
        val updateStatement = modelAdapter.getUpdateStatement(wrapper)
        var success = false
        try {
            success = update(model, wrapper, updateStatement)
        } finally {
            // since we generate an insert every time, we can safely close the statement here.
            updateStatement.close()
        }
        return success
    }

    @Synchronized
    fun update(model: TModel, wrapper: DatabaseWrapper,
               databaseStatement: DatabaseStatement): Boolean {
        modelAdapter.saveForeignKeys(model, wrapper)
        modelAdapter.bindToUpdateStatement(databaseStatement, model)
        val successful = databaseStatement.executeUpdateDelete() != 0L
        if (successful) {
            NotifyDistributor.get().notifyModelChanged(model, modelAdapter, BaseModel.Action.UPDATE)
        }
        return successful
    }

    @Synchronized open fun insert(model: TModel): Long {
        return insert(model, modelAdapter.insertStatement, writableDatabase)
    }

    @Synchronized open fun insert(model: TModel, wrapper: DatabaseWrapper): Long {
        val insertStatement = modelAdapter.getInsertStatement(wrapper)
        var result: Long = 0
        try {
            result = insert(model, insertStatement, wrapper)
        } finally {
            // since we generate an insert every time, we can safely close the statement here.
            insertStatement.close()
        }
        return result
    }

    @Synchronized open fun insert(model: TModel,
                                  insertStatement: DatabaseStatement,
                                  wrapper: DatabaseWrapper): Long {
        modelAdapter.saveForeignKeys(model, wrapper)
        modelAdapter.bindToInsertStatement(insertStatement, model)
        val id = insertStatement.executeInsert()
        if (id > INSERT_FAILED) {
            modelAdapter.updateAutoIncrement(model, id)
            NotifyDistributor.get().notifyModelChanged(model, modelAdapter, BaseModel.Action.INSERT)
        }
        return id
    }

    @Synchronized
    fun delete(model: TModel): Boolean {
        return delete(model, modelAdapter.deleteStatement, writableDatabase)
    }

    @Synchronized
    fun delete(model: TModel, wrapper: DatabaseWrapper): Boolean {
        val deleteStatement = modelAdapter.getDeleteStatement(wrapper)
        var success = false
        try {
            success = delete(model, deleteStatement, wrapper)
        } finally {
            // since we generate an insert every time, we can safely close the statement here.
            deleteStatement.close()
        }
        return success
    }

    @Synchronized
    fun delete(model: TModel,
               deleteStatement: DatabaseStatement,
               wrapper: DatabaseWrapper): Boolean {
        modelAdapter.deleteForeignKeys(model, wrapper)
        modelAdapter.bindToDeleteStatement(deleteStatement, model)

        val success = deleteStatement.executeUpdateDelete() != 0L
        if (success) {
            NotifyDistributor.get().notifyModelChanged(model, modelAdapter, BaseModel.Action.DELETE)
        }
        modelAdapter.updateAutoIncrement(model, 0)
        return success
    }

    /**
     * Legacy save method. Uses [ContentValues] vs. the faster [DatabaseStatement] for updates.
     *
     * @see .save
     */
    @Deprecated("")
    @Synchronized
    fun save(model: TModel,
             wrapper: DatabaseWrapper,
             insertStatement: DatabaseStatement,
             contentValues: ContentValues): Boolean {
        var exists = modelAdapter.exists(model, wrapper)

        if (exists) {
            exists = update(model, wrapper, contentValues)
        }

        if (!exists) {
            exists = insert(model, insertStatement, wrapper) > INSERT_FAILED
        }

        if (exists) {
            NotifyDistributor.get().notifyModelChanged(model, modelAdapter, BaseModel.Action.SAVE)
        }

        // return successful store into db.
        return exists
    }

    /**
     * @see .update
     */
    @Deprecated("")
    @Synchronized
    fun update(model: TModel, wrapper: DatabaseWrapper, contentValues: ContentValues): Boolean {
        modelAdapter.saveForeignKeys(model, wrapper)
        modelAdapter.bindToContentValues(contentValues, model)
        val successful = wrapper.updateWithOnConflict(modelAdapter.tableName, contentValues,
                modelAdapter.getPrimaryConditionClause(model).query, null,
                ConflictAction.getSQLiteDatabaseAlgorithmInt(modelAdapter.updateOnConflictAction)) != 0L
        if (successful) {
            NotifyDistributor.get().notifyModelChanged(model, modelAdapter, BaseModel.Action.UPDATE)
        }
        return successful
    }

    companion object {

        val INSERT_FAILED = -1
    }
}

