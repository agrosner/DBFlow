package com.raizlabs.android.dbflow.sql.queriable

import com.raizlabs.android.dbflow.structure.Model
import com.raizlabs.android.dbflow.structure.database.FlowCursor

/**
 * Description: More optimized version of [CacheableModelLoader] which assumes that the [Model]
 * only utilizes a single primary key.
 */
class SingleKeyCacheableModelLoader<TModel>(modelClass: Class<TModel>)
    : CacheableModelLoader<TModel>(modelClass) {

    /**
     * Converts data by loading from cache based on its sequence of caching ids. Will reuse the passed
     * [TModel] if it's not found in the cache and non-null.
     *
     * @return A model from cache.
     */
    override fun convertToData(cursor: FlowCursor, data: TModel?,
                               moveToFirst: Boolean): TModel? {
        if (!moveToFirst || cursor.moveToFirst()) {
            val value = modelAdapter.getCachingColumnValueFromCursor(cursor)
            var model: TModel? = modelCache.get(value)
            if (model == null) {
                model = (data ?: modelAdapter.newInstance()).apply {
                    modelAdapter.loadFromCursor(cursor, this)
                    modelCache.addModel(value, this)
                }
            } else {
                modelAdapter.reloadRelationships(model, cursor)
            }
            return model
        } else {
            return null
        }
    }
}
