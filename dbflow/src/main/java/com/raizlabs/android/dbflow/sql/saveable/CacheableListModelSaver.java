package com.raizlabs.android.dbflow.sql.saveable;

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

    public CacheableListModelSaver(@NonNull ModelSaver<TModel> modelSaver) {
        super(modelSaver);
    }

    @Override
    public synchronized void saveAll(@NonNull Collection<TModel> tableCollection,
                                     @NonNull DatabaseWrapper wrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return;
        }

        ModelSaver<TModel> modelSaver = getModelSaver();
        ModelAdapter<TModel> modelAdapter = modelSaver.getModelAdapter();
        DatabaseStatement statement = modelAdapter.getInsertStatement(wrapper);
        DatabaseStatement updateStatement = modelAdapter.getUpdateStatement(wrapper);
        try {
            for (TModel model : tableCollection) {
                if (modelSaver.save(model, wrapper, statement, updateStatement)) {
                    modelAdapter.storeModelInCache(model);
                }
            }
        } finally {
            updateStatement.close();
            statement.close();
        }
    }

    @Override
    public synchronized void insertAll(@NonNull Collection<TModel> tableCollection,
                                       @NonNull DatabaseWrapper wrapper) {
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
                                       @NonNull DatabaseWrapper wrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return;
        }
        ModelSaver<TModel> modelSaver = getModelSaver();
        ModelAdapter<TModel> modelAdapter = modelSaver.getModelAdapter();
        DatabaseStatement statement = modelAdapter.getUpdateStatement(wrapper);
        try {
            for (TModel model : tableCollection) {
                if (modelSaver.update(model, wrapper, statement)) {
                    modelAdapter.storeModelInCache(model);
                }
            }
        } finally {
            statement.close();
        }
    }

    @Override
    public synchronized void deleteAll(@NonNull Collection<TModel> tableCollection,
                                       @NonNull DatabaseWrapper wrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return;
        }

        ModelSaver<TModel> modelSaver = getModelSaver();
        for (TModel model : tableCollection) {
            if (modelSaver.delete(model, wrapper)) {
                getModelSaver().getModelAdapter().removeModelFromCache(model);
            }
        }
    }
}
