package com.raizlabs.android.dbflow.test.structure.caching;

import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: An implementation of the {@link ModelCache} that stores its contents in a {@link Map}.
 * It will grow and continue to grow unless you {@link #clear()} out it's contents. It's good for simple
 * data, but not recommended when holding a significant amount.
 */
public class SimpleMapCache<TModel> extends ModelCache<TModel, Map<String, TModel>> {

    public SimpleMapCache() {
        super(new HashMap<String, TModel>());
    }

    /**
     * Constructs new instance with a cache
     *
     * @param cache The arbitrary underlying cache class.
     */
    public SimpleMapCache(Map<String, TModel> cache) {
        super(cache);
    }

    @Override
    public void addModel(Object id, TModel model) {
        getCache().put(String.valueOf(id), model);
    }

    @Override
    public TModel removeModel(Object id) {
        return getCache().remove(id);
    }

    @Override
    public void clear() {
        getCache().clear();
    }

    @Override
    public TModel get(Object id) {
        return getCache().get(id);
    }

    @Override
    public void setCacheSize(int size) {
        // ignored. Doesn't do anything.
    }
}
