package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Represents how models load from DB. It will query a {@link SQLiteDatabase}
 * and query for a {@link Cursor}. Then the cursor is used to convert itself into an object.
 */
public abstract class ModelLoader<TModel extends Model, TReturn> {

    private final Class<TModel> modelClass;
    private final DatabaseDefinition databaseDefinition;
    private InstanceAdapter instanceAdapter;

    public ModelLoader(Class<TModel> modelClass) {
        this.modelClass = modelClass;
        databaseDefinition = FlowManager.getDatabaseForTable(modelClass);
        instanceAdapter = FlowManager.getInstanceAdapter(modelClass);
    }

    /**
     * Loads the data from a query and returns it as a {@link TReturn}.
     *
     * @param query The query to call.
     * @return The data loaded from the database.
     */
    public TReturn load(String query) {
        return load(databaseDefinition.getWritableDatabase(), query);
    }

    public TReturn load(String query, @Nullable TReturn data) {
        return load(databaseDefinition.getWritableDatabase(), query, data);
    }

    /**
     * Loads the data from a query and returns it as a {@link TReturn}.
     *
     * @param databaseWrapper A custom database wrapper object to use.
     * @param query           The query to call.
     * @return The data loaded from the database.
     */
    @Nullable
    public TReturn load(@NonNull DatabaseWrapper databaseWrapper, String query) {
        return load(databaseWrapper, query, null);
    }

    @Nullable
    public TReturn load(@NonNull DatabaseWrapper databaseWrapper, String query, @Nullable TReturn data) {
        final Cursor cursor = databaseWrapper.rawQuery(query, null);
        return load(cursor, data);
    }

    @Nullable
    public TReturn load(@Nullable Cursor cursor) {
        return load(cursor, null);
    }

    @Nullable
    public TReturn load(@Nullable Cursor cursor, @Nullable TReturn data) {
        if (cursor != null) {
            try {
                data = convertToData(cursor, data);
            } finally {
                cursor.close();
            }
        }
        return data;
    }

    public Class<TModel> getModelClass() {
        return modelClass;
    }

    @NonNull
    public InstanceAdapter getInstanceAdapter() {
        return instanceAdapter;
    }

    public DatabaseDefinition getDatabaseDefinition() {
        return databaseDefinition;
    }

    /**
     * Specify how to convert the {@link Cursor} data into a {@link TReturn}. Can be null.
     *
     * @param cursor The cursor resulting from a query passed into {@link #load(String)}
     * @param data   The data (if not null) that we can reuse without need to create new object.
     * @return A new (or reused) instance that represents the {@link Cursor}.
     */
    @Nullable
    public abstract TReturn convertToData(@NonNull final Cursor cursor, @Nullable TReturn data);
}
