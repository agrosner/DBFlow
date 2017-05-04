package com.raizlabs.android.dbflow.config;

import android.content.Context;

import com.raizlabs.android.dbflow.StringUtils;
import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager;
import com.raizlabs.android.dbflow.runtime.ContentResolverNotifier;
import com.raizlabs.android.dbflow.runtime.ModelNotifier;
import com.raizlabs.android.dbflow.sql.migration.Migration;
import com.raizlabs.android.dbflow.structure.BaseModelView;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.ModelViewAdapter;
import com.raizlabs.android.dbflow.structure.QueryModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.FlowSQLiteOpenHelper;
import com.raizlabs.android.dbflow.structure.database.OpenHelper;
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionManager;
import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionQueue;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: The main interface that all Database implementations extend from. This is for internal usage only
 * as it will be generated for every {@link Database}.
 */
public abstract class DatabaseDefinition {

    private final Map<Integer, List<Migration>> migrationMap = new HashMap<>();

    private final Map<Class<?>, ModelAdapter> modelAdapters = new HashMap<>();

    private final Map<String, Class<?>> modelTableNames = new HashMap<>();

    private final Map<Class<?>, ModelViewAdapter> modelViewAdapterMap = new LinkedHashMap<>();

    private final Map<Class<?>, QueryModelAdapter> queryModelAdapterMap = new LinkedHashMap<>();

    /**
     * The helper that manages database changes and initialization
     */
    private OpenHelper openHelper;

    /**
     * Allows for the app to listen for database changes.
     */
    private DatabaseHelperListener helperListener;

    /**
     * Used when resetting the DB
     */
    private boolean isResetting = false;

    private BaseTransactionManager transactionManager;

    private DatabaseConfig databaseConfig;

    private ModelNotifier modelNotifier;

    @SuppressWarnings("unchecked")
    public DatabaseDefinition() {
        databaseConfig = FlowManager.getConfig()
            .databaseConfigMap().get(getAssociatedDatabaseClassFile());


        if (databaseConfig != null) {
            // initialize configuration if exists.
            Collection<TableConfig> tableConfigCollection = databaseConfig.tableConfigMap().values();
            for (TableConfig tableConfig : tableConfigCollection) {
                ModelAdapter modelAdapter = modelAdapters.get(tableConfig.tableClass());
                if (modelAdapter == null) {
                    continue;
                }
                if (tableConfig.listModelLoader() != null) {
                    modelAdapter.setListModelLoader(tableConfig.listModelLoader());
                }

                if (tableConfig.singleModelLoader() != null) {
                    modelAdapter.setSingleModelLoader(tableConfig.singleModelLoader());
                }

                if (tableConfig.modelSaver() != null) {
                    modelAdapter.setModelSaver(tableConfig.modelSaver());
                }

            }
            helperListener = databaseConfig.helperListener();
        }
        if (databaseConfig == null || databaseConfig.transactionManagerCreator() == null) {
            transactionManager = new DefaultTransactionManager(this);
        } else {
            transactionManager = databaseConfig.transactionManagerCreator().createManager(this);
        }
    }

    protected <T> void addModelAdapter(ModelAdapter<T> modelAdapter, DatabaseHolder holder) {
        holder.putDatabaseForTable(modelAdapter.getModelClass(), this);
        modelTableNames.put(modelAdapter.getTableName(), modelAdapter.getModelClass());
        modelAdapters.put(modelAdapter.getModelClass(), modelAdapter);
    }

    protected <T> void addModelViewAdapter(ModelViewAdapter<T> modelViewAdapter, DatabaseHolder holder) {
        holder.putDatabaseForTable(modelViewAdapter.getModelClass(), this);
        modelViewAdapterMap.put(modelViewAdapter.getModelClass(), modelViewAdapter);
    }

    protected <T> void addQueryModelAdapter(QueryModelAdapter<T> queryModelAdapter, DatabaseHolder holder) {
        holder.putDatabaseForTable(queryModelAdapter.getModelClass(), this);
        queryModelAdapterMap.put(queryModelAdapter.getModelClass(), queryModelAdapter);
    }

    protected void addMigration(int version, Migration migration) {
        List<Migration> list = migrationMap.get(version);
        if (list == null) {
            list = new ArrayList<>();
            migrationMap.put(version, list);
        }
        list.add(migration);
    }

    /**
     * @return a list of all model classes in this database.
     */
    public List<Class<?>> getModelClasses() {
        return new ArrayList<>(modelAdapters.keySet());
    }

    public BaseTransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Internal method used to create the database schema.
     *
     * @return List of Model Adapters
     */
    public List<ModelAdapter> getModelAdapters() {
        return new ArrayList<>(modelAdapters.values());
    }

    /**
     * Returns the associated {@link ModelAdapter} within this database for
     * the specified table. If the Model is missing the {@link Table} annotation,
     * this will return null.
     *
     * @param table The model that exists in this database.
     * @return The ModelAdapter for the table.
     */
    @SuppressWarnings("unchecked")
    public <T> ModelAdapter<T> getModelAdapterForTable(Class<T> table) {
        return modelAdapters.get(table);
    }

    /**
     * @param tableName The name of the table in this db.
     * @return The associated {@link ModelAdapter} within this database for the specified table name.
     * If the Model is missing the {@link Table} annotation, this will return null.
     */
    public Class<?> getModelClassForName(String tableName) {
        return modelTableNames.get(tableName);
    }

    /**
     * @return the {@link BaseModelView} list for this database.
     */
    public List<Class<?>> getModelViews() {
        return new ArrayList<>(modelViewAdapterMap.keySet());
    }

