package com.raizlabs.dbflow5.adapter.saveable

import com.raizlabs.dbflow5.Synchronized
import com.raizlabs.dbflow5.adapter.CacheAdapter
import com.raizlabs.dbflow5.database.DatabaseStatement
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.use

/**
 * Description: Used for model caching, enables caching models when saving in list.
 */
class CacheableListModelSaver<T : Any>(modelSaver: ModelSaver<T>,
                                       private val cacheAdapter: CacheAdapter<T>)
    : ListModelSaver<T>(modelSaver) {

    @Synchronized
    override fun saveAll(tableCollection: Collection<T>,
                         wrapper: DatabaseWrapper): Long {
        return applyAndCount(
            tableCollection = tableCollection,
            databaseStatement = modelAdapter.getSaveStatement(wrapper),
            cacheFn = cacheAdapter::storeModelInCache) { model, statement ->
            modelSaver.save(model, statement, wrapper)
        }
    }

    @Synchronized
    override fun insertAll(tableCollection: Collection<T>,
                           wrapper: DatabaseWrapper): Long {
        return applyAndCount(
            tableCollection = tableCollection,
            databaseStatement = modelAdapter.getInsertStatement(wrapper),
            cacheFn = cacheAdapter::storeModelInCache) { model, statement ->
            modelSaver.insert(model, statement, wrapper) > 0
        }
    }

    @Synchronized
    override fun updateAll(tableCollection: Collection<T>,
                           wrapper: DatabaseWrapper): Long {
        return applyAndCount(tableCollection, modelAdapter.getUpdateStatement(wrapper),
            cacheFn = cacheAdapter::storeModelInCache) { model, statement ->
            modelSaver.update(model, statement, wrapper)
        }
    }

    @Synchronized
    override fun deleteAll(tableCollection: Collection<T>,
                           wrapper: DatabaseWrapper): Long {
        return applyAndCount(tableCollection, modelAdapter.getDeleteStatement(wrapper),
            cacheFn = cacheAdapter::removeModelFromCache) { model, statement ->
            modelSaver.delete(model, statement, wrapper)
        }
    }

    private inline fun applyAndCount(tableCollection: Collection<T>,
                                     databaseStatement: DatabaseStatement,
                                     crossinline cacheFn: (T) -> Unit,
                                     crossinline fn: (T, DatabaseStatement) -> Boolean): Long {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return 0L
        }

        var count = 0L
        databaseStatement.use { statement ->
            tableCollection.forEach { model ->
                if (fn(model, statement)) {
                    cacheFn(model)
                    count++
                }
            }
        }
        return count
    }
}
