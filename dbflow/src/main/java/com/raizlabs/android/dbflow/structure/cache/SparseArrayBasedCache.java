package com.raizlabs.android.dbflow.structure.cache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.raizlabs.android.dbflow.config.FlowLog;

/**
 * Description: A cache backed by a {@link android.util.SparseArray}
 */
public class SparseArrayBasedCache<TModel> extends ModelCache<TModel, SparseArray<TModel>> {

    /**
     * Constructs new instance with a {@link android.util.SparseArray} cache
     */
    public SparseArrayBasedCache() {
        super(new SparseArray<TModel>());
    }

    /**
     * Constructs new instance with a {@link android.util.SparseArray} cache
     *
     * @param initialCapacity The initial capacity of the sparse array
     */
    public SparseArrayBasedCache(int initialCapacity) {
        super(new SparseArray<TModel>(initialCapacity));
    }

    /**
     * Constructs new instance with the specified {@link java.util.List}
     *
     * @param sparseArray The sparse array to use.
     */
    public SparseArrayBasedCache(@NonNull SparseArray<TModel> sparseArray) {
        super(sparseArray);
    }

    @Override
    public void addModel(@Nullable Object id, @NonNull TModel model) {
        if (id instanceof Number) {
            synchronized (getCache()) {
                getCache().put(((Number) id).intValue(), model);
            }
        } else {
            throw new IllegalArgumentException("A SparseArrayBasedCache must use an id that can cast to " +
                    "a Number to convert it into a int");
        }
    }

    @Override
    public TModel removeModel(@NonNull Object id) {
        TModel model = get(id);
        synchronized (getCache()) {
            getCache().remove(((Number) id).intValue());
        }
        return model;
    }

    @Override
    public void clear() {
        synchronized (getCache()) {
            getCache().clear();
        }
    }

    @Override
    public void setCacheSize(int size) {
        FlowLog.log(FlowLog.INSTANCE.Level.I, "The cache size for " + SparseArrayBasedCache.class.getSimpleName() + " is not re-configurable.");
    }

    @Override
    public TModel get(@Nullable Object id) {
        if (id instanceof Number) {
            return getCache().get(((Number) id).intValue());
        } else {
            throw new IllegalArgumentException("A SparseArrayBasedCache uses an id that can cast to " +
                    "a Number to convert it into a int");
        }
    }
}
