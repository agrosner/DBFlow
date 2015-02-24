package com.raizlabs.android.dbflow.structure.cache;

import android.database.Cursor;

import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.InvalidDBConfiguration;
import com.raizlabs.android.dbflow.structure.listener.LoadFromCursorListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: Provides a handy way to cache models in memory for even faster data retrieval. Note:
 * this class must utilize an {@link com.raizlabs.android.dbflow.annotation.Column#PRIMARY_KEY_AUTO_INCREMENT}
 * or describe a {@link com.raizlabs.android.dbflow.annotation.Column#PRIMARY_KEY} that returns an id using
 * the {@link com.raizlabs.android.dbflow.annotation.Table}
 * primary key. The corresponding {@link com.raizlabs.android.dbflow.structure.ModelAdapter}
 * describes how to retrieve the Id field so that is why its required.
 */
public abstract class BaseCacheableModel extends BaseModel implements LoadFromCursorListener {

    public static final int DEFAULT_CACHE_SIZE = 1000;

    private static Map<Class<? extends BaseCacheableModel>, ModelCache> mCacheMap = new HashMap<>();

    /**
     * @param table        The cacheable model class to use
     * @param <CacheClass> The class that implements {@link com.raizlabs.android.dbflow.structure.cache.BaseCacheableModel}
     * @return A {@link com.raizlabs.android.dbflow.structure.cache.ModelCache} if it exists in memory. If not,
     * null is returned.
     */
    @SuppressWarnings("unchecked")
    public static <CacheClass extends BaseCacheableModel> ModelCache<CacheClass, ?> getCache(Class<CacheClass> table) {
        return mCacheMap.get(table);
    }

    /**
     * Called when a {@link com.raizlabs.android.dbflow.structure.cache.BaseCacheableModel} is created if
     * no existing cache exists.
     *
     * @param table      The cacheable model to use
     * @param modelCache The cache to store.
     */
    static void putCache(Class<? extends BaseCacheableModel> table, ModelCache<? extends BaseCacheableModel, ?> modelCache) {
        mCacheMap.put(table, modelCache);
    }

    /**
     * The cache to use
     */
    private ModelCache mCache;

    /**
     * Constructs a new instance, instantiating the {@link com.raizlabs.android.dbflow.structure.cache.ModelCache}
     * if it does not already exist for this model.
     */
    @SuppressWarnings("unchecked")
    public BaseCacheableModel() {
        mCache = getCache(getClass());
        if (mCache == null) {
            mCache = getBackingCache();
            putCache(getClass(), mCache);
        }
    }

    /**
     * @return A cache to use for this class statically. Only called if an existing cache does not exist. Override
     * this method to use your own.
     */
    protected ModelCache<? extends BaseCacheableModel, ?> getBackingCache() {
        return new ModelLruCache<>(getCacheSize());
    }

    @Override
    public void save(boolean async) {
        super.save(async);
        if (!async) {
            addToCache();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void delete(boolean async) {
        long id = getModelAdapter().getAutoIncrementingId(this);
        super.delete(async);
        if (!async) {
            mCache.removeModel(id);
        }
    }

    @Override
    public void update(boolean async) {
        super.update(async);
        if (!async) {
            addToCache();
        }
    }

    @Override
    public void insert(boolean async) {
        super.insert(async);
        if (!async) {
            addToCache();
        }
    }

    @Override
    public void onLoadFromCursor(Cursor cursor) {
        addToCache();
    }

    @SuppressWarnings("unchecked")
    protected void addToCache() {
        long id = getModelAdapter().getCachingId(this);
        if (id == 0) {
            throw new InvalidDBConfiguration(String.format("The cacheable model class %1s must contain" +
                    "an autoincrementing primary key. Although its possible that this method was called" +
                    "after an insert/update/save failure", getClass()));
        } else {
            mCache.addModel(id, this);
        }
    }

    /**
     * @return Override this method to specify the size of the cache you wish to maintain for this class. It is only called if
     * the cache is not created yet.
     */
    public int getCacheSize() {
        return DEFAULT_CACHE_SIZE;
    }


}
