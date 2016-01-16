package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;
import com.raizlabs.android.dbflow.structure.Model;

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
     * @param query The query to call.
     * @return The data loaded from the database.
     */
    @Nullable
    public TReturn load(String query) {
        return load(query, null);
    }

    @Nullable
    public TReturn load(String query, @Nullable TReturn data) {
        final Cursor cursor = databaseDefinition.getWritableDatabase().rawQuery(query, null);
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

    protected abstract TReturn convertToData(@NonNull final Cursor cursor, @Nullable TReturn data);
}
