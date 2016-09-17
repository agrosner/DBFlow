package com.raizlabs.android.dbflow.sql.saveable;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.Collection;

/**
 * Description:
 */
public class ListModelSaver<TModel> {


    private final ModelSaver<TModel> modelSaver;

    public ListModelSaver(ModelSaver<TModel> modelSaver) {
        this.modelSaver = modelSaver;
    }

    public synchronized void saveAll(@NonNull Collection<TModel> tableCollection) {
        saveAll(tableCollection, modelSaver.getWritableDatabase());
    }

    public synchronized void saveAll(@NonNull Collection<TModel> tableCollection, DatabaseWrapper wrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return;
        }

        DatabaseStatement statement = modelSaver.getModelAdapter().getInsertStatement(wrapper);
        ContentValues contentValues = new ContentValues();
        try {
            for (TModel model : tableCollection) {
                modelSaver.save(model, wrapper, statement, contentValues);
            }
        } finally {
            statement.close();
        }
    }

    public synchronized void insertAll(@NonNull Collection<TModel> tableCollection) {
        insertAll(tableCollection, modelSaver.getWritableDatabase());
    }

    public synchronized void insertAll(@NonNull Collection<TModel> tableCollection, DatabaseWrapper wrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return;
        }

        DatabaseStatement statement = modelSaver.getModelAdapter().getInsertStatement(wrapper);
        try {
            for (TModel model : tableCollection) {
                modelSaver.insert(model, statement);
            }
        } finally {
            statement.close();
        }
    }

    public synchronized void updateAll(@NonNull Collection<TModel> tableCollection) {
        saveAll(tableCollection, modelSaver.getWritableDatabase());
    }

    public synchronized void updateAll(@NonNull Collection<TModel> tableCollection,
                                       DatabaseWrapper wrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        for (TModel model : tableCollection) {
            modelSaver.update(model, wrapper, contentValues);
        }
    }

    public ModelSaver<TModel> getModelSaver() {
        return modelSaver;
    }
}
