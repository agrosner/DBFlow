package com.raizlabs.android.dbflow.structure.cache;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: A generic cache for models that is implemented or can be implemented to your liking.
 */
public abstract class ModelCache<ModelClass extends Model, CacheClass> {

    private CacheClass mCache;

    /**
     * Constructs new instance with a cache
     *
     * @param cache The arbitrary underlying cache class.
     */
    public ModelCache(CacheClass cache) {
        mCache = cache;
    }

    /**
     * Adds a model to this cache.
     *
     * @param id    The id of the model to use.
     * @param model The model to add
     */
    public abstract void addModel(Object id, ModelClass model);

    /**
     * Removes a model from this cache.
     *
     * @param id The id of the model to remove.
     */
    public abstract ModelClass removeModel(Object id);

    /**
     * Clears out all models from this cache.
     */
    public abstract void clear();

    /**
     * @param id The id of the model to retrieve.
     * @return a model for the specified id. May be null.
     */
    public abstract ModelClass get(Object id);

    /**
     * Sets a new size for the underlying cache (if applicable) and may destroy the cache.
     *
     * @param size The size of cache to set to
     */
    public abstract void setCacheSize(int size);

    /**
     * @return The cache that's backing this cache.
     */
    public CacheClass getCache() {
        return mCache;
    }
}
