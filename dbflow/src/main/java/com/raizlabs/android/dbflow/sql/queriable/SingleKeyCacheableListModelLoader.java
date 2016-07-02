package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 */
public class SingleKeyCacheableListModelLoader<TModel extends Model> extends CacheableListModelLoader<TModel> {

    public SingleKeyCacheableListModelLoader(Class<TModel> tModelClass) {
        super(tModelClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TModel> convertToData(@NonNull Cursor cursor, @Nullable List<TModel> data) {
        if (data == null) {
            data = new ArrayList<>();
        }
        Object cacheValue;
        // Ensure that we aren't iterating over this cursor concurrently from different threads
        if (cursor.moveToFirst()) {
            do {
                cacheValue = getModelAdapter().getCachingColumnValueFromCursor(cursor);
                TModel model = getModelCache().get(cacheValue);
                if (model != null) {
                    getModelAdapter().reloadRelationships(model, cursor);
                    data.add(model);
                } else {
                    model = getModelAdapter().newInstance();
                    getModelAdapter().loadFromCursor(cursor, model);
                    getModelCache().addModel(cacheValue, model);
                    data.add(model);
                }
            } while (cursor.moveToNext());
        }
        return data;
    }

}
