package com.raizlabs.android.dbflow.sql.saveable

import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

open class ListModelSaver<TModel>(val modelSaver: ModelSaver<TModel>) {

    @Synchronized
    fun saveAll(tableCollection: Collection<TModel>) {
        saveAll(tableCollection, modelSaver.writableDatabase)
    }

    @Synchronized
    fun saveAll(tableCollection: Collection<TModel>,
                wrapper: DatabaseWrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return
        }

        val statement = modelSaver.modelAdapter.getInsertStatement(wrapper)
        val updateStatement = modelSaver.modelAdapter.getUpdateStatement(wrapper)
        try {
            for (model in tableCollection) {
                modelSaver.save(model, wrapper, statement, updateStatement)
            }
        } finally {
            statement.close()
            updateStatement.close()
        }
    }

    @Synchronized
    fun insertAll(tableCollection: Collection<TModel>) {
        insertAll(tableCollection, modelSaver.writableDatabase)
    }

    @Synchronized
    fun insertAll(tableCollection: Collection<TModel>,
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
    fun updateAll(tableCollection: Collection<TModel>) {
        updateAll(tableCollection, modelSaver.writableDatabase)
    }

    @Synchronized
    fun updateAll(tableCollection: Collection<TModel>,
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
    fun deleteAll(tableCollection: Collection<TModel>) {
        deleteAll(tableCollection, modelSaver.writableDatabase)
    }

    @Synchronized
    fun deleteAll(tableCollection: Collection<TModel>,
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
