package com.raizlabs.android.dbflow.structure.cache;

import android.util.SparseArray;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: A cache backed by a {@link android.util.SparseArray}
 */
public class SparseArrayBasedCache<ModelClass extends Model> extends ModelCache<ModelClass, SparseArray<ModelClass>> {
    /**
     * Constructs new instance with a {@link android.util.SparseArray} cache
     */
    public SparseArrayBasedCache() {
        super(new SparseArray<ModelClass>());
    }

    /**
     * Constructs new instance with the specified {@link java.util.List}
     *
     * @param sparseArray The sparse array to use.
     */
    public SparseArrayBasedCache(SparseArray<ModelClass> sparseArray) {
        super(sparseArray);
    }

    @Override
    public void addModel(Long id, ModelClass model) {
        synchronized (getCache()) {
            getCache().put(id.intValue(), model);
        }
    }

    @Override
    public ModelClass removeModel(Long id) {
        ModelClass model = get(id);
        synchronized (getCache()) {
            getCache().remove(id.intValue());
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
    public ModelClass get(Long id) {
        return id == null ? null : getCache().get(id.intValue());
    }
}
