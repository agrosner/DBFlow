package com.raizlabs.android.dbflow.sql.saveable;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.Collection;

/**
 * Description: Used for model caching, enables caching models when saving in list.
 */
public class CacheableListModelSaver<TModel>
    extends ListModelSaver<TModel> {

    public CacheableListModelSaver(ModelSaver<TModel> modelSaver) {
        super(modelSaver);
    }

    @Override
    public synchronized void saveAll(@NonNull Collection<TModel> tableCollection,
                                     DatabaseWrapper wrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return;
        }

        ModelSaver<TModel> modelSaver = getModelSaver();
        ModelAdapter<TModel> modelAdapter = modelSaver.getModelAdapter();
        DatabaseStatement statement = modelAdapter.getInsertStatement(wrapper);
        ContentValues contentValues = new ContentValues();
        try {
            for (TModel model : tableCollection) {
                if (modelSaver.save(model, wrapper, statement, contentValues)) {
                    modelAdapter.storeModelInCache(model);
                }
            }
        } finally {
            statement.close();
        }
    }

    @Override
    public synchronized void insertAll(@NonNull Collection<TModel> tableCollection,
                                       DatabaseWrapper wrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return;
        }

        ModelSaver<TModel> modelSaver = getModelSaver();
        ModelAdapter<TModel> modelAdapter = modelSaver.getModelAdapter();
        DatabaseStatement statement = modelAdapter.getInsertStatement(wrapper);
        try {
            for (TModel model : tableCollection) {
                if (modelSaver.insert(model, statement, wrapper) > 0) {
                    modelAdapter.storeModelInCache(model);
                }
            }
        } finally {
            statement.close();
        }
    }

    @Override
    public synchronized void updateAll(@NonNull Collection<TModel> tableCollection,
                                       DatabaseWrapper wrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return;
        }
        ModelSaver<TModel> modelSaver = getModelSaver();
        ModelAdapter<TModel> modelAdapter = modelSaver.getModelAdapter();
        ContentValues contentValues = new ContentValues();
        for (TModel model : tableCollection) {
            if (modelSaver.update(model, wrapper, contentValues)) {
                modelAdapter.storeModelInCache(model);
            }
        }
    }
}
