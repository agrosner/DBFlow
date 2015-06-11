package com.raizlabs.android.dbflow.test.structure.caching;

import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 */
public class SimpleMapCache<ModelClass extends Model> extends ModelCache<ModelClass, Map<String, ModelClass>> {

    public SimpleMapCache() {
        super(new HashMap<String, ModelClass>());
    }

    /**
     * Constructs new instance with a cache
     *
     * @param cache The arbitrary underlying cache class.
     */
    public SimpleMapCache(Map<String, ModelClass> cache) {
        super(cache);
    }

    @Override
    public void addModel(Object id, ModelClass model) {
        getCache().put(String.valueOf(id), model);
    }

    @Override
    public ModelClass removeModel(Object id) {
        return getCache().remove(id);
    }

    @Override
    public void clear() {
        getCache().clear();
    }

    @Override
    public ModelClass get(Object id) {
        return getCache().get(id);
    }

    @Override
    public void setCacheSize(int size) {
        // ignored. Doesn't do anything.
    }
}
