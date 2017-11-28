package com.raizlabs.android.dbflow.adapter.queriable

import com.raizlabs.android.dbflow.database.DatabaseWrapper
import com.raizlabs.android.dbflow.database.FlowCursor
import com.raizlabs.android.dbflow.query.cache.addOrReload

/**
 * Description:
 */
class SingleKeyCacheableListModelLoader<T : Any>(tModelClass: Class<T>)
    : CacheableListModelLoader<T>(tModelClass) {

    override fun convertToData(cursor: FlowCursor, data: MutableList<T>?,
                               databaseWrapper: DatabaseWrapper): MutableList<T> {
        val _data = data ?: arrayListOf()
        var cacheValue: Any?
        // Ensure that we aren't iterating over this cursor concurrently from different threads
        if (cursor.moveToFirst()) {
            do {
                cacheValue = modelAdapter.getCachingColumnValueFromCursor(cursor)
                val model = modelCache.addOrReload(cacheValue, modelAdapter, cursor, databaseWrapper)
                _data.add(model)
            } while (cursor.moveToNext())
        }
        return _data
    }

}
