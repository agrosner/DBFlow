package com.raizlabs.android.dbflow.adapter.queriable

import com.raizlabs.android.dbflow.adapter.ModelAdapter
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.database.DatabaseWrapper
import com.raizlabs.android.dbflow.database.FlowCursor
import com.raizlabs.android.dbflow.query.cache.ModelCache
import com.raizlabs.android.dbflow.query.cache.addOrReload

/**
 * Description: Loads model data that is backed by a [ModelCache]. Used when [Table.cachingEnabled]
 * is true.
 */
open class CacheableModelLoader<T : Any>(modelClass: Class<T>)
    : SingleModelLoader<T>(modelClass) {

    val modelAdapter: ModelAdapter<T> by lazy {
        when (instanceAdapter) {
            !is ModelAdapter<*> -> throw IllegalArgumentException("A non-Table type was used.")
            else -> {
                val modelAdapter = instanceAdapter as ModelAdapter<T>
                if (!modelAdapter.cachingEnabled()) {
                    throw IllegalArgumentException("""You cannot call this method for a table that
                        |has no caching id. Either use one Primary Key or
                        |use the MultiCacheKeyConverter""".trimMargin())
                }
                return@lazy modelAdapter
            }
        }
    }

    val modelCache: ModelCache<T, *> by lazy { modelAdapter.modelCache }

    /**
     * Converts data by loading from cache based on its sequence of caching ids. Will reuse the passed
     * [T] if it's not found in the cache and non-null.
     *
     * @return A model from cache.
     */
    override fun convertToData(cursor: FlowCursor, data: T?, moveToFirst: Boolean,
                               databaseWrapper: DatabaseWrapper): T? {
        return if (!moveToFirst || cursor.moveToFirst()) {
            val values = modelAdapter.getCachingColumnValuesFromCursor(
                    arrayOfNulls(modelAdapter.cachingColumns.size), cursor)
            modelCache.addOrReload(modelAdapter.getCachingId(values), modelAdapter,
                    cursor, databaseWrapper, data)
        } else null
    }
}
