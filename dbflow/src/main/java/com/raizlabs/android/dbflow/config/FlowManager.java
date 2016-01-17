package com.raizlabs.android.dbflow.config;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.DatabaseHelperListener;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.sql.migration.Migration;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.BaseModelView;
import com.raizlabs.android.dbflow.structure.BaseQueryModel;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;
import com.raizlabs.android.dbflow.structure.InvalidDBConfiguration;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.ModelViewAdapter;
import com.raizlabs.android.dbflow.structure.QueryModelAdapter;
import com.raizlabs.android.dbflow.structure.container.ModelContainerAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperDelegate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Description: The main entry point into the generated database code. It uses reflection to look up
 * and construct the generated database holder class used in defining the structure for all databases
 * used in this application.
 */
public class FlowManager {


    private static class GlobalDatabaseHolder extends DatabaseHolder {
        public void add(DatabaseHolder holder) {
            managerMap.putAll(holder.managerMap);
            managerNameMap.putAll(holder.managerNameMap);
            typeConverters.putAll(holder.typeConverters);
        }
    }

    private static Context context;

    private static GlobalDatabaseHolder globalDatabaseHolder = new GlobalDatabaseHolder();

    private static HashSet<Class<? extends DatabaseHolder>> loadedModules = new HashSet<>();


    private static final String DEFAULT_DATABASE_HOLDER_NAME = "GeneratedDatabaseHolder";

    private static final String DEFAULT_DATABASE_HOLDER_PACKAGE_NAME =
        FlowManager.class.getPackage().getName();

    private static final String DEFAULT_DATABASE_HOLDER_CLASSNAME =
        DEFAULT_DATABASE_HOLDER_PACKAGE_NAME + "." + DEFAULT_DATABASE_HOLDER_NAME;

    /**
     * Returns the table name for the specific model class
     *
     * @param table The class that implements {@link Model}
     * @return The table name, which can be different than the {@link Model} class name
     */
    @SuppressWarnings("unchecked")

    public static String getTableName(Class<? extends Model> table) {
        ModelAdapter modelAdapter = getModelAdapter(table);
        String tableName = null;
        if (modelAdapter == null) {
            ModelViewAdapter modelViewAdapter = getDatabaseForTable(table).getModelViewAdapterForTable(
                (Class<? extends BaseModelView>) table);
            if (modelViewAdapter != null) {
                tableName = modelViewAdapter.getViewName();
            }
        } else {
            tableName = modelAdapter.getTableName();
        }
        return tableName;
    }

    /**
     * @param databaseName The name of the database. Will throw an exception if the database doesn't exist.
     * @param tableName    The name of the table in the DB.
     * @return The associated table class for the specified name.
     */
    public static Class<? extends Model> getTableClassForName(String databaseName, String tableName) {
        BaseDatabaseDefinition databaseDefinition = getDatabase(databaseName);
        if (databaseDefinition == null) {
            throw new IllegalArgumentException(String.format("The specified database %1s was not found. " +
                "Did you forget to add the @Database?", databaseName));
        }
        Class<? extends Model> modelClass = databaseDefinition.getModelClassForName(tableName);
        if (modelClass == null) {
            throw new IllegalArgumentException(String.format("The specified table %1s was not found. " +
                    "Did you forget to add the @Table annotation and point it to %1s?",
                tableName, databaseName));
        }
        return modelClass;
    }

    /**
     * @param table The table to lookup the database for.
     * @return the corresponding {@link BaseDatabaseDefinition} for the specified model
     */
    public static BaseDatabaseDefinition getDatabaseForTable(Class<? extends Model> table) {
        BaseDatabaseDefinition flowManager = globalDatabaseHolder.getDatabaseForTable(table);
        if (flowManager == null) {
            throw new InvalidDBConfiguration("Table: " + table.getName() + " is not registered with a Database. " +
                "Did you forget the @Table annotation?");
        }
        return flowManager;
    }

    /**
     * @param databaseName The name of the database. Will throw an exception if the database doesn't exist.
     * @return the {@link BaseDatabaseDefinition} for the specified database
     */
    public static BaseDatabaseDefinition getDatabase(String databaseName) {
        BaseDatabaseDefinition database = globalDatabaseHolder.getDatabase(databaseName);

        if (database != null) {
            return database;
        }

        throw new InvalidDBConfiguration("The specified database" + databaseName + " was not found. " +
            "Did you forget the @Database annotation?");
    }

