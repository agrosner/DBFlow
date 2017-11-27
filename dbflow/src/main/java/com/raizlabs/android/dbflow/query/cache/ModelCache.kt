package com.raizlabs.android.dbflow.query.cache

/**
 * Description: A generic cache for models that is implemented or can be implemented to your liking.
 */
abstract class ModelCache<TModel, out CacheClass>
/**
 * Constructs new instance with a cache
 *
 * @param cache The arbitrary underlying cache class.
 */
(
        /**
         * @return The cache that's backing this cache.
         */
        val cache: CacheClass) {

    /**
     * Adds a model to this cache.
     *
     * @param id    The id of the model to use.
     * @param model The model to add
     */
    abstract fun addModel(id: Any?, model: TModel)

    /**
     * Removes a model from this cache.
     *
     * @param id The id of the model to remove.
     */
    abstract fun removeModel(id: Any): TModel?

    /**
     * Clears out all models from this cache.
     */
    abstract fun clear()

    /**
     * @param id The id of the model to retrieve.
     * @return a model for the specified id. May be null.
     */
    abstract operator fun get(id: Any?): TModel?

    /**
     * Sets a new size for the underlying cache (if applicable) and may destroy the cache.
     *
     * @param size The size of cache to set to
     */
    abstract fun setCacheSize(size: Int)
}
