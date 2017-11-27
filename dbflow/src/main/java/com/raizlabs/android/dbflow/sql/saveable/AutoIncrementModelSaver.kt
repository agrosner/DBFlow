package com.raizlabs.android.dbflow.sql.saveable

import com.raizlabs.android.dbflow.config.FlowLog
import com.raizlabs.android.dbflow.runtime.NotifyDistributor
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: Used to properly handle autoincrementing fields.
 */
class AutoIncrementModelSaver<T : Any> : ModelSaver<T>() {

    @Synchronized override fun insert(model: T, wrapper: DatabaseWrapper): Long {
        val hasAutoIncrement = modelAdapter.hasAutoIncrement(model)
        val insertStatement = when {
            hasAutoIncrement -> modelAdapter.getCompiledStatement(wrapper)
            else -> modelAdapter.getInsertStatement(wrapper)
        }
        val id: Long
        try {
            modelAdapter.saveForeignKeys(model, wrapper)
            if (hasAutoIncrement) {
                modelAdapter.bindToStatement(insertStatement, model)
            } else {
                modelAdapter.bindToInsertStatement(insertStatement, model)
            }
            id = insertStatement.executeInsert()
            if (id > ModelSaver.INSERT_FAILED) {
                modelAdapter.updateAutoIncrement(model, id)
                NotifyDistributor.get().notifyModelChanged(model, modelAdapter, BaseModel.Action.INSERT)
            }
        } finally {
            // since we generate an insert every time, we can safely close the statement here.
            insertStatement.close()
        }
        return id
    }

    @Synchronized override fun insert(model: T,
                                      insertStatement: DatabaseStatement,
                                      wrapper: DatabaseWrapper): Long {
        return if (!modelAdapter.hasAutoIncrement(model)) {
            super.insert(model, insertStatement, wrapper)
        } else {
            FlowLog.log(FlowLog.Level.W, "Ignoring insert statement $insertStatement since an autoincrement column specified in the insert.")
            insert(model, wrapper)
        }
    }
}
