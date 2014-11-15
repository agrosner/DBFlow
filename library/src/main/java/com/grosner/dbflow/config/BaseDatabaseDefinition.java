package com.grosner.dbflow.config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.grosner.dbflow.DatabaseHelperListener;
import com.grosner.dbflow.sql.migration.Migration;
import com.grosner.dbflow.structure.BaseModelView;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.ModelAdapter;
import com.grosner.dbflow.structure.ModelViewAdapter;
import com.grosner.dbflow.structure.container.ContainerAdapter;

import java.util.List;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Description: The main interface that all Flow Managers implement. This is for internal usage only
 * as it will be generated for every {@link com.grosner.dbflow.annotation.Database}.
 */
public abstract class BaseDatabaseDefinition {

    /**
     * The helper that manages database changes and initialization
     */
    private FlowSQLiteOpenHelper mHelper;

    /**
     * Allows for the app to listen for database changes.
     */
    private DatabaseHelperListener mHelperListener;

    /**
     * Used when resetting the DB
     */
    private boolean isResetting = false;

    /**
     * Returns a list of all model classes in this database.
     *
     * @return
     */
    abstract List<Class<? extends Model>> getModelClasses();

    /**
     * Internal method used to create the database schema.
     * @return
     */
    abstract List<ModelAdapter> getModelAdapters();

    /**
     * Returns the associated {@link com.grosner.dbflow.structure.ModelAdapter} within this database for
     * the specified table. If the Model is missing the {@link com.grosner.dbflow.annotation.Table} annotation,
     * this will fail.
     * @param table
     * @return
     */
    abstract ModelAdapter getModelAdapterForTable(Class<? extends Model> table);

    /**
     * Returns the associated {@link com.grosner.dbflow.structure.container.ContainerAdapter} within this
     * database for the specified table. These are used for {@link com.grosner.dbflow.structure.container.ModelContainer}
     * and require {@link com.grosner.dbflow.structure.Model} to add the {@link com.grosner.dbflow.annotation.ContainerAdapter}.
     * @param table
     * @return
     */
    public abstract ContainerAdapter getModelContainerAdapterForTable(Class<? extends Model> table);

    /**
     * @return the {@link com.grosner.dbflow.structure.BaseModelView} list for this database.
     */
    abstract List<Class<? extends BaseModelView>> getModelViews();

    /**
     * @param table
     * @return the associated {@link com.grosner.dbflow.structure.ModelViewAdapter} for the specified table.
     */
    abstract ModelViewAdapter getModelViewAdapterForTable(Class<? extends BaseModelView> table);

    /**
     * @return The list of {@link com.grosner.dbflow.structure.ModelViewAdapter}. Internal method for
     *  creating model views in the DB.
     */
    abstract List<ModelViewAdapter> getModelViewAdapters();

    /**
     *
     * @return
     */
    abstract Map<Integer, List<Migration>> getMigrations();

    public SQLiteDatabase getWritableDatabase() {
        if (mHelper == null) {
            mHelper = new FlowSQLiteOpenHelper(this, mInternalHelperListener);
        }
        return mHelper.getWritableDatabase();
    }

    /**
     * Register to listen for database changes
     * @param databaseHelperListener
     */
    public void setHelperListener(DatabaseHelperListener databaseHelperListener) {
        mHelperListener = databaseHelperListener;
    }

    public abstract String getDatabaseName();

    public abstract int getDatabaseVersion();

    public abstract boolean areConsistencyChecksEnabled();

    public abstract boolean isForeignKeysSupported();

    public void reset(Context context) {
        if (!isResetting) {
            isResetting = true;
            context.deleteDatabase(getDatabaseName());
            isResetting = false;
        }
    }

    private final DatabaseHelperListener mInternalHelperListener = new DatabaseHelperListener() {
        @Override
        public void onOpen(SQLiteDatabase database) {
            if (mHelperListener != null) {
                mHelperListener.onOpen(database);
            }
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            if (mHelperListener != null) {
                mHelperListener.onCreate(database);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            if (mHelperListener != null) {
                mHelperListener.onUpgrade(database, oldVersion, newVersion);
            }
        }
    };
}
