package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
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
    private final BaseDatabaseDefinition databaseDefinition;
    private InstanceAdapter instanceAdapter;

    public ModelLoader(Class<TModel> modelClass) {
        this.modelClass = modelClass;
        databaseDefinition = FlowManager.getDatabaseForTable(modelClass);
        instanceAdapter = FlowManager.getInstanceAdapter(modelClass);
    }

    /**
     * Loads the data from a query and returns it as a {@link TReturn}.
     *
     * @param query         The query to call.
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values
     *                      from selectionArgs, in order that they appear in the selection. The
     *                      values will be bound as Strings.
     * @return The data loaded from the database.
     */
    public TReturn load(String query, String... selectionArgs) {
        return load(databaseDefinition.getWritableDatabase(), query, selectionArgs);
    }

    public TReturn load(String query, @Nullable TReturn data, String... selectionArgs) {
        return load(databaseDefinition.getWritableDatabase(), query, data, selectionArgs);
    }

    /**
     * Loads the data from a query and returns it as a {@link TReturn}.
     *
     * @param databaseWrapper A custom database wrapper object to use.
     * @param query           The query to call.
     * @param selectionArgs   You may include ?s in selection, which will be replaced by the values
     *                        from selectionArgs, in order that they appear in the selection. The
     *                        values will be bound as Strings.
     * @return The data loaded from the database.
     */
    @Nullable
    public TReturn load(@NonNull DatabaseWrapper databaseWrapper, String query, String... selectionArgs) {
        return load(databaseWrapper, query, null, selectionArgs);
    }

    @Nullable
    public TReturn load(@NonNull DatabaseWrapper databaseWrapper, String query, @Nullable TReturn data, String... selectionArgs) {
        final Cursor cursor = databaseWrapper.rawQuery(query, selectionArgs);
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

    public BaseDatabaseDefinition getDatabaseDefinition() {
        return databaseDefinition;
    }

    /**
     * Specify how to convert the {@link Cursor} data into a {@link TReturn}. Can be null.
     *
     * @param cursor The cursor resulting from a query passed into {@link #load(String, String...)}
     * @param data   The data (if not null) that we can reuse without need to create new object.
     * @return A new (or reused) instance that represents the {@link Cursor}.
     */
    @Nullable
    protected abstract TReturn convertToData(@NonNull final Cursor cursor, @Nullable TReturn data);
}
