package com.raizlabs.dbflow5.adapter.queriable

import com.raizlabs.dbflow5.KClass
import com.raizlabs.dbflow5.adapter.CacheAdapter
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor
import com.raizlabs.dbflow5.query.cache.addOrReload
import com.raizlabs.dbflow5.structure.Model

/**
 * Description: More optimized version of [CacheableModelLoader] which assumes that the [Model]
 * only utilizes a single primary key.
 */
class SingleKeyCacheableModelLoader<T : Any>(modelClass: KClass<T>,
                                             cacheAdapter: CacheAdapter<T>)
    : CacheableModelLoader<T>(modelClass, cacheAdapter) {

    /**
     * Converts data by loading from cache based on its sequence of caching ids. Will reuse the passed
     * [T] if it's not found in the cache and non-null.
     *
     * @return A model from cache.
     */
    override fun convertToData(cursor: FlowCursor, moveToFirst: Boolean, databaseWrapper: DatabaseWrapper): T? {
        return if (!moveToFirst || cursor.moveToFirst()) {
            val value = cacheAdapter.getCachingColumnValueFromCursor(cursor)
            modelCache.addOrReload(value, cacheAdapter, modelAdapter, cursor, databaseWrapper)
        } else null
    }
}
