package com.raizlabs.dbflow5.query.cache

import com.raizlabs.dbflow5.config.FlowLog
import com.raizlabs.dbflow5.structure.Model

/**
 * Description: A simple implementation that keeps [Model] you interact with in memory.
 */
class SimpleMapCache<TModel> : ModelCache<TModel, MutableMap<Any?, TModel>> {

    /**
     * Constructs new instance with a [HashMap] with the specified capacity.
     *
     * @param capacity The capacity to use on the hashmap.
     */
    constructor(capacity: Int) : super(HashMap<Any?, TModel>(capacity))

    /**
     * Constructs new instance with a cache
     *
     * @param cache The arbitrary underlying cache class.
     */
    constructor(cache: MutableMap<Any?, TModel>) : super(cache)

    override fun addModel(id: Any?, model: TModel) {
        cache.put(id, model)
    }

    override fun removeModel(id: Any): TModel? = cache.remove(id)

    override fun clear() {
        cache.clear()
    }

    override fun get(id: Any?): TModel? = cache[id]

    override fun setCacheSize(size: Int) {
        FlowLog.log(FlowLog.Level.W, "The cache size for SimpleMapCache is not re-configurable.")
    }
}