    /**
     * @return The database holder, creating if necessary using reflection.
     */
    protected static void loadDatabaseHolder(Class<? extends DatabaseHolder> holderClass) {
        if (loadedModules.contains(holderClass)) {
            return;
        }

        try {
            // Load the database holder, and add it to the global collection.
            DatabaseHolder dbHolder = holderClass.newInstance();

            if (dbHolder != null) {
                globalDatabaseHolder.add(dbHolder);

                // Cache the holder for future reference.
                loadedModules.add(holderClass);
            }
        } catch (Throwable e) {
            throw new ModuleNotFoundException("Cannot load " + holderClass, e);
        }
    }

    /**
     * Will throw an exception if this class is not initialized yet in {@link #init(Context)}
     *
     * @return The shared context.
     */
    public static Context getContext() {
        if (context == null) {
            throw new IllegalStateException("Context cannot be null for FlowManager");
        }
        return context;
    }

    /**
     * Initializes DBFlow, loading the main application Database holder via reflection one time only.
     * This will trigger all creations, updates, and instantiation for each database defined.
     *
     * @param context The shared context for database usage.
     */
    public static void init(@NonNull Context context) {
        // Initialize the context, then load the default database holder.
        initContext(context);

        try {
            //noinspection unchecked
            Class<? extends DatabaseHolder> defaultHolderClass = (Class<? extends DatabaseHolder>) Class.forName(DEFAULT_DATABASE_HOLDER_CLASSNAME);
            loadDatabaseHolder(defaultHolderClass);
        } catch (ModuleNotFoundException e) {
            // Ignore this exception since it means the application does not have its
            // own database. The initialization happens because the application is using
            // a module that has a database.
            FlowLog.log(FlowLog.Level.W, e.getMessage());
        } catch (ClassNotFoundException e) {
            // warning if a library uses DBFlow with module support but the app you're using doesn't support it.
            FlowLog.log(FlowLog.Level.W, "Could not find the default GeneratedDatabaseHolder");
        }
    }

    /**
     * Loading the module Database holder via reflection. This will trigger all creations,
     * updates, and instantiation for each database defined.
     * <p/>
     * It is assumed FlowManager.init() is called by the application that uses the
     * module database. This method should only be called if you need to load databases
     * that are part of a module. Building once will give you the ability to add the class.
     */
    public static void initModule(Class<? extends DatabaseHolder> generatedClassName) {
        loadDatabaseHolder(generatedClassName);
    }

    private static void initContext(@NonNull Context context) {
        // only initialize Context once.
        if (FlowManager.context == null) {
            FlowManager.context = context.getApplicationContext();
        }
    }

    /**
     * Registers a listener for database creation/update events. Call this before running any query.
     *
     * @param databaseName           The name of the database. Will throw an exception if the database doesn't exist.
     * @param databaseHelperListener Provides callbacks for database events.
     */
    public static void setDatabaseListener(String databaseName, DatabaseHelperListener databaseHelperListener) {
        getDatabase(databaseName).setHelperListener(databaseHelperListener);
    }

    /**
     * @param objectClass A class with an associated type converter. May return null if not found.
     * @return The specific {@link TypeConverter} for the specified class. It defines
     * how the custom datatype is handled going into and out of the DB.
     */
    public static TypeConverter getTypeConverterForClass(Class<?> objectClass) {
        return globalDatabaseHolder.getTypeConverterForClass(objectClass);
    }

    // region Getters

    /**
     * Release reference to context
     */
    public static synchronized void destroy() {
        context = null;

        // Reset the global database holder.
        globalDatabaseHolder = new GlobalDatabaseHolder();
        loadedModules.clear();
    }

    /**
     * @param modelClass The class that implements {@link Model} to find an adapter for.
     * @return The adapter associated with the class. If its not a {@link ModelAdapter},
     * it checks both the {@link ModelViewAdapter} and {@link QueryModelAdapter}.
     */
    @SuppressWarnings("unchecked")
    public static InstanceAdapter getInstanceAdapter(Class<? extends Model> modelClass) {
        InstanceAdapter internalAdapter = getModelAdapter(modelClass);
        if (internalAdapter == null) {
            if (BaseModelView.class.isAssignableFrom(modelClass)) {
                internalAdapter = FlowManager.getModelViewAdapter(
                    (Class<? extends BaseModelView<? extends Model>>) modelClass);
            } else if (BaseQueryModel.class.isAssignableFrom(modelClass)) {
                internalAdapter = FlowManager.getQueryModelAdapter(
                    (Class<? extends BaseQueryModel>) modelClass);
            }
        }

        return internalAdapter;
    }

