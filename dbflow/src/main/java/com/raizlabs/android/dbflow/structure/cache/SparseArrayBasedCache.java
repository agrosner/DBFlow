package com.raizlabs.android.dbflow.structure.cache;

import android.util.SparseArray;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: A cache backed by a {@link android.util.SparseArray}
 */
public class SparseArrayBasedCache<TModel extends Model> extends ModelCache<TModel, SparseArray<TModel>> {

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
    public SparseArrayBasedCache(SparseArray<TModel> sparseArray) {
        super(sparseArray);
    }

    @Override
    public void addModel(Object id, TModel model) {
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
    public TModel removeModel(Object id) {
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
        FlowLog.log(FlowLog.Level.I, "The cache size for " + SparseArrayBasedCache.class.getSimpleName() + " is not re-configurable.");
    }

    @Override
    public TModel get(Object id) {
        if (id instanceof Number) {
            return getCache().get(((Number) id).intValue());
        } else {
            throw new IllegalArgumentException("A SparseArrayBasedCache uses an id that can cast to " +
                    "a Number to convert it into a int");
        }
    }
}
