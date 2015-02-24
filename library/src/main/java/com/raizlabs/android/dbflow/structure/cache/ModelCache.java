package com.raizlabs.android.dbflow.structure.cache;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: A generic cache for models that is implemented or can be implemented to your liking.
 */
public abstract class ModelCache<ModelClass extends Model, CacheClass> extends KeyedModelCache<ModelClass, CacheClass, Long> {

    /**
     * Constructs new instance with a cache
     *
     * @param cache The arbitrary underlying cache class.
     */
    public ModelCache(CacheClass cache) {
        super(cache);
    }
}
