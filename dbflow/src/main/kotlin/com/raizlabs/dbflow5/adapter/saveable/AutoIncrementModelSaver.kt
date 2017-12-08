package com.raizlabs.dbflow5.adapter.saveable

import com.raizlabs.dbflow5.config.FlowLog
import com.raizlabs.dbflow5.database.DatabaseStatement
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.runtime.NotifyDistributor
import com.raizlabs.dbflow5.structure.ChangeAction

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
            if (id > INSERT_FAILED) {
                modelAdapter.updateAutoIncrement(model, id)
                NotifyDistributor.get().notifyModelChanged(model, modelAdapter, ChangeAction.INSERT)
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