    /**
     * @param modelClass   The class of the table
     * @param <ModelClass> The class that implements {@link Model}
     * @return The associated model adapter (DAO) that is generated from a {@link Table} class. Handles
     * interactions with the database. This method is meant for internal usage only.
     * We strongly prefer you use the built-in methods associated with {@link Model} and {@link BaseModel}.
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ModelAdapter<ModelClass> getModelAdapter(Class<ModelClass> modelClass) {
        return FlowManager.getDatabaseForTable(modelClass).getModelAdapterForTable(modelClass);
    }

    /**
     * @param modelClass   The class of the table
     * @param <ModelClass> The class that implements {@link Model}
     * @return the container adapter for the specified table. These are only generated when you specify {@link ModelContainer}
     * in your model class so it can be used for containers. These are not generated by default as a means to keep app size down.
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ModelContainerAdapter<ModelClass> getContainerAdapter(
        Class<ModelClass> modelClass) {
        return FlowManager.getDatabaseForTable(modelClass).getModelContainerAdapterForTable(modelClass);
    }

    /**
     * Returns the model view adapter for a SQLite VIEW. These are only created with the {@link com.raizlabs.android.dbflow.annotation.ModelView} annotation.
     *
     * @param modelViewClass   The class of the VIEW
     * @param <ModelViewClass> The class that extends {@link BaseModelView}
     * @return The model view adapter for the specified model view.
     */
    @SuppressWarnings("unchecked")
    public static <ModelViewClass extends BaseModelView<? extends Model>> ModelViewAdapter<? extends Model, ModelViewClass> getModelViewAdapter(
        Class<ModelViewClass> modelViewClass) {
        return FlowManager.getDatabaseForTable(modelViewClass).getModelViewAdapterForTable(modelViewClass);
    }

    /**
     * Returns the query model adapter for an undefined query. These are only created with the {@link QueryModel} annotation.
     *
     * @param queryModel   The class of the query
     * @param <QueryModel> The class that extends {@link BaseQueryModel}
     * @return The query model adapter for the specified model query.
     */
    @SuppressWarnings("unchecked")
    public static <QueryModel extends BaseQueryModel> QueryModelAdapter<QueryModel> getQueryModelAdapter(
        Class<QueryModel> queryModel) {
        return FlowManager.getDatabaseForTable(queryModel).getQueryModelAdapterForQueryClass(queryModel);
    }

    /**
     * @param databaseName The name of the database. Will throw an exception if the database doesn't exist.
     * @return The map of migrations for the specified database.
     */
    static Map<Integer, List<Migration>> getMigrations(String databaseName) {
        return getDatabase(databaseName).getMigrations();
    }

    /**
     * Checks a standard database helper for integrity using quick_check(1).
     *
     * @param databaseName The name of the database to check. Will thrown an exception if it does not exist.
     * @return true if it's integrity is OK.
     */
    public static boolean isDatabaseIntegrityOk(String databaseName) {
        return isDatabaseIntegrityOk(getDatabase(databaseName).getHelper());
    }


    /**
     * Checks a standard database helper for integrity using quick_check(1).
     *
     * @param helperDelegate The helper to user to look up integrity.
     * @return true if it's integrity is OK.
     */
    public static boolean isDatabaseIntegrityOk(DatabaseHelperDelegate helperDelegate) {
        boolean integrityOk = true;

        SQLiteStatement prog = null;
        try {
            prog = helperDelegate.getWritableDatabase().compileStatement("PRAGMA quick_check(1)");
            String rslt = prog.simpleQueryForString();
            if (!rslt.equalsIgnoreCase("ok")) {
                // integrity_checker failed on main or attached databases
                FlowLog.log(FlowLog.Level.E, "PRAGMA integrity_check on temp DB returned: " + rslt);

                integrityOk = false;
            }
        } finally {
            if (prog != null) {
                prog.close();
            }
        }
        return integrityOk;
    }

    // endregion

    /**
     * Exception thrown when a database holder cannot load the database holder
     * for a module.
     */
    public static class ModuleNotFoundException extends RuntimeException {
        public ModuleNotFoundException() {
        }

        public ModuleNotFoundException(String detailMessage) {
            super(detailMessage);
        }

        public ModuleNotFoundException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public ModuleNotFoundException(Throwable throwable) {
            super(throwable);
        }
    }

}