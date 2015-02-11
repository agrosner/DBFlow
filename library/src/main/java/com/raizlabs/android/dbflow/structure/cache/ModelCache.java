package com.raizlabs.android.dbflow.structure.cache;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Wraps around a {@link com.raizlabs.android.dbflow.structure.cache.LruCache}
 * and provides synchronization mechanisms.
 */
public class ModelCache<ModelClass extends Model> {

    private final LruCache<Long, ModelClass> mCache;

    public ModelCache(int size) {
        this.mCache = new LruCache<>(size);
    }

    public void addModel(Long id, ModelClass model) {
        synchronized (mCache) {
            mCache.put(id, model);
        }
    }

    public void removeModel(Long id) {
        synchronized (mCache) {
            mCache.remove(id);
        }
    }

    public void clear() {
        synchronized (mCache) {
            mCache.evictAll();
        }
    }

    public ModelClass get(Long id) {
        return mCache.get(id);
    }
}
