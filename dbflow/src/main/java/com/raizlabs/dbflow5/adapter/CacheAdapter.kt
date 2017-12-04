package com.raizlabs.dbflow5.adapter

import com.raizlabs.dbflow5.annotation.DEFAULT_CACHE_SIZE
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor
import com.raizlabs.dbflow5.query.cache.IMultiKeyCacheConverter
import com.raizlabs.dbflow5.query.cache.ModelCache
import com.raizlabs.dbflow5.query.cache.SimpleMapCache
import com.raizlabs.dbflow5.structure.InvalidDBConfiguration

/**
 * Description:
 */
abstract class CacheAdapter<T : Any> {

    val modelCache: ModelCache<T, *> by lazy { createModelCache() }

    open val cacheSize: Int
        get() = DEFAULT_CACHE_SIZE

    open val cachingColumnSize: Int
        get() = 1

    open val cacheConverter: IMultiKeyCacheConverter<*>
        get() = throw InvalidDBConfiguration("For multiple primary keys, a public static IMultiKeyCacheConverter field must" +
            "be  marked with @MultiCacheField in the corresponding model class. The resulting key" +
            "must be a unique combination of the multiple keys, otherwise inconsistencies may occur.")

    /**
     * @param cursor The cursor to load caching id from.
     * @return The single cache column from cursor (if single).
     */
    open fun getCachingColumnValueFromCursor(cursor: FlowCursor): Any? = Unit

    /**
     * @param model The model to load cache column data from.
     * @return The single cache column from model (if single).
     */
    open fun getCachingColumnValueFromModel(model: T): Any? = Unit


    /**
     * Loads all primary keys from the [FlowCursor] into the inValues. The size of the array must
     * match all primary keys. This method gets generated when caching is enabled.
     *
     * @param inValues The reusable array of values to populate.
     * @param cursor   The cursor to load from.
     * @return The populated set of values to load from cache.
     */
    open fun getCachingColumnValuesFromCursor(inValues: Array<Any?>, cursor: FlowCursor): Array<Any>? = null

    /**
     * Loads all primary keys from the [TModel] into the inValues. The size of the array must
     * match all primary keys. This method gets generated when caching is enabled. It converts the primary fields
     * of the [TModel] into the array of values the caching mechanism uses.
     *
     * @param inValues The reusable array of values to populate.
     * @param TModel   The model to load from.
     * @return The populated set of values to load from cache.
     */
    open fun getCachingColumnValuesFromModel(inValues: Array<Any?>, TModel: T): Array<Any>? = null

    fun storeModelInCache(model: T) {
        modelCache.addModel(getCachingId(model), model)
    }

    fun removeModelFromCache(model: T) {
        getCachingId(model)?.let { modelCache.removeModel(it) }
    }

    fun getCachingId(inValues: Array<Any>?): Any? = when {
        inValues?.size == 1 -> // if it exists in cache no matter the query we will use that one
            inValues.getOrNull(0)
        inValues != null -> cacheConverter.getCachingKey(inValues)
        else -> null
    }

    open fun getCachingId(model: T): Any? =
        getCachingId(getCachingColumnValuesFromModel(arrayOfNulls(cachingColumnSize), model))


    open fun createModelCache(): ModelCache<T, *> = SimpleMapCache(cacheSize)

    /**
     * Reloads relationships when loading from [FlowCursor] in a model that's cacheable. By having
     * relationships with cached models, the retrieval will be very fast.
     *
     * @param cursor The cursor to reload from.
     */
    open fun reloadRelationships(model: T, cursor: FlowCursor, databaseWrapper: DatabaseWrapper) = Unit

}