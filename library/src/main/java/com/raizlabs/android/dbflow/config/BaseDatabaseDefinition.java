package com.raizlabs.android.dbflow.config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.raizlabs.android.dbflow.DatabaseHelperListener;
import com.raizlabs.android.dbflow.sql.migration.Migration;
import com.raizlabs.android.dbflow.structure.BaseModelView;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.ModelViewAdapter;
import com.raizlabs.android.dbflow.structure.container.ContainerAdapter;

import java.util.List;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Description: The main interface that all Flow Managers implement. This is for internal usage only
 * as it will be generated for every {@link com.raizlabs.android.dbflow.annotation.Database}.
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
     * @return a list of all model classes in this database.
     */
    abstract List<Class<? extends Model>> getModelClasses();

    /**
     * Internal method used to create the database schema.
     *
     * @return List of Model Adapters
     */
    abstract List<ModelAdapter> getModelAdapters();

    /**
     * Returns the associated {@link com.raizlabs.android.dbflow.structure.ModelAdapter} within this database for
     * the specified table. If the Model is missing the {@link com.raizlabs.android.dbflow.annotation.Table} annotation,
     * this will fail.
     *
     * @param table The model that exists in this database.
     * @return The ModelAdapter for the table.
     */
    abstract ModelAdapter getModelAdapterForTable(Class<? extends Model> table);

    /**
     * @param table The table that has a {@link com.raizlabs.android.dbflow.annotation.ContainerAdapter} annotation.
     * @return the associated {@link com.raizlabs.android.dbflow.structure.container.ContainerAdapter} within this
     * database for the specified table. These are used for {@link com.raizlabs.android.dbflow.structure.container.ModelContainer}
     * and require {@link com.raizlabs.android.dbflow.structure.Model} to add the {@link com.raizlabs.android.dbflow.annotation.ContainerAdapter}.
     */
    public abstract ContainerAdapter getModelContainerAdapterForTable(Class<? extends Model> table);

    /**
     * @return the {@link com.raizlabs.android.dbflow.structure.BaseModelView} list for this database.
     */
    abstract List<Class<? extends BaseModelView>> getModelViews();

    /**
     * @param table the VIEW class to retrieve the ModelViewAdapter from.
     * @return the associated {@link com.raizlabs.android.dbflow.structure.ModelViewAdapter} for the specified table.
     */
    abstract ModelViewAdapter getModelViewAdapterForTable(Class<? extends BaseModelView> table);

    /**
     * @return The list of {@link com.raizlabs.android.dbflow.structure.ModelViewAdapter}. Internal method for
     * creating model views in the DB.
     */
    abstract List<ModelViewAdapter> getModelViewAdapters();

    /**
     * @return The map of migrations to DB version
     */
    abstract Map<Integer, List<Migration>> getMigrations();

    FlowSQLiteOpenHelper getHelper() {
        if (mHelper == null) {
            mHelper = new FlowSQLiteOpenHelper(this, mInternalHelperListener);
        }
        return mHelper;
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
