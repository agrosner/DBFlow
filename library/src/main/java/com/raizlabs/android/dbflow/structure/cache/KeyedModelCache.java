package com.raizlabs.android.dbflow.structure.cache;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Enables any kind of {@link KeyClass} to be used as the key in the cache.
 */
public abstract class KeyedModelCache<ModelClass extends Model, CacheClass, KeyClass> {

    private CacheClass mCache;

    /**
     * Constructs new instance with a cache
     *
     * @param cache The arbitrary underlying cache class.
     */
    public KeyedModelCache(CacheClass cache) {
        mCache = cache;
    }

    /**
     * Adds a model to this cache.
     *
     * @param id    The id of the model to use.
     * @param model The model to add
     */
    public abstract void addModel(KeyClass id, ModelClass model);

    /**
     * Removes a model from this cache.
     *
     * @param id The id of the model to remove.
     */
    public abstract ModelClass removeModel(KeyClass id);

    /**
     * Clears out all models from this cache.
     */
    public abstract void clear();

    /**
     * @param id The id of the model to retrieve.
     * @return a model for the specified id. May be null.
     */
    public abstract ModelClass get(KeyClass id);

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
