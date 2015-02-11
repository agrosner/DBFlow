package com.raizlabs.android.dbflow.structure.cache;

import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.InvalidDBConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: Provides a handy way to cache models in memory for even faster data retrieval. Note:
 * this class must utilize an {@link com.raizlabs.android.dbflow.annotation.Column#PRIMARY_KEY_AUTO_INCREMENT}
 * primary key. The corresponding {@link com.raizlabs.android.dbflow.structure.ModelAdapter}
 * describes how to retrieve the Id field so that is why its required.
 */
public abstract class BaseCacheableModel extends BaseModel {

    private static Map<Class<? extends BaseCacheableModel>, ModelCache> mCacheMap = new HashMap<>();

    public static ModelCache getCache(Class<? extends BaseCacheableModel> table) {
        return mCacheMap.get(table);
    }

    static void putCache(Class<? extends BaseCacheableModel> table, ModelCache modelCache) {
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
    public BaseCacheableModel() {
        mCache = getCache(getClass());
        if (mCache == null) {
            mCache = new ModelCache(getCacheSize());
            putCache(getClass(), mCache);
        }
    }

    @Override
    public void save(boolean async) {
        super.save(async);
        if (!async) {
            addToCache();
        }
    }

    @Override
    public void delete(boolean async) {
        super.delete(async);
        if (!async) {
            addToCache();
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

    @SuppressWarnings("unchecked")
    protected void addToCache() {
        long id = getModelAdapter().getAutoIncrementingId(this);
        if(id == 0) {
            throw new InvalidDBConfiguration(String.format("The cacheable model class %1s must contain" +
                    "an autoincrementing primary key. Although its possible that this method was called" +
                    "after an insert/update/save failure", getClass()));
        } else {
            mCache.addModel(id, this);
        }
    }

    /**
     * @return the size of the cache you wish to maintain for this class. It is only called if
     * the cache is not created yet.
     */
    public abstract int getCacheSize();

}
