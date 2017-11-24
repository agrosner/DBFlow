package com.raizlabs.android.dbflow.sql.queriable

import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.ModelAdapter
import com.raizlabs.android.dbflow.structure.cache.ModelCache
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.FlowCursor

/**
 * Description: Loads a [List] of [T] with [Table.cachingEnabled] true.
 */
open class CacheableListModelLoader<T : Any>(modelClass: Class<T>)
    : ListModelLoader<T>(modelClass) {

    val modelAdapter: ModelAdapter<T> by lazy {
        if (instanceAdapter !is ModelAdapter<*>) {
            throw IllegalArgumentException("A non-Table type was used.")
        }
        val modelAdapter = instanceAdapter as ModelAdapter<T>
        if (!modelAdapter.cachingEnabled()) {
            throw IllegalArgumentException("You cannot call this method for a table that has no caching id. Either" + "use one Primary Key or use the MultiCacheKeyConverter")
        }
        return@lazy modelAdapter
    }

    val modelCache: ModelCache<T, *> by lazy { modelAdapter.modelCache }

    override fun convertToData(cursor: FlowCursor, data: MutableList<T>?,
                               databaseWrapper: DatabaseWrapper): MutableList<T> {
        val _data = data ?: arrayListOf()
        val cacheValues = arrayOfNulls<Any>(modelAdapter.cachingColumns.size)
        // Ensure that we aren't iterating over this cursor concurrently from different threads
        if (cursor.moveToFirst()) {
            do {
                val values = modelAdapter.getCachingColumnValuesFromCursor(cacheValues, cursor)
                var model: T? = modelCache[modelAdapter.getCachingId(values)]
                if (model != null) {
                    modelAdapter.reloadRelationships(model, cursor, databaseWrapper)
                    _data.add(model)
                } else {
                    model = modelAdapter.newInstance()
                    modelAdapter.loadFromCursor(cursor, model, databaseWrapper)
                    modelCache.addModel(modelAdapter.getCachingId(values), model)
                    _data.add(model)
                }
            } while (cursor.moveToNext())
        }
        return _data
    }

}
