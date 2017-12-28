package com.raizlabs.android.dbflow.structure.cache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: A simple implementation that keeps {@link Model} you interact with in memory.
 */
public class SimpleMapCache<TModel> extends ModelCache<TModel, Map<Object, TModel>> {

    /**
     * Constructs new instance with a {@link HashMap} with the specified capacity.
     *
     * @param capacity The capacity to use on the hashmap.
     */
    public SimpleMapCache(int capacity) {
        super(new HashMap<Object, TModel>(capacity));
    }

    /**
     * Constructs new instance with a cache
     *
     * @param cache The arbitrary underlying cache class.
     */
    public SimpleMapCache(@NonNull Map<Object, TModel> cache) {
        super(cache);
    }

    @Override
    public void addModel(@Nullable Object id, @NonNull TModel model) {
        getCache().put(id, model);
    }

    @Override
    public TModel removeModel(@NonNull Object id) {
        return getCache().remove(id);
    }

    @Override
    public void clear() {
        getCache().clear();
    }

    @Override
    public TModel get(@Nullable Object id) {
        return getCache().get(id);
    }

    @Override
    public void setCacheSize(int size) {
        FlowLog.log(FlowLog.Level.I, "The cache size for " + SimpleMapCache.class.getSimpleName() +
                " is not re-configurable.");
    }
}
