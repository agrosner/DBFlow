package com.raizlabs.android.dbflow.sql.saveable

import com.raizlabs.android.dbflow.structure.ModelAdapter
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: Used for model caching, enables caching models when saving in list.
 */
class CacheableListModelSaver<TModel>(modelSaver: ModelSaver<TModel>) : ListModelSaver<TModel>(modelSaver) {

    @Synchronized override fun saveAll(tableCollection: Collection<TModel>,
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

    @Synchronized override fun insertAll(tableCollection: Collection<TModel>,
                                         wrapper: DatabaseWrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return
        }

        val modelSaver = modelSaver
        val modelAdapter = modelSaver.modelAdapter
        val statement = modelAdapter.getInsertStatement(wrapper)
        try {
            for (model in tableCollection) {
                if (modelSaver.insert(model, statement, wrapper) > 0) {
                    modelAdapter.storeModelInCache(model)
                }
            }
        } finally {
            statement.close()
        }
    }

    @Synchronized override fun updateAll(tableCollection: Collection<TModel>,
                                         wrapper: DatabaseWrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return
        }
        val modelSaver = modelSaver
        val modelAdapter = modelSaver.modelAdapter
        val statement = modelAdapter.getUpdateStatement(wrapper)
        try {
            for (model in tableCollection) {
                if (modelSaver.update(model, wrapper, statement)) {
                    modelAdapter.storeModelInCache(model)
                }
            }
        } finally {
            statement.close()
        }
    }

    @Synchronized override fun deleteAll(tableCollection: Collection<TModel>,
                                         wrapper: DatabaseWrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return
        }

        val modelSaver = modelSaver
        for (model in tableCollection) {
            if (modelSaver.delete(model, wrapper)) {
                modelSaver.modelAdapter.removeModelFromCache(model)
            }
        }
    }
}
