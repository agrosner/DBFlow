package com.raizlabs.android.dbflow.adapter.queriable

import com.raizlabs.android.dbflow.database.DatabaseWrapper
import com.raizlabs.android.dbflow.database.FlowCursor

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
                var model: T? = modelCache[cacheValue]
                if (model != null) {
                    modelAdapter.reloadRelationships(model, cursor, databaseWrapper)
                } else {
                    model = modelAdapter.newInstance()
                    modelAdapter.loadFromCursor(cursor, model, databaseWrapper)
                    modelCache.addModel(cacheValue, model)
                }
                _data.add(model)
            } while (cursor.moveToNext())
        }
        return _data
    }

}
