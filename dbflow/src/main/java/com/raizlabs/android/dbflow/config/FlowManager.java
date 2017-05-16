package com.raizlabs.android.dbflow.config;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.runtime.ModelNotifier;
import com.raizlabs.android.dbflow.runtime.TableNotifierRegister;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
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
import com.raizlabs.android.dbflow.structure.RetrievalAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description: The main entry point into the generated database code. It uses reflection to look up
 * and construct the generated database holder class used in defining the structure for all databases
 * used in this application.
 */
public class FlowManager {

    private static class GlobalDatabaseHolder extends DatabaseHolder {

        private boolean initialized = false;

        public void add(DatabaseHolder holder) {
            databaseDefinitionMap.putAll(holder.databaseDefinitionMap);
            databaseNameMap.putAll(holder.databaseNameMap);
            typeConverters.putAll(holder.typeConverters);
            databaseClassLookupMap.putAll(holder.databaseClassLookupMap);
            initialized = true;
        }

        public boolean isInitialized() {
            return initialized;
        }
    }

    static FlowConfig config;

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
    @NonNull
    public static String getTableName(Class<?> table) {
        ModelAdapter modelAdapter = getModelAdapterOrNull(table);
        String tableName = null;
        if (modelAdapter == null) {
            ModelViewAdapter modelViewAdapter = getModelViewAdapterOrNull(table);
            if (modelViewAdapter != null) {
                tableName = modelViewAdapter.getViewName();
            } else {
                throwCannotFindAdapter("ModelAdapter/ModelViewAdapter", table);
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
    public static Class<?> getTableClassForName(String databaseName, String tableName) {
        DatabaseDefinition databaseDefinition = getDatabase(databaseName);
        Class<?> modelClass = databaseDefinition.getModelClassForName(tableName);
        if (modelClass == null) {
            modelClass = databaseDefinition.getModelClassForName(QueryBuilder.quote(tableName));
            if (modelClass == null) {
                throw new IllegalArgumentException(String.format("The specified table %1s was not found. " +
                        "Did you forget to add the @Table annotation and point it to %1s?",
                    tableName, databaseName));
            }
        }
        return modelClass;
    }

    /**
     * @param table The table to lookup the database for.
     * @return the corresponding {@link DatabaseDefinition} for the specified model
     */
    @NonNull
    public static DatabaseDefinition getDatabaseForTable(Class<?> table) {
        checkDatabaseHolder();
        DatabaseDefinition databaseDefinition = globalDatabaseHolder.getDatabaseForTable(table);
        if (databaseDefinition == null) {
            throw new InvalidDBConfiguration("Model object: " + table.getName() +
                " is not registered with a Database. " + "Did you forget an annotation?");
        }
        return databaseDefinition;
    }

    @NonNull
    public static DatabaseDefinition getDatabase(Class<?> databaseClass) {
        checkDatabaseHolder();
        DatabaseDefinition databaseDefinition = globalDatabaseHolder.getDatabase(databaseClass);
        if (databaseDefinition == null) {
            throw new InvalidDBConfiguration("Database: " + databaseClass.getName() + " is not a registered Database. " +
                "Did you forget the @Database annotation?");
        }
        return databaseDefinition;
    }

    @NonNull
    public static DatabaseWrapper getWritableDatabaseForTable(Class<?> table) {
        return getDatabaseForTable(table).getWritableDatabase();
    }

    /**
     * @param databaseName The name of the database. Will throw an exception if the database doesn't exist.
     * @return the {@link DatabaseDefinition} for the specified database
     */
    @NonNull
    public static DatabaseDefinition getDatabase(String databaseName) {
        checkDatabaseHolder();
        DatabaseDefinition database = globalDatabaseHolder.getDatabase(databaseName);

        if (database != null) {
            return database;
        }

        throw new InvalidDBConfiguration("The specified database" + databaseName + " was not found. " +
            "Did you forget the @Database annotation?");
    }

    @NonNull
    public static DatabaseWrapper getWritableDatabase(String databaseName) {
        return getDatabase(databaseName).getWritableDatabase();
    }

    @NonNull
    public static DatabaseWrapper getWritableDatabase(Class<?> databaseClass) {
        return getDatabase(databaseClass).getWritableDatabase();
    }

    /**
     * Loading the module Database holder via reflection.
     * <p>
     * It is assumed FlowManager.init() is called by the application that uses the
     * module database. This method should only be called if you need to load databases
     * that are part of a module. Building once will give you the ability to add the class.
     */
    public static void initModule(Class<? extends DatabaseHolder> generatedClassName) {
        loadDatabaseHolder(generatedClassName);
    }

    public static FlowConfig getConfig() {
        if (config == null) {
            throw new IllegalStateException("Configuration is not initialized. " +
                "Please call init(FlowConfig) in your application class.");
        }
        return config;
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
            e.printStackTrace();
            throw new ModuleNotFoundException("Cannot load " + holderClass, e);
        }
    }

    /**
     * Resets all databases and associated files.
     */
    public static void reset() {
        Set<Map.Entry<Class<?>, DatabaseDefinition>> entrySet = globalDatabaseHolder.databaseClassLookupMap.entrySet();
        for (Map.Entry<Class<?>, DatabaseDefinition> value : entrySet) {
            value.getValue().reset(getContext());
        }
        globalDatabaseHolder.reset();
        loadedModules.clear();
    }

    /**
     * Will throw an exception if this class is not initialized yet in {@link #init(FlowConfig)}
     *
     * @return The shared context.
     */
    @NonNull
    public static Context getContext() {
        if (config == null) {
            throw new IllegalStateException("You must provide a valid FlowConfig instance. " +
                "We recommend calling init() in your application class.");
        }
        return config.getContext();
    }

    /**
     * Helper method to simplify the {@link #init(FlowConfig)}. Use {@link #init(FlowConfig)} to provide
     * more customization.
     *
     * @param context - should be application context, but not necessary as we retrieve it anyways.
     */
    public static void init(@NonNull Context context) {
        init(new FlowConfig.Builder(context).build());
    }

    /**
     * Initializes DBFlow, loading the main application Database holder via reflection one time only.
     * This will trigger all creations, updates, and instantiation for each database defined.
     *
     * @param flowConfig The configuration instance that will help shape how DBFlow gets constructed.
     */
    public static void init(@NonNull FlowConfig flowConfig) {
        FlowManager.config = flowConfig;

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

        if (flowConfig.databaseHolders() != null && !flowConfig.databaseHolders().isEmpty()) {
            for (Class<? extends DatabaseHolder> holder : flowConfig.databaseHolders()) {
                loadDatabaseHolder(holder);
            }
        }

        if (flowConfig.openDatabasesOnInit()) {
            List<DatabaseDefinition> databaseDefinitions = globalDatabaseHolder.getDatabaseDefinitions();
            for (DatabaseDefinition databaseDefinition : databaseDefinitions) {
                // triggers open, create, migrations.
                databaseDefinition.getWritableDatabase();
            }
        }
    }

    /**
     * @param objectClass A class with an associated type converter. May return null if not found.
     * @return The specific {@link TypeConverter} for the specified class. It defines
     * how the custom datatype is handled going into and out of the DB.
     */
    public static TypeConverter getTypeConverterForClass(Class<?> objectClass) {
        checkDatabaseHolder();
        return globalDatabaseHolder.getTypeConverterForClass(objectClass);
    }

    // region Getters

    /**
     * Release reference to context and {@link FlowConfig}
     */
    public static synchronized void destroy() {
        Set<Map.Entry<Class<?>, DatabaseDefinition>> entrySet =
            globalDatabaseHolder.databaseClassLookupMap.entrySet();
        for (Map.Entry<Class<?>, DatabaseDefinition> value : entrySet) {
            value.getValue().destroy(getContext());
        }

        config = null;

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
    @NonNull
    public static <TModel> InstanceAdapter<TModel> getInstanceAdapter(Class<TModel> modelClass) {
        InstanceAdapter internalAdapter = getModelAdapterOrNull(modelClass);
        if (internalAdapter == null) {
            internalAdapter = getModelViewAdapterOrNull(modelClass);
            if (internalAdapter == null) {
                internalAdapter = getQueryModelAdapterOrNull(modelClass);
            }
        }

        if (internalAdapter == null) {
            throwCannotFindAdapter("InstanceAdapter", modelClass);
        }

        return internalAdapter;
    }

    /**
     * @param modelClass The class that implements {@link Model} to find an adapter for.
     * @return The adapter associated with the class. If its not a {@link ModelAdapter},
     * it checks both the {@link ModelViewAdapter} and {@link QueryModelAdapter}.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static <TModel> RetrievalAdapter<TModel> getRetrievalAdapter(Class<TModel> modelClass) {
        RetrievalAdapter<TModel> retrievalAdapter = getModelAdapterOrNull(modelClass);
        if (retrievalAdapter == null) {
            retrievalAdapter = getModelViewAdapterOrNull(modelClass);
            if (retrievalAdapter == null) {
                retrievalAdapter = getQueryModelAdapterOrNull(modelClass);
            }
        }
        if (retrievalAdapter == null) {
            throwCannotFindAdapter("RetrievalAdapter", modelClass);
        }

        return retrievalAdapter;
    }


    /**
     * @param modelClass The class of the table
     * @param <TModel>   The class that implements {@link Model}
     * @return The associated model adapter (DAO) that is generated from a {@link Table} class. Handles
     * interactions with the database. This method is meant for internal usage only.
     * We strongly prefer you use the built-in methods associated with {@link Model} and {@link BaseModel}.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static <TModel> ModelAdapter<TModel> getModelAdapter(Class<TModel> modelClass) {
        final ModelAdapter modelAdapter = getModelAdapterOrNull(modelClass);
        if (modelAdapter == null) {
            throwCannotFindAdapter("ModelAdapter", modelClass);
        }
        return modelAdapter;
    }

    /**
     * Returns the model view adapter for a SQLite VIEW. These are only created with the {@link com.raizlabs.android.dbflow.annotation.ModelView} annotation.
     *
     * @param modelViewClass The class of the VIEW
     * @param <TModelView>   The class that extends {@link BaseModelView}
     * @return The model view adapter for the specified model view.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static <TModelView> ModelViewAdapter<TModelView> getModelViewAdapter(
        Class<TModelView> modelViewClass) {
        final ModelViewAdapter modelViewAdapter = getModelViewAdapterOrNull(modelViewClass);
        if (modelViewAdapter == null) {
            throwCannotFindAdapter("ModelViewAdapter", modelViewClass);
        }
        return modelViewAdapter;
    }

    /**
     * Returns the query model adapter for an undefined query. These are only created with the {@link TQueryModel} annotation.
     *
     * @param queryModelClass The class of the query
     * @param <TQueryModel>   The class that extends {@link BaseQueryModel}
     * @return The query model adapter for the specified model query.
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static <TQueryModel> QueryModelAdapter<TQueryModel> getQueryModelAdapter(
        Class<TQueryModel> queryModelClass) {
        final QueryModelAdapter queryModelAdapter = getQueryModelAdapterOrNull(queryModelClass);
        if (queryModelAdapter == null) {
            throwCannotFindAdapter("QueryModelAdapter", queryModelClass);
        }
        return queryModelAdapter;
    }

    @NonNull
    public static ModelNotifier getModelNotifierForTable(Class<?> table) {
        return getDatabaseForTable(table).getModelNotifier();
    }

    @NonNull
    public static TableNotifierRegister newRegisterForTable(Class<?> table) {
        return getModelNotifierForTable(table).newRegister();
    }

    @Nullable
    private static <T> ModelAdapter<T> getModelAdapterOrNull(Class<T> modelClass) {
        return FlowManager.getDatabaseForTable(modelClass).getModelAdapterForTable(modelClass);
    }

    @Nullable
    private static <T> ModelViewAdapter<T> getModelViewAdapterOrNull(Class<T> modelClass) {
        return FlowManager.getDatabaseForTable(modelClass).getModelViewAdapterForTable(modelClass);
    }

    @Nullable
    private static <T> QueryModelAdapter<T> getQueryModelAdapterOrNull(Class<T> modelClass) {
        return FlowManager.getDatabaseForTable(modelClass).getQueryModelAdapterForQueryClass(modelClass);
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
        return getDatabase(databaseName).getHelper().isDatabaseIntegrityOk();
    }

    private static void throwCannotFindAdapter(String type, Class<?> clazz) {
        throw new IllegalArgumentException("Cannot find " + type + " for " + clazz + ". Ensure " +
            "the class is annotated with proper annotation.");
    }

    private static void checkDatabaseHolder() {
        if (!globalDatabaseHolder.isInitialized()) {
            throw new IllegalStateException("The global database holder is not initialized. Ensure you call " +
                "FlowManager.init() before accessing the database.");
        }
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