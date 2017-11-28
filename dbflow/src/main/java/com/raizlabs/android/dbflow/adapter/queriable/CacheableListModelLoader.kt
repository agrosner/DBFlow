package com.raizlabs.android.dbflow.adapter.queriable

import com.raizlabs.android.dbflow.adapter.ModelAdapter
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.database.DatabaseWrapper
import com.raizlabs.android.dbflow.database.FlowCursor
import com.raizlabs.android.dbflow.query.cache.ModelCache
import com.raizlabs.android.dbflow.query.cache.addOrReload

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
                val model = modelCache.addOrReload(modelAdapter.getCachingId(values),
                        modelAdapter, cursor, databaseWrapper)
                _data.add(model)
            } while (cursor.moveToNext())
        }
        return _data
    }

}
