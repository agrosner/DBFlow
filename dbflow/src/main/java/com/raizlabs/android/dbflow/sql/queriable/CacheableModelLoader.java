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
    }

    @SuppressWarnings("unchecked")
    public ModelAdapter<TModel> getModelAdapter() {
        if (modelAdapter == null) {
            if (!(getInstanceAdapter() instanceof ModelAdapter)) {
                throw new IllegalArgumentException("A non-Table type was used.");
            }
            modelAdapter = (ModelAdapter<TModel>) getInstanceAdapter();
            if (!modelAdapter.cachingEnabled()) {
                throw new IllegalArgumentException("You cannot call this method for a table that has no caching id. Either" +
                        "use one Primary Key or call convertToList()");
            }
        }
        return modelAdapter;
    }

    /**
     * Converts data by loading from cache based on its sequence of caching ids. Will reuse the passed
     * {@link TModel} if it's not found in the cache and non-null.
     *
     * @return A model from cache.
     */
    @Nullable
    @Override
    public TModel convertToData(@NonNull Cursor cursor, @Nullable TModel data) {
        if (cursor.moveToFirst()) {
            ModelCache<TModel, ?> modelCache = getModelAdapter().getModelCache();
            Object[] values = getModelAdapter().getCachingColumnValuesFromCursor(
                    new Object[getModelAdapter().getCachingColumns().length], cursor);
            TModel model = modelCache.get(getModelAdapter().getCachingId(values));
            if (model == null) {
                if (data == null) {
                    model = getModelAdapter().newInstance();
                } else {
                    model = data;
                }
                getModelAdapter().loadFromCursor(cursor, model);
                modelCache.addModel(getModelAdapter().getCachingId(values), model);
            } else {
                getModelAdapter().reloadRelationships(model, cursor);
            }
            return model;
        } else {
            return null;
        }
    }
}
