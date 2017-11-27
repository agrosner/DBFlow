package com.raizlabs.android.dbflow.query.cache

import android.util.SparseArray

import com.raizlabs.android.dbflow.config.FlowLog

/**
 * Description: A cache backed by a [android.util.SparseArray]
 */
class SparseArrayBasedCache<TModel> : ModelCache<TModel, SparseArray<TModel>> {

    /**
     * Constructs new instance with a [android.util.SparseArray] cache
     */
    constructor() : super(SparseArray<TModel>()) {}

    /**
     * Constructs new instance with a [android.util.SparseArray] cache
     *
     * @param initialCapacity The initial capacity of the sparse array
     */
    constructor(initialCapacity: Int) : super(SparseArray<TModel>(initialCapacity)) {}

    /**
     * Constructs new instance with the specified [java.util.List]
     *
     * @param sparseArray The sparse array to use.
     */
    constructor(sparseArray: SparseArray<TModel>) : super(sparseArray) {}

    override fun addModel(id: Any?, model: TModel) {
        if (id is Number) {
            synchronized(cache) {
                cache.put(id.toInt(), model)
            }
        } else {
            throw IllegalArgumentException("A SparseArrayBasedCache must use an id that can cast to " + "a Number to convert it into a int")
        }
    }

    override fun removeModel(id: Any): TModel? {
        val model = get(id)
        synchronized(cache) {
            cache.remove((id as Number).toInt())
        }
        return model
    }

    override fun clear() {
        synchronized(cache) {
            cache.clear()
        }
    }

    override fun setCacheSize(size: Int) {
        FlowLog.log(FlowLog.Level.I, "The cache size for ${SparseArrayBasedCache::class.java.simpleName} is not re-configurable.")
    }

    override fun get(id: Any?): TModel? {
        return if (id is Number) {
            cache.get(id.toInt())
        } else {
            throw IllegalArgumentException("A SparseArrayBasedCache uses an id that can cast to "
                    + "a Number to convert it into a int")
        }
    }
}
