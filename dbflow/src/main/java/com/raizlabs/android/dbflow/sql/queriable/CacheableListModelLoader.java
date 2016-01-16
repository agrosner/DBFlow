package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.cache.ModelCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Loads a {@link List} of {@link TModel} with {@link Table#cachingEnabled()} true.
 */
public class CacheableListModelLoader<TModel extends Model> extends ListModelLoader<TModel> {

    private ModelAdapter<TModel> modelAdapter;
    private ModelCache<TModel, ?> modelCache;

    public CacheableListModelLoader(Class<TModel> modelClass) {
        super(modelClass);

        if (!(getInstanceAdapter() instanceof ModelAdapter)) {
            throw new IllegalArgumentException("A non-Table type was used.");
        }
        //noinspection unchecked
        modelAdapter = (ModelAdapter<TModel>) getInstanceAdapter();
        modelCache = modelAdapter.getModelCache();

        if (!modelAdapter.cachingEnabled()) {
            throw new IllegalArgumentException("You cannot call this method for a table that has no caching id. Either" +
                    "use one Primary Key or call convertToList()");
        } else if (modelCache == null) {
            throw new IllegalArgumentException("ModelCache specified in convertToCacheableList() must not be null.");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<TModel> convertToData(@NonNull Cursor cursor) {
        final List<TModel> modelList = new ArrayList<>();
        Object[] cacheValues = new Object[modelAdapter.getCachingColumns().length];
        // Ensure that we aren't iterating over this cursor concurrently from different threads
        if (cursor.moveToFirst()) {
            do {
                Object[] values = modelAdapter.getCachingColumnValuesFromCursor(cacheValues, cursor);
                TModel model = modelCache.get(modelAdapter.getCachingId(values));
                if (model != null) {
                    modelAdapter.reloadRelationships(model, cursor);
                    modelList.add(model);
                } else {
                    model = modelAdapter.newInstance();
                    modelAdapter.loadFromCursor(cursor, model);
                    modelList.add(model);
                }
            } while (cursor.moveToNext());
        }
        return modelList;
    }

}
