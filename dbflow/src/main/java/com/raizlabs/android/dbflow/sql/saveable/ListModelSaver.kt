package com.raizlabs.android.dbflow.sql.saveable

import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

open class ListModelSaver<T : Any>(val modelSaver: ModelSaver<T>) {

    @Synchronized
    open fun saveAll(tableCollection: Collection<T>,
                     wrapper: DatabaseWrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return
        }

        val statement = modelSaver.modelAdapter.getInsertStatement(wrapper)
        val updateStatement = modelSaver.modelAdapter.getUpdateStatement(wrapper)
        try {
            tableCollection.forEach { modelSaver.save(it, wrapper, statement, updateStatement) }
        } finally {
            statement.close()
            updateStatement.close()
        }
    }

    @Synchronized
    open fun insertAll(tableCollection: Collection<T>,
                       wrapper: DatabaseWrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return
        }

        val statement = modelSaver.modelAdapter.getInsertStatement(wrapper)
        try {
            tableCollection.forEach { modelSaver.insert(it, statement, wrapper) }
        } finally {
            statement.close()
        }
    }

    @Synchronized
    open fun updateAll(tableCollection: Collection<T>,
                       wrapper: DatabaseWrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return
        }

        val updateStatement = modelSaver.modelAdapter.getUpdateStatement(wrapper)
        try {
            tableCollection.forEach { modelSaver.update(it, wrapper, updateStatement) }
        } finally {
            updateStatement.close()
        }
    }

    @Synchronized
    open fun deleteAll(tableCollection: Collection<T>,
                       wrapper: DatabaseWrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return
        }

        val deleteStatement = modelSaver.modelAdapter.getDeleteStatement(wrapper)
        try {
            tableCollection.forEach { modelSaver.delete(it, deleteStatement, wrapper) }
        } finally {
            deleteStatement.close()
        }
    }
}
