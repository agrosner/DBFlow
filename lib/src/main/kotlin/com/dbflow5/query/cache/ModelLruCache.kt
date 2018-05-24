package com.dbflow5.query.cache

import android.util.LruCache
import com.dbflow5.annotation.DEFAULT_CACHE_SIZE

/**
 * Description: Provides an [android.util.LruCache] under its hood
 * and provides synchronization mechanisms.
 */
class ModelLruCache<TModel>(size: Int)
    : ModelCache<TModel, LruCache<Long, TModel>>(LruCache<Long, TModel>(size)) {

    override fun addModel(id: Any?, model: TModel) {
        throwIfNotNumber(id) {
            synchronized(cache) {
                cache.put(it.toLong(), model)
            }
        }
    }

    override fun removeModel(id: Any): TModel? = throwIfNotNumber(id) {
        synchronized(cache) {
            cache.remove(it.toLong())
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

    override fun get(id: Any?): TModel? = throwIfNotNumber(id) { cache[it.toLong()] }

    private inline fun <R> throwIfNotNumber(id: Any?, fn: (Number) -> R) =
        if (id is Number) {
            fn(id)
        } else {
            throw IllegalArgumentException("A ModelLruCache must use an id that can cast to"
                + "a Number to convert it into a long")
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
