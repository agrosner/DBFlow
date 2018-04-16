package com.raizlabs.dbflow5.adapter.saveable

import com.raizlabs.dbflow5.Synchronized
import com.raizlabs.dbflow5.database.DatabaseStatement
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.use

open class ListModelSaver<T : Any>(val modelSaver: ModelSaver<T>) {

    val modelAdapter = modelSaver.modelAdapter

    @Synchronized
    open fun saveAll(tableCollection: Collection<T>,
                     wrapper: DatabaseWrapper): Long {
        return applyAndCount(tableCollection, modelAdapter.getSaveStatement(wrapper)) { model, statement ->
            modelSaver.save(model, statement, wrapper)
        }
    }

    @Synchronized
    open fun insertAll(tableCollection: Collection<T>,
                       wrapper: DatabaseWrapper): Long {
        return applyAndCount(tableCollection, modelAdapter.getInsertStatement(wrapper)) { model, statement ->
            modelSaver.insert(model, statement, wrapper) > ModelSaver.INSERT_FAILED
        }
    }

    @Synchronized
    open fun updateAll(tableCollection: Collection<T>,
                       wrapper: DatabaseWrapper): Long {
        return applyAndCount(tableCollection, modelAdapter.getUpdateStatement(wrapper)) { model, statement ->
            modelSaver.update(model, statement, wrapper)
        }
    }

    @Synchronized
    open fun deleteAll(tableCollection: Collection<T>,
                       wrapper: DatabaseWrapper): Long {
        return applyAndCount(tableCollection, modelAdapter.getDeleteStatement(wrapper)) { model, statement ->
            modelSaver.delete(model, statement, wrapper)
        }
    }

    private inline fun applyAndCount(tableCollection: Collection<T>,
                                     databaseStatement: DatabaseStatement,
                                     crossinline fn: (T, DatabaseStatement) -> Boolean): Long {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return 0L
        }

        var count = 0L
        databaseStatement.use { statement ->
            tableCollection.forEach {
                if (fn(it, statement)) {
                    count++
                }
            }
        }
        return count
    }
}