    /**
     * @param table the VIEW class to retrieve the ModelViewAdapter from.
     * @return the associated {@link ModelViewAdapter} for the specified table.
     */
    @SuppressWarnings("unchecked")
    public <T> ModelViewAdapter<T> getModelViewAdapterForTable(Class<T> table) {
        return modelViewAdapterMap.get(table);
    }

    /**
     * @return The list of {@link ModelViewAdapter}. Internal method for
     * creating model views in the DB.
     */
    public List<ModelViewAdapter> getModelViewAdapters() {
        return new ArrayList<>(modelViewAdapterMap.values());
    }

    /**
     * @return The list of {@link QueryModelAdapter}. Internal method for creating query models in the DB.
     */
    public List<QueryModelAdapter> getModelQueryAdapters() {
        return new ArrayList<>(queryModelAdapterMap.values());
    }

    /**
     * @param queryModel The {@link QueryModel} class
     * @return The adapter that corresponds to the specified class.
     */
    @SuppressWarnings("unchecked")
    public <T> QueryModelAdapter<T> getQueryModelAdapterForQueryClass(Class<T> queryModel) {
        return queryModelAdapterMap.get(queryModel);
    }

    /**
     * @return The map of migrations to DB version
     */
    public Map<Integer, List<Migration>> getMigrations() {
        return migrationMap;
    }

    public synchronized OpenHelper getHelper() {
        if (openHelper == null) {
            DatabaseConfig config = FlowManager.getConfig().databaseConfigMap()
                .get(getAssociatedDatabaseClassFile());
            if (config == null || config.helperCreator() == null) {
                openHelper = new FlowSQLiteOpenHelper(this, helperListener);
            } else {
                openHelper = config.helperCreator().createHelper(this, helperListener);
            }
            openHelper.performRestoreFromBackup();
        }
        return openHelper;
    }

    public DatabaseWrapper getWritableDatabase() {
        return getHelper().getDatabase();
    }

    public ModelNotifier getModelNotifier() {
        if (modelNotifier == null) {
            DatabaseConfig config = FlowManager.getConfig().databaseConfigMap()
                .get(getAssociatedDatabaseClassFile());
            if (config == null || config.modelNotifier() == null) {
                modelNotifier = new ContentResolverNotifier();
            } else {
                modelNotifier = config.modelNotifier();
            }
        }
        return modelNotifier;
    }

    public Transaction.Builder beginTransactionAsync(ITransaction transaction) {
        return new Transaction.Builder(transaction, this);
    }

    public void executeTransaction(ITransaction transaction) {
        DatabaseWrapper database = getWritableDatabase();
        try {
            database.beginTransaction();
            transaction.execute(database);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    /**
     * @return The name of this database as defined in {@link Database}
     */
    public abstract String getDatabaseName();

    /**
     * @return The file name that this database points to
     */
    public String getDatabaseFileName() {
        return getDatabaseName() + (StringUtils.isNotNullOrEmpty(getDatabaseExtensionName()) ?
            "." + getDatabaseExtensionName() : "");
    }

    /**
     * @return the extension for the file name.
     */
    public String getDatabaseExtensionName() {
        return "db";
    }

    /**
     * @return True if the database will reside in memory.
     */
    public abstract boolean isInMemory();

    /**
     * @return The version of the database currently.
     */
    public abstract int getDatabaseVersion();

    /**
     * @return True if the {@link Database#consistencyCheckEnabled()} annotation is true.
     */
    public abstract boolean areConsistencyChecksEnabled();

    /**
     * @return True if the {@link Database#foreignKeyConstraintsEnforced()} annotation is true.
     */
    public abstract boolean isForeignKeysSupported();

    /**
     * @return True if the {@link Database#backupEnabled()} annotation is true.
     */
    public abstract boolean backupEnabled();

    /**
     * @return The class that defines the {@link Database} annotation.
     */
    public abstract Class<?> getAssociatedDatabaseClassFile();

    /**
     * Performs a full deletion of this database. Reopens the {@link FlowSQLiteOpenHelper} as well.
     *
     * @param context Where the database resides
     */
    public void reset(Context context) {
        if (!isResetting) {
            isResetting = true;
            getTransactionManager().stopQueue();
            getHelper().closeDB();
            for (ModelAdapter modelAdapter : modelAdapters.values()) {
                modelAdapter.closeInsertStatement();
                modelAdapter.closeCompiledStatement();
            }
            context.deleteDatabase(getDatabaseFileName());

            // recreate queue after interrupting it.
            if (databaseConfig == null || databaseConfig.transactionManagerCreator() == null) {
                transactionManager = new DefaultTransactionManager(this);
            } else {
                transactionManager = databaseConfig.transactionManagerCreator().createManager(this);
            }
            openHelper = null;
            isResetting = false;
            getHelper().getDatabase();
        }
    }

    public void destroy(Context context) {
        if (!isResetting) {
            isResetting = true;
            getTransactionManager().stopQueue();
            getHelper().closeDB();
            context.deleteDatabase(getDatabaseFileName());

            openHelper = null;
            isResetting = false;
        }
    }

    public void close() {
        getTransactionManager().stopQueue();
        getHelper().closeDB();

        openHelper = null;
        isResetting = false;
    }

    /**
     * @return True if the database is ok. If backups are enabled, we restore from backup and will
     * override the return value if it replaces the main DB.
     */
    public boolean isDatabaseIntegrityOk() {
        return getHelper().isDatabaseIntegrityOk();
    }

    /**
     * Saves the database as a backup on the {@link DefaultTransactionQueue}. This will
     * create a THIRD database to use as a backup to the backup in case somehow the overwrite fails.
     *
     * @throws java.lang.IllegalStateException if {@link Database#backupEnabled()}
     *                                         or {@link Database#consistencyCheckEnabled()} is not enabled.
     */
    public void backupDatabase() {
        getHelper().backupDB();
    }

}
