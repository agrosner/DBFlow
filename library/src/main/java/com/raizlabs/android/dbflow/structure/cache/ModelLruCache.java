package com.raizlabs.android.dbflow.structure.cache;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Provides an {@link com.raizlabs.android.dbflow.structure.cache.LruCache} under its hood
 * and provides synchronization mechanisms.
 */
public class ModelLruCache<ModelClass extends Model> extends ModelCache<ModelClass, LruCache<Long, ModelClass>>{

    public ModelLruCache(int size) {
        super(new LruCache<Long, ModelClass>(size));
    }

    @Override
    public void addModel(Long id, ModelClass model) {
        synchronized (getCache()) {
            getCache().put(id, model);
        }
    }

    @Override
    public ModelClass removeModel(Long id) {
        ModelClass model = null;
        synchronized (getCache()) {
            model = getCache().remove(id);
        }
        return model;
    }

    @Override
    public void clear() {
        synchronized (getCache()) {
            getCache().evictAll();
        }
    }

    @Override
    public void setCacheSize(int size) {
        getCache().resize(size);
    }

    @Override
    public ModelClass get(Long id) {
        return id == null ? null : getCache().get(id);
    }
}
