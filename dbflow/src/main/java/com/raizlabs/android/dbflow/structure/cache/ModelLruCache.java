package com.raizlabs.android.dbflow.structure.cache;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Provides an {@link com.raizlabs.android.dbflow.structure.cache.LruCache} under its hood
 * and provides synchronization mechanisms.
 */
public class ModelLruCache<ModelClass extends Model> extends ModelCache<ModelClass, LruCache<Long, ModelClass>> {

    /**
     * @param size The size, if less than or equal to 0 we set it to {@link Table#DEFAULT_CACHE_SIZE}.
     */
    public static <ModelClass extends Model> ModelLruCache<ModelClass> newInstance(int size) {
        if (size <= 0) {
            size = Table.DEFAULT_CACHE_SIZE;
        }
        return new ModelLruCache<>(size);
    }

    protected ModelLruCache(int size) {
        super(new LruCache<Long, ModelClass>(size));
    }

    @Override
    public void addModel(Object id, ModelClass model) {
        if (id instanceof Number) {
            synchronized (getCache()) {
                Number number = ((Number) id);
                getCache().put(number.longValue(), model);
            }
        } else {
            throw new IllegalArgumentException("A ModelLruCache must use an id that can cast to" +
                    "a Number to convert it into a long");
        }
    }

    @Override
    public ModelClass removeModel(Object id) {
        ModelClass model;
        if (id instanceof Number) {
            synchronized (getCache()) {
                model = getCache().remove(((Number) id).longValue());
            }
        } else {
            throw new IllegalArgumentException("A ModelLruCache uses an id that can cast to" +
                    "a Number to convert it into a long");
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
    public ModelClass get(Object id) {
        if (id instanceof Number) {
            return getCache().get(((Number) id).longValue());
        } else {
            throw new IllegalArgumentException("A ModelLruCache must use an id that can cast to" +
                    "a Number to convert it into a long");
        }
    }
}
