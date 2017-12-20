package com.raizlabs.dbflow5.query.cache

import android.util.LruCache
import com.raizlabs.dbflow5.annotation.DEFAULT_CACHE_SIZE

/**
 * Description: Provides an [android.util.LruCache] under its hood
 * and provides synchronization mechanisms.
 */
class ModelLruCache<TModel>(size: Int)
    : ModelCache<TModel, LruCache<Long, TModel>>(LruCache<Long, TModel>(size)) {

    override fun addModel(id: Any?, model: TModel) {
        if (id is Number) {
            synchronized(cache) {
                cache.put(id.toLong(), model)
            }
        } else {
            throw IllegalArgumentException("A ModelLruCache must use an id that can cast to" + "a Number to convert it into a long")
        }
    }

    override fun removeModel(id: Any): TModel? {
        if (id is Number) {
            synchronized(cache) {
                return cache.remove(id.toLong())
            }
        } else {
            throw IllegalArgumentException("A ModelLruCache uses an id that can cast to" + "a Number to convert it into a long")
        }
    }

    override fun clear() {
        synchronized(cache) {
            cache.evictAll()
        }
    }

    override fun setCacheSize(size: Int) {
        cache.resize(size)
    }

    override fun get(id: Any?): TModel? {
        return if (id is Number) {
            cache.get(id.toLong())
        } else {
            throw IllegalArgumentException("A ModelLruCache must use an id that can cast to" + "a Number to convert it into a long")
        }
    }

    companion object {

        /**
         * @param size The size, if less than or equal to 0 we set it to [DEFAULT_CACHE_SIZE].
         */
        fun <TModel> newInstance(size: Int): ModelLruCache<TModel> {
            var locSize = size
            if (locSize <= 0) {
                locSize = DEFAULT_CACHE_SIZE
            }
            return ModelLruCache(locSize)
        }
    }
}
