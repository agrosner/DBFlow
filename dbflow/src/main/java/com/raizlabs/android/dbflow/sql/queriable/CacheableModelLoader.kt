package com.raizlabs.android.dbflow.sql.queriable

import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.ModelAdapter
import com.raizlabs.android.dbflow.structure.cache.ModelCache
import com.raizlabs.android.dbflow.structure.database.FlowCursor

/**
 * Description: Loads model data that is backed by a [ModelCache]. Used when [Table.cachingEnabled]
 * is true.
 */
open class CacheableModelLoader<TModel>(modelClass: Class<TModel>)
    : SingleModelLoader<TModel>(modelClass) {

    val modelAdapter by lazy {
        if (instanceAdapter !is ModelAdapter<*>) {
            throw IllegalArgumentException("A non-Table type was used.")
        }
        val modelAdapter = instanceAdapter as ModelAdapter<TModel>
        if (!modelAdapter.cachingEnabled()) {
            throw IllegalArgumentException("You cannot call this method for a table that has no caching id. Either" + "use one Primary Key or use the MultiCacheKeyConverter")
        }
        return@lazy modelAdapter
    }

    val modelCache: ModelCache<TModel, *> by lazy { modelAdapter.modelCache }

    /**
     * Converts data by loading from cache based on its sequence of caching ids. Will reuse the passed
     * [TModel] if it's not found in the cache and non-null.
     *
     * @return A model from cache.
     */
    override fun convertToData(cursor: FlowCursor, data: TModel?, moveToFirst: Boolean): TModel? {
        if (!moveToFirst || cursor.moveToFirst()) {
            val values = modelAdapter.getCachingColumnValuesFromCursor(
                    arrayOfNulls(modelAdapter.cachingColumns.size), cursor)
            var model: TModel? = modelCache.get(modelAdapter.getCachingId(values))
            if (model == null) {
                model = (data ?: modelAdapter.newInstance()).apply {
                    modelAdapter.loadFromCursor(cursor, this)
                    modelCache.addModel(modelAdapter.getCachingId(values), this)
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
