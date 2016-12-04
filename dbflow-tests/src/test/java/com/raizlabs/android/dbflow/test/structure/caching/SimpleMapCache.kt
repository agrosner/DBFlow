package com.raizlabs.android.dbflow.test.structure.caching

import com.raizlabs.android.dbflow.structure.Model
import com.raizlabs.android.dbflow.structure.cache.ModelCache

import java.util.HashMap

/**
 * Description: An implementation of the [ModelCache] that stores its contents in a [Map].
 * It will grow and continue to grow unless you [.clear] out it's contents. It's good for simple
 * data, but not recommended when holding a significant amount.
 */
class SimpleMapCache<TModel> : ModelCache<TModel, Map<String, TModel>> {

    constructor() : super(HashMap<String, TModel>()) {
    }

    /**
     * Constructs new instance with a cache

     * @param cache The arbitrary underlying cache class.
     */
    constructor(cache: Map<String, TModel>) : super(cache) {
    }

    override fun addModel(id: Any, model: TModel) {
        cache.put(id.toString(), model)
    }

    override fun removeModel(id: Any): TModel {
        return cache.remove(id)
    }

    override fun clear() {
        cache.clear()
    }

    override fun get(id: Any): TModel {
        return cache.get(id)
    }

    override fun setCacheSize(size: Int) {
        // ignored. Doesn't do anything.
    }
}
