package com.raizlabs.android.dbflow.structure.cache;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Provides an {@link com.raizlabs.android.dbflow.structure.cache.LruCache} under its hood
 * and provides synchronization mechanisms.
 */
public class ModelLruCache<TModel> extends ModelCache<TModel, LruCache<Long, TModel>> {

    /**
     * @param size The size, if less than or equal to 0 we set it to {@link Table#DEFAULT_CACHE_SIZE}.
     */
    public static <TModel> ModelLruCache<TModel> newInstance(int size) {
        if (size <= 0) {
            size = Table.DEFAULT_CACHE_SIZE;
        }
        return new ModelLruCache<>(size);
    }

    protected ModelLruCache(int size) {
        super(new LruCache<Long, TModel>(size));
    }

    @Override
    public void addModel(Object id, TModel model) {
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
    public TModel removeModel(Object id) {
        TModel model;
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
    public TModel get(Object id) {
        if (id instanceof Number) {
            return getCache().get(((Number) id).longValue());
        } else {
            throw new IllegalArgumentException("A ModelLruCache must use an id that can cast to" +
                    "a Number to convert it into a long");
        }
    }
}
