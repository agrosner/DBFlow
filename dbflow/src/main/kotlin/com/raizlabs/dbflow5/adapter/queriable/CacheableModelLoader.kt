package com.raizlabs.dbflow5.adapter.queriable

import com.raizlabs.dbflow5.adapter.CacheAdapter
import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.annotation.Table
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor
import com.raizlabs.dbflow5.query.cache.ModelCache
import com.raizlabs.dbflow5.query.cache.addOrReload

/**
 * Description: Loads model data that is backed by a [ModelCache]. Used when [Table.cachingEnabled]
 * is true.
 */
open class CacheableModelLoader<T : Any>(modelClass: Class<T>,
                                         protected val cacheAdapter: CacheAdapter<T>)
    : SingleModelLoader<T>(modelClass) {

    protected val modelAdapter: ModelAdapter<T> by lazy {
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

    protected val modelCache: ModelCache<T, *> by lazy { cacheAdapter.modelCache }

    /**
     * Converts data by loading from cache based on its sequence of caching ids. Will reuse the passed
     * [T] if it's not found in the cache and non-null.
     *
     * @return A model from cache.
     */
    override fun convertToData(cursor: FlowCursor, moveToFirst: Boolean,
                               databaseWrapper: DatabaseWrapper): T? {
        return if (!moveToFirst || cursor.moveToFirst()) {
            val values = cacheAdapter.getCachingColumnValuesFromCursor(
                    arrayOfNulls(cacheAdapter.cachingColumnSize), cursor)
            modelCache.addOrReload(cacheAdapter.getCachingId(values), cacheAdapter, modelAdapter,
                    cursor, databaseWrapper)
        } else null
    }
}
