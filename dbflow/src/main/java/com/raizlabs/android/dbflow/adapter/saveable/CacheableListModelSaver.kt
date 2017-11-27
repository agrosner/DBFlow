package com.raizlabs.android.dbflow.adapter.saveable

import com.raizlabs.android.dbflow.database.DatabaseStatement
import com.raizlabs.android.dbflow.database.DatabaseWrapper

/**
 * Description: Used for model caching, enables caching models when saving in list.
 */
class CacheableListModelSaver<T : Any>(modelSaver: ModelSaver<T>) : ListModelSaver<T>(modelSaver) {

    @Synchronized override fun saveAll(tableCollection: Collection<T>,
                                       wrapper: DatabaseWrapper): Long {
        val statement = modelAdapter.getInsertStatement(wrapper)
        val updateStatement = modelAdapter.getUpdateStatement(wrapper)
        return applyAndCount(tableCollection, statement, updateStatement, modelAdapter::storeModelInCache) {
            modelSaver.save(it, statement, updateStatement, wrapper)
        }
    }

    @Synchronized override fun insertAll(tableCollection: Collection<T>,
                                         wrapper: DatabaseWrapper): Long {
        val statement = modelAdapter.getInsertStatement(wrapper)
        return applyAndCount(tableCollection, statement,
                cacheFn = modelAdapter::storeModelInCache) {
            modelSaver.insert(it, statement, wrapper) > 0
        }
    }

    @Synchronized override fun updateAll(tableCollection: Collection<T>,
                                         wrapper: DatabaseWrapper): Long {
        val statement = modelAdapter.getUpdateStatement(wrapper)
        return applyAndCount(tableCollection, statement,
                cacheFn = modelAdapter::storeModelInCache) {
            modelSaver.update(it, statement, wrapper)
        }
    }

    @Synchronized override fun deleteAll(tableCollection: Collection<T>,
                                         wrapper: DatabaseWrapper): Long {
        val statement = modelAdapter.getDeleteStatement(wrapper)
        return applyAndCount(tableCollection, statement,
                cacheFn = modelAdapter::removeModelFromCache) {
            modelSaver.delete(it, statement, wrapper)
        }
    }

    private inline fun applyAndCount(tableCollection: Collection<T>,
                                     databaseStatement: DatabaseStatement,
                                     otherStatement: DatabaseStatement? = null,
                                     crossinline cacheFn: (T) -> Unit,
                                     crossinline fn: (T) -> Boolean): Long {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return 0L
        }

        var count = 0L
        try {
            tableCollection.forEach {
                if (fn(it)) {
                    cacheFn(it)
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
