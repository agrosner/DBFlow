package com.raizlabs.android.dbflow.adapter.queriable

import com.raizlabs.android.dbflow.database.DatabaseWrapper
import com.raizlabs.android.dbflow.database.FlowCursor
import com.raizlabs.android.dbflow.query.cache.addOrReload
import com.raizlabs.android.dbflow.structure.Model

/**
 * Description: More optimized version of [CacheableModelLoader] which assumes that the [Model]
 * only utilizes a single primary key.
 */
class SingleKeyCacheableModelLoader<T : Any>(modelClass: Class<T>)
    : CacheableModelLoader<T>(modelClass) {

    /**
     * Converts data by loading from cache based on its sequence of caching ids. Will reuse the passed
     * [T] if it's not found in the cache and non-null.
     *
     * @return A model from cache.
     */
    override fun convertToData(cursor: FlowCursor, data: T?,
                               moveToFirst: Boolean,
                               databaseWrapper: DatabaseWrapper): T? {
        return if (!moveToFirst || cursor.moveToFirst()) {
            val value = modelAdapter.getCachingColumnValueFromCursor(cursor)
            modelCache.addOrReload(value, modelAdapter,
                    cursor, databaseWrapper, data)
        } else null
    }
}
