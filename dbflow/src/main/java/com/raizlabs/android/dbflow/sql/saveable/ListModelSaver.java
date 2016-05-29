package com.raizlabs.android.dbflow.sql.saveable;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.InternalAdapter;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.RetrievalAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.Collection;

/**
 * Description:
 */
public class ListModelSaver<TModel extends Model, TTable extends Model,
        TAdapter extends RetrievalAdapter & InternalAdapter> {


    private final ModelSaver<TModel, TTable, TAdapter> modelSaver;

    public ListModelSaver(ModelSaver<TModel, TTable, TAdapter> modelSaver) {
        this.modelSaver = modelSaver;
    }

    public synchronized void saveAll(@NonNull Collection<TTable> tableCollection) {
        saveAll(tableCollection, modelSaver.getWritableDatabase());
    }

    public synchronized void saveAll(@NonNull Collection<TTable> tableCollection, DatabaseWrapper wrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return;
        }

        DatabaseStatement statement = modelSaver.getModelAdapter().getInsertStatement(wrapper);
        ContentValues contentValues = new ContentValues();
        try {
            for (TTable model : tableCollection) {
                modelSaver.save(model, wrapper, statement, contentValues);
            }
        } finally {
            statement.close();
        }
    }

    public synchronized void insertAll(@NonNull Collection<TTable> tableCollection) {
        insertAll(tableCollection, modelSaver.getWritableDatabase());
    }

    public synchronized void insertAll(@NonNull Collection<TTable> tableCollection, DatabaseWrapper wrapper) {
        // skip if empty.
        if (tableCollection.isEmpty()) {
            return;
        }

        DatabaseStatement statement = modelSaver.getModelAdapter().getInsertStatement(wrapper);
        try {
            for (TTable model : tableCollection) {
                modelSaver.insert(model, statement);
            }
        } finally {
            statement.close();
        }
    }

    public synchronized void updateAll(@NonNull Collection<TTable> tableCollection) {
        saveAll(tableCollection, modelSaver.getWritableDatabase());
    }

    public synchronized void updateAll(@NonNull Collection<TTable> tableCollection, DatabaseWrapper wrapper) {
            // skip if empty.
            if (tableCollection.isEmpty()) {
                return;
            }

            ContentValues contentValues = new ContentValues();
            for (TTable model : tableCollection) {
                modelSaver.update(model, wrapper, contentValues);
            }
    }

    public ModelSaver<TModel, TTable, TAdapter> getModelSaver() {
        return modelSaver;
    }
}
