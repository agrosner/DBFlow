package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: More optimized version of {@link CacheableModelLoader} which assumes that the {@link Model}
 * only utilizes a single primary key.
 */
public class SingleKeyCacheableModelLoader<TModel extends Model> extends CacheableModelLoader<TModel> {

    public SingleKeyCacheableModelLoader(Class<TModel> modelClass) {
        super(modelClass);
    }

    /**
     * Converts data by loading from cache based on its sequence of caching ids. Will reuse the passed
     * {@link TModel} if it's not found in the cache and non-null.
     *
     * @return A model from cache.
     */
    @Nullable
    @Override
    public TModel convertToData(@NonNull Cursor cursor, @Nullable TModel data, boolean moveToFirst) {
        if (!moveToFirst || cursor.moveToFirst()) {
            Object value = getModelAdapter().getCachingColumnValueFromCursor(cursor);
            TModel model = getModelCache().get(value);
            if (model == null) {
                if (data == null) {
                    model = getModelAdapter().newInstance();
                } else {
                    model = data;
                }
                getModelAdapter().loadFromCursor(cursor, model);
                getModelCache().addModel(value, model);
            } else {
                getModelAdapter().reloadRelationships(model, cursor);
            }
            return model;
        } else {
            return null;
        }
    }
}
