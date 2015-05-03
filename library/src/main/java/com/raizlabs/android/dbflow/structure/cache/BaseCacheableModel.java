package com.raizlabs.android.dbflow.structure.cache;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
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
     * it creates a new instance and tries to get the cache again.
     */
    @SuppressWarnings("unchecked")
    public static <CacheClass extends BaseCacheableModel> ModelCache<CacheClass, ?> getCache(Class<CacheClass> table) {
        ModelCache<CacheClass, ?> cache = mCacheMap.get(table);
        if (cache == null) {
            FlowManager.getModelAdapter(table).newInstance();
            cache = mCacheMap.get(table);
        }
        return cache;
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
        mCache = mCacheMap.get(getClass());
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
    public void save() {
        super.save();
        addToCache();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void delete() {
        Object id = getModelAdapter().getCachingId(this);
        super.delete();
        mCache.removeModel(id);
    }

    @Override
    public void update() {
        super.update();
        addToCache();
    }

    @Override
    public void insert() {
        super.insert();
        addToCache();
    }

    @Override
    public void onLoadFromCursor(Cursor cursor) {
        addToCache();
    }

    @SuppressWarnings("unchecked")
    protected void addToCache() {
        mCache.addModel(getModelAdapter().getCachingId(this), this);
    }

    /**
     * @return Override this method to specify the size of the cache you wish to maintain for this class. It is only called if
     * the cache is not created yet.
     */
    public int getCacheSize() {
        return DEFAULT_CACHE_SIZE;
    }


}
