package com.raizlabs.android.dbflow.config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.raizlabs.android.dbflow.DatabaseHelperListener;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.sql.migration.Migration;
import com.raizlabs.android.dbflow.structure.BaseModelView;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.ModelViewAdapter;
import com.raizlabs.android.dbflow.structure.container.ModelContainerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Description: The main interface that all Flow Managers implement. This is for internal usage only
 * as it will be generated for every {@link com.raizlabs.android.dbflow.annotation.Database}.
 */
public abstract class BaseDatabaseDefinition {

    final Map<Integer, List<Migration>> migrationMap = new HashMap<>();

    final List<Class<? extends Model>> models = new ArrayList<>();

    final Map<Class<? extends Model>, ModelAdapter> modelAdapters = new HashMap<>();

    final Map<String, Class<? extends Model>> modelTableNames = new HashMap<>();

    final Map<Class<? extends Model>, ModelContainerAdapter> modelContainerAdapters = new HashMap<>();

    final List<Class<? extends BaseModelView>> modelViews = new ArrayList<>();

    final Map<Class<? extends BaseModelView>, ModelViewAdapter> modelViewAdapterMap = new HashMap<>();

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
     * @return a list of all model classes in this database.
     */
    List<Class<? extends Model>> getModelClasses() {
        return models;
    }

    /**
     * Internal method used to create the database schema.
     *
     * @return List of Model Adapters
     */
    List<ModelAdapter> getModelAdapters() {
        return new ArrayList<>(modelAdapters.values());
    }

    /**
     * Returns the associated {@link com.raizlabs.android.dbflow.structure.ModelAdapter} within this database for
     * the specified table. If the Model is missing the {@link com.raizlabs.android.dbflow.annotation.Table} annotation,
     * this will return null.
     *
     * @param table The model that exists in this database.
     * @return The ModelAdapter for the table.
     */
    ModelAdapter getModelAdapterForTable(Class<? extends Model> table) {
        return modelAdapters.get(table);
    }

    /**
     * @param tableName The name of the table in this db.
     * @return The associated {@link com.raizlabs.android.dbflow.structure.ModelAdapter} within this database for the specified table name.
     * If the Model is missing the {@link com.raizlabs.android.dbflow.annotation.Table} annotation, this will return null.
     */
    public Class<? extends Model> getModelClassForName(String tableName) {
        return modelTableNames.get(tableName);
    }

    /**
     * @param table The table that has a {@link ModelContainer} annotation.
     * @return the associated {@link ModelContainerAdapter} within this
     * database for the specified table. These are used for {@link com.raizlabs.android.dbflow.structure.container.ModelContainer}
     * and require {@link com.raizlabs.android.dbflow.structure.Model} to add the {@link ModelContainer}.
     */
    public ModelContainerAdapter getModelContainerAdapterForTable(Class<? extends Model> table) {
        return modelContainerAdapters.get(table);
    }

    /**
     * @return the {@link com.raizlabs.android.dbflow.structure.BaseModelView} list for this database.
     */
    List<Class<? extends BaseModelView>> getModelViews() {
        return modelViews;
    }

    /**
     * @param table the VIEW class to retrieve the ModelViewAdapter from.
     * @return the associated {@link com.raizlabs.android.dbflow.structure.ModelViewAdapter} for the specified table.
     */
    ModelViewAdapter getModelViewAdapterForTable(Class<? extends BaseModelView> table) {
        return modelViewAdapterMap.get(table);
    }

    /**
     * @return The list of {@link com.raizlabs.android.dbflow.structure.ModelViewAdapter}. Internal method for
     * creating model views in the DB.
     */
    List<ModelViewAdapter> getModelViewAdapters() {
        return new ArrayList<>(modelViewAdapterMap.values());
    }

    /**
     * @return The map of migrations to DB version
     */
    Map<Integer, List<Migration>> getMigrations() {
        return migrationMap;
    }

    FlowSQLiteOpenHelper getHelper() {
        if (mHelper == null) {
            mHelper = createHelper();
        }
        return mHelper;
    }

    protected FlowSQLiteOpenHelper createHelper() {
        return new FlowSQLiteOpenHelper(this, mInternalHelperListener);
    }

    public SQLiteDatabase getWritableDatabase() {
        return getHelper().getWritableDatabase();
    }

    /**
     * Register to listen for database changes
     *
     * @param databaseHelperListener Listens for DB changes
     */
    public void setHelperListener(DatabaseHelperListener databaseHelperListener) {
        mHelperListener = databaseHelperListener;
    }

    /**
     * @return The name of this database as defined in {@link com.raizlabs.android.dbflow.annotation.Database}
     */
    public abstract String getDatabaseName();

    /**
     * @return The file name that this database points to
     */
    public String getDatabaseFileName() {
        return getDatabaseName() + ".db";
    }

    /**
     * @return The version of the database currently.
     */
    public abstract int getDatabaseVersion();

    /**
     * @return True if the {@link com.raizlabs.android.dbflow.annotation.Database#consistencyCheckEnabled()} annotation is true.
     */
    public abstract boolean areConsistencyChecksEnabled();

    /**
     * @return True if the {@link com.raizlabs.android.dbflow.annotation.Database#foreignKeysSupported()} annotation is true.
     */
    public abstract boolean isForeignKeysSupported();

    /**
     * @return True if the {@link com.raizlabs.android.dbflow.annotation.Database#backupEnabled()} annotation is true.
     */
    public abstract boolean backupEnabled();

    /**
     * Performs a full deletion of this database. Reopens the {@link com.raizlabs.android.dbflow.config.FlowSQLiteOpenHelper} as well.
     *
     * @param context Where the database resides
     */
    public void reset(Context context) {
        if (!isResetting) {
            isResetting = true;
            context.deleteDatabase(getDatabaseFileName());
            mHelper = new FlowSQLiteOpenHelper(this, mInternalHelperListener);
            isResetting = false;
        }
    }

    /**
     * @return True if the database is ok. If backups are enabled, we restore from backup and will
     * override the return value if it replaces the main DB.
     */
    public boolean isDatabaseIntegrityOk() {
        return getHelper().isDatabaseIntegrityOk();
    }

    /**
     * Saves the database as a backup on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}. This will
     * create a THIRD database to use as a backup to the backup in case somehow the overwrite fails.
     *
     * @throws java.lang.IllegalStateException if {@link com.raizlabs.android.dbflow.annotation.Database#backupEnabled()}
     *                                         or {@link com.raizlabs.android.dbflow.annotation.Database#consistencyCheckEnabled()} is not enabled.
     */
    public void backupDatabase() {
        getHelper().backupDB();
    }

    protected final DatabaseHelperListener mInternalHelperListener = new DatabaseHelperListener() {
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
