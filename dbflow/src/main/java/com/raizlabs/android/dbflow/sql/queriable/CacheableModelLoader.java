package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;

/**
 * Description: Loads model data that is backed by a {@link ModelCache}. Used when {@link Table#cachingEnabled()}
 * is true.
 */
public class CacheableModelLoader<TModel extends Model> extends SingleModelLoader<TModel> {

    private ModelAdapter<TModel> modelAdapter;

    public CacheableModelLoader(Class<TModel> modelClass) {
        super(modelClass);
        if (!(getInstanceAdapter() instanceof ModelAdapter)) {
            throw new IllegalArgumentException("A non-Table type was used.");
        }
        //noinspection unchecked
        modelAdapter = (ModelAdapter<TModel>) getInstanceAdapter();
    }

    /**
     * Converts data by loading from cache based on its sequence of caching ids. Will reuse the passed
     * {@link TModel} if it's not found in the cache and non-null.
     *
     * @return A model from cache.
     */
    @Nullable
    @Override
    protected TModel convertToData(@NonNull Cursor cursor, @Nullable TModel data) {
        ModelCache<TModel, ?> modelCache = modelAdapter.getModelCache();
        Object[] values = modelAdapter.getCachingColumnValuesFromCursor(
                new Object[modelAdapter.getCachingColumns().length], cursor);
        TModel model = modelCache.get(modelAdapter.getCachingId(values));
        if (model == null) {
            if (data == null) {
                model = modelAdapter.newInstance();
            } else {
                model = data;
            }
            modelAdapter.loadFromCursor(cursor, model);
            modelCache.addModel(modelAdapter.getCachingId(values), model);
        } else {
            modelAdapter.reloadRelationships(model, cursor);
        }
        return model;
    }
}
