package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Loads a {@link List} of {@link TModel} with {@link Table#cachingEnabled()} true.
 */
public class CacheableListModelLoader<TModel> extends ListModelLoader<TModel> {

    private ModelAdapter<TModel> modelAdapter;
    private ModelCache<TModel, ?> modelCache;

    public CacheableListModelLoader(Class<TModel> modelClass) {
        super(modelClass);
    }

    public ModelCache<TModel, ?> getModelCache() {
        if (modelCache == null) {
            modelCache = modelAdapter.getModelCache();
            if (modelCache == null) {
                throw new IllegalArgumentException("ModelCache specified in convertToCacheableList() must not be null.");
            }
        }
        return modelCache;
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
                        "use one Primary Key or use the MultiCacheKeyConverter");
            }
        }
        return modelAdapter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TModel> convertToData(@NonNull Cursor cursor, @Nullable List<TModel> data) {
        if (data == null) {
            data = new ArrayList<>();
        }
        Object[] cacheValues = new Object[getModelAdapter().getCachingColumns().length];
        // Ensure that we aren't iterating over this cursor concurrently from different threads
        if (cursor.moveToFirst()) {
            do {
                Object[] values = getModelAdapter().getCachingColumnValuesFromCursor(cacheValues, cursor);
                TModel model = getModelCache().get(getModelAdapter().getCachingId(values));
                if (model != null) {
                    getModelAdapter().reloadRelationships(model, cursor);
                    data.add(model);
                } else {
                    model = getModelAdapter().newInstance();
                    getModelAdapter().loadFromCursor(cursor, model);
                    getModelCache().addModel(getModelAdapter().getCachingId(values), model);
                    data.add(model);
                }
            } while (cursor.moveToNext());
        }
        return data;
    }

}
