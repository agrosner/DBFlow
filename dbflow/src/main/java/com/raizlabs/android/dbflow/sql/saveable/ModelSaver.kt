package com.raizlabs.android.dbflow.sql.saveable

import android.content.ContentValues
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.runtime.NotifyDistributor
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.ModelAdapter
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: Defines how models get saved into the DB. It will bind values to [DatabaseStatement]
 * for all CRUD operations as they are wildly faster and more efficient than [ContentValues].
 */
open class ModelSaver<T : Any> {

    lateinit var modelAdapter: ModelAdapter<T>

    @Synchronized
    fun save(model: T,
             wrapper: DatabaseWrapper): Boolean {
        return save(model, wrapper, modelAdapter.getInsertStatement(wrapper),
                modelAdapter.getUpdateStatement(wrapper))
    }

    @Synchronized
    fun save(model: T,
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
            NotifyDistributor().notifyModelChanged(model, modelAdapter, BaseModel.Action.SAVE)
        }

        // return successful store into db.
        return exists
    }

    @Synchronized
    fun update(model: T, wrapper: DatabaseWrapper): Boolean {
        val updateStatement = modelAdapter.getUpdateStatement(wrapper)
        val success: Boolean
        try {
            success = update(model, wrapper, updateStatement)
        } finally {
            // since we generate an insert every time, we can safely close the statement here.
            updateStatement.close()
        }
        return success
    }

    @Synchronized
    fun update(model: T, wrapper: DatabaseWrapper,
               databaseStatement: DatabaseStatement): Boolean {
        modelAdapter.saveForeignKeys(model, wrapper)
        modelAdapter.bindToUpdateStatement(databaseStatement, model)
        val successful = databaseStatement.executeUpdateDelete() != 0L
        if (successful) {
            NotifyDistributor().notifyModelChanged(model, modelAdapter, BaseModel.Action.UPDATE)
        }
        return successful
    }

    @Synchronized open fun insert(model: T, wrapper: DatabaseWrapper): Long {
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

    @Synchronized open fun insert(model: T,
                                  insertStatement: DatabaseStatement,
                                  wrapper: DatabaseWrapper): Long {
        modelAdapter.saveForeignKeys(model, wrapper)
        modelAdapter.bindToInsertStatement(insertStatement, model)
        val id = insertStatement.executeInsert()
        if (id > INSERT_FAILED) {
            modelAdapter.updateAutoIncrement(model, id)
            NotifyDistributor().notifyModelChanged(model, modelAdapter, BaseModel.Action.INSERT)
        }
        return id
    }

    @Synchronized
    fun delete(model: T, wrapper: DatabaseWrapper): Boolean {
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
    fun delete(model: T,
               deleteStatement: DatabaseStatement,
               wrapper: DatabaseWrapper): Boolean {
        modelAdapter.deleteForeignKeys(model, wrapper)
        modelAdapter.bindToDeleteStatement(deleteStatement, model)

        val success = deleteStatement.executeUpdateDelete() != 0L
        if (success) {
            NotifyDistributor().notifyModelChanged(model, modelAdapter, BaseModel.Action.DELETE)
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
    fun save(model: T,
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
            NotifyDistributor().notifyModelChanged(model, modelAdapter, BaseModel.Action.SAVE)
        }

        // return successful store into db.
        return exists
    }

    /**
     * @see .update
     */
    @Deprecated("")
    @Synchronized
    fun update(model: T, wrapper: DatabaseWrapper, contentValues: ContentValues): Boolean {
        modelAdapter.saveForeignKeys(model, wrapper)
        modelAdapter.bindToContentValues(contentValues, model)
        val successful = wrapper.updateWithOnConflict(modelAdapter.tableName, contentValues,
                modelAdapter.getPrimaryConditionClause(model).query, null,
                ConflictAction.getSQLiteDatabaseAlgorithmInt(modelAdapter.updateOnConflictAction)) != 0L
        if (successful) {
            NotifyDistributor().notifyModelChanged(model, modelAdapter, BaseModel.Action.UPDATE)
        }
        return successful
    }

    companion object {

        val INSERT_FAILED = -1
    }
}

