package com.raizlabs.android.dbflow.sql.saveable

import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

open class ListModelSaver<T : Any>(val modelSaver: ModelSaver<T>) {

    val modelAdapter = modelSaver.modelAdapter

    @Synchronized
    open fun saveAll(tableCollection: Collection<T>,
                     wrapper: DatabaseWrapper): Long {
        val statement = modelAdapter.getInsertStatement(wrapper)
        val updateStatement = modelAdapter.getUpdateStatement(wrapper)
        return applyAndCount(tableCollection, statement, updateStatement) {
            modelSaver.save(it, statement, updateStatement, wrapper)
        }
    }

    @Synchronized
    open fun insertAll(tableCollection: Collection<T>,
                       wrapper: DatabaseWrapper): Long {
        val statement = modelAdapter.getInsertStatement(wrapper)
        return applyAndCount(tableCollection, statement) { modelSaver.insert(it, statement, wrapper) > 0 }
    }

    @Synchronized
    open fun updateAll(tableCollection: Collection<T>,
                       wrapper: DatabaseWrapper): Long {
        val statement = modelAdapter.getUpdateStatement(wrapper)
        return applyAndCount(tableCollection, statement) { modelSaver.update(it, statement, wrapper) }
    }

    @Synchronized
    open fun deleteAll(tableCollection: Collection<T>,
                       wrapper: DatabaseWrapper): Long {
        val statement = modelAdapter.getDeleteStatement(wrapper)
        return applyAndCount(tableCollection, statement) { modelSaver.delete(it, statement, wrapper) }
    }

    private inline fun applyAndCount(tableCollection: Collection<T>,
                                     databaseStatement: DatabaseStatement,
                                     otherStatement: DatabaseStatement? = null,
                                     crossinline fn: (T) -> Boolean): Long {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return 0L
        }

        var count = 0L
        try {
            tableCollection.forEach {
                if (fn(it)) {
                    count++
                }
            }
        } finally {
            databaseStatement.close()
            otherStatement?.close()
        }
        return count
    }
}
