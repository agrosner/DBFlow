package com.raizlabs.android.dbflow.sql.saveable

import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: Used for model caching, enables caching models when saving in list.
 */
class CacheableListModelSaver<T : Any>(modelSaver: ModelSaver<T>) : ListModelSaver<T>(modelSaver) {

    @Synchronized override fun saveAll(tableCollection: Collection<T>,
                                       wrapper: DatabaseWrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return
        }

        val modelSaver = modelSaver
        val modelAdapter = modelSaver.modelAdapter
        val statement = modelAdapter.getInsertStatement(wrapper)
        val updateStatement = modelAdapter.getUpdateStatement(wrapper)
        try {
            for (model in tableCollection) {
                if (modelSaver.save(model, wrapper, statement, updateStatement)) {
                    modelAdapter.storeModelInCache(model)
                }
            }
        } finally {
            updateStatement.close()
            statement.close()
        }
    }

    @Synchronized override fun insertAll(tableCollection: Collection<T>,
                                         wrapper: DatabaseWrapper): Long {
        return applyAndCount(tableCollection, wrapper,
                modelSaver.modelAdapter.getInsertStatement(wrapper),
                modelSaver.modelAdapter::storeModelInCache) { model, statement, databaseWrapper ->
            modelSaver.insert(model, statement, databaseWrapper) > 0
        }
    }

    @Synchronized override fun updateAll(tableCollection: Collection<T>,
                                         wrapper: DatabaseWrapper): Long {
        return applyAndCount(tableCollection, wrapper,
                modelSaver.modelAdapter.getUpdateStatement(wrapper),
                modelSaver.modelAdapter::storeModelInCache,
                modelSaver::update)
    }

    @Synchronized override fun deleteAll(tableCollection: Collection<T>,
                                         wrapper: DatabaseWrapper): Long {
        return applyAndCount(tableCollection, wrapper,
                modelSaver.modelAdapter.getDeleteStatement(wrapper),
                modelSaver.modelAdapter::removeModelFromCache,
                modelSaver::delete)
    }

    private inline fun applyAndCount(tableCollection: Collection<T>,
                                     wrapper: DatabaseWrapper,
                                     databaseStatement: DatabaseStatement,
                                     crossinline cacheFn: (T) -> Unit,
                                     crossinline fn: (T, DatabaseStatement, DatabaseWrapper) -> Boolean): Long {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return 0L
        }

        var count = 0L
        try {
            tableCollection.forEach {
                if (fn(it, databaseStatement, wrapper)) {
                    cacheFn(it)
                    count++
                }
            }
        } finally {
            databaseStatement.close()
        }
        return count
    }
}
