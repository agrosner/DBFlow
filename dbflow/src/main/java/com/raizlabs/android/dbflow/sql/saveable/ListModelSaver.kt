package com.raizlabs.android.dbflow.sql.saveable

import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
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
                       wrapper: DatabaseWrapper): Long {
        return applyAndCount(tableCollection, wrapper,
                modelSaver.modelAdapter.getInsertStatement(wrapper)) { model, statement, databaseWrapper ->
            modelSaver.insert(model, statement, databaseWrapper) > 0
        }
    }

    @Synchronized
    open fun updateAll(tableCollection: Collection<T>,
                       wrapper: DatabaseWrapper): Long {
        return applyAndCount(tableCollection, wrapper,
                modelSaver.modelAdapter.getUpdateStatement(wrapper), modelSaver::update)
    }

    @Synchronized
    open fun deleteAll(tableCollection: Collection<T>,
                       wrapper: DatabaseWrapper): Long {
        return applyAndCount(tableCollection, wrapper,
                modelSaver.modelAdapter.getDeleteStatement(wrapper), modelSaver::delete)
    }

    private inline fun applyAndCount(tableCollection: Collection<T>,
                                     wrapper: DatabaseWrapper,
                                     databaseStatement: DatabaseStatement,
                                     crossinline fn: (T, DatabaseStatement, DatabaseWrapper) -> Boolean): Long {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return 0L
        }

        var count = 0L
        try {
            tableCollection.forEach {
                if (fn(it, databaseStatement, wrapper)) {
                    count++
                }
            }
        } finally {
            databaseStatement.close()
        }
        return count
    }
}
