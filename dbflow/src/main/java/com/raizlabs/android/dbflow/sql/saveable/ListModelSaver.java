package com.raizlabs.android.dbflow.sql.saveable;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.Collection;

public class ListModelSaver<TModel> {

    private final ModelSaver<TModel> modelSaver;

    public ListModelSaver(ModelSaver<TModel> modelSaver) {
        this.modelSaver = modelSaver;
    }

    public synchronized void saveAll(@NonNull Collection<TModel> tableCollection) {
        saveAll(tableCollection, modelSaver.getWritableDatabase());
    }

    public synchronized void saveAll(@NonNull Collection<TModel> tableCollection,
                                     @NonNull DatabaseWrapper wrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return;
        }

        DatabaseStatement statement = modelSaver.getModelAdapter().getInsertStatement(wrapper);
        DatabaseStatement updateStatement = modelSaver.getModelAdapter().getUpdateStatement(wrapper);
        try {
            for (TModel model : tableCollection) {
                modelSaver.save(model, wrapper, statement, updateStatement);
            }
        } finally {
            statement.close();
        }
    }

    public synchronized void insertAll(@NonNull Collection<TModel> tableCollection) {
        insertAll(tableCollection, modelSaver.getWritableDatabase());
    }

    public synchronized void insertAll(@NonNull Collection<TModel> tableCollection,
                                       @NonNull DatabaseWrapper wrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return;
        }

        DatabaseStatement statement = modelSaver.getModelAdapter().getInsertStatement(wrapper);
        try {
            for (TModel model : tableCollection) {
                modelSaver.insert(model, statement, wrapper);
            }
        } finally {
            statement.close();
        }
    }

    public synchronized void updateAll(@NonNull Collection<TModel> tableCollection) {
        updateAll(tableCollection, modelSaver.getWritableDatabase());
    }

    public synchronized void updateAll(@NonNull Collection<TModel> tableCollection,
                                       @NonNull DatabaseWrapper wrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return;
        }

        DatabaseStatement updateStatement = modelSaver.getModelAdapter().getUpdateStatement(wrapper);
        try {
            for (TModel model : tableCollection) {
                modelSaver.update(model, wrapper, updateStatement);
            }
        } finally {
            updateStatement.close();
        }
    }

    public synchronized void deleteAll(@NonNull Collection<TModel> tableCollection) {
        deleteAll(tableCollection, modelSaver.getWritableDatabase());
    }

    public synchronized void deleteAll(@NonNull Collection<TModel> tableCollection,
                                       @NonNull DatabaseWrapper wrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return;
        }

        DatabaseStatement deleteStatement = modelSaver.getModelAdapter().getDeleteStatement(wrapper);
        try {
            for (TModel model : tableCollection) {
                modelSaver.delete(model, deleteStatement, wrapper);
            }
        } finally {
            deleteStatement.close();
        }
    }

    @NonNull
    public ModelSaver<TModel> getModelSaver() {
        return modelSaver;
    }
}
