package com.raizlabs.android.dbflow.config;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.raizlabs.android.dbflow.DatabaseHelperListener;
import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.migration.Migration;
import com.raizlabs.android.dbflow.structure.BaseModelView;
import com.raizlabs.android.dbflow.structure.InvalidDBConfiguration;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.ModelViewAdapter;
import com.raizlabs.android.dbflow.structure.container.ContainerAdapter;

import java.util.List;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Description: Holds information about the database and wraps some of the methods.
 */
public class FlowManager {

    private static Context context;

    private static DatabaseHolder mDatabaseHolder;

    /**
     * Returns the table name for the specific model class
     *
     * @param table The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return The table name, which can be different than the {@link com.raizlabs.android.dbflow.structure.Model} class name
     */
    @SuppressWarnings("unchecked")
    public static String getTableName(Class<? extends Model> table) {
        ModelAdapter modelAdapter = getModelAdapter(table);
        String tableName = null;
        if (modelAdapter == null) {
            ModelViewAdapter modelViewAdapter = getDatabaseForTable(table).getModelViewAdapterForTable((Class<? extends BaseModelView>) table);
            if (modelViewAdapter != null) {
                tableName = modelViewAdapter.getViewName();
            }
        } else {
            tableName = modelAdapter.getTableName();
        }
        return tableName;
    }

    /**
     * @param table The table to lookup the database for.
     * @returnthe corresponding {@link com.raizlabs.android.dbflow.config.BaseDatabaseDefinition} for the specified model
     */
    public static BaseDatabaseDefinition getDatabaseForTable(Class<? extends Model> table) {
        getDatabaseHolder();

        BaseDatabaseDefinition flowManager = mDatabaseHolder.getDatabaseForTable(table);
        if (flowManager == null) {
            throw new InvalidDBConfiguration("Table: " + table.getName() + " is not registered with a Database. " +
                    "Did you forget the @Table annotation?");
        }
        return flowManager;
    }

    /**
     * @param databaseName The name of the database. Will throw an exception if the database doesn't exist.
     * @return the {@link com.raizlabs.android.dbflow.config.BaseDatabaseDefinition} for the specified databaseName
     */
    public static BaseDatabaseDefinition getDatabase(String databaseName) {
        getDatabaseHolder();

        BaseDatabaseDefinition database = mDatabaseHolder.getDatabase(databaseName);
        if (database == null) {
            throw new InvalidDBConfiguration("The specified database" + databaseName + " was not found. " +
                    "Did you forget the @Database annotation?");
        }
        return database;
    }

    /**
     * @return The database holder, creating if necessary using reflection.
     */
    protected static DatabaseHolder getDatabaseHolder() {
        if (mDatabaseHolder == null) {
            try {
                mDatabaseHolder = (DatabaseHolder) Class.forName("com.raizlabs.android.dbflow.config.Database$Holder").newInstance();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        return mDatabaseHolder;
    }

    /**
     * Returns the primary where query for a specific table. Its the WHERE statement containing columnName = ?.
     *
     * @param table The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return The primary where query
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ConditionQueryBuilder<ModelClass> getPrimaryWhereQuery(Class<ModelClass> table) {
        return getDatabaseForTable(table).getModelAdapterForTable(table).getPrimaryModelWhere();
    }

    /**
     * Will throw an exception if this class is not initialized yet in {@link #init(android.content.Context)}
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
     * Initializes DBFlow, retrieving the Database holder via reflection. This will trigger all creations,
     * updates, and instantiation for each database defined.
     *
     * @param context The shared context for database usage.
     */
    public static void init(Context context) {
        FlowManager.context = context;
        getDatabaseHolder();
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
     * Returns the specific {@link com.raizlabs.android.dbflow.converter.TypeConverter} for the specified class. It defines
     * how the class is stored in the DB
     *
     * @param objectClass A class with an associated type converter. May return null if not found.
     * @return
     */
    public static TypeConverter getTypeConverterForClass(Class<?> objectClass) {
        return mDatabaseHolder.getTypeConverterForClass(objectClass);
    }

    // region Getters

    /**
     * Release reference to context
     */
    public static synchronized void destroy() {
        context = null;
    }

    /**
     * Returns the model adapter for the specified table. Used in loading and modifying the model class.
     *
     * @param modelClass   The class of the table
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ModelAdapter<ModelClass> getModelAdapter(Class<ModelClass> modelClass) {
        return FlowManager.getDatabaseForTable(modelClass).getModelAdapterForTable(modelClass);
    }

    /**
     * @param modelClass   The class of the table
     * @param <ModelClass> The class that implements {@link com.raizlabs.android.dbflow.structure.Model}
     * @return the container adapter for the specified table. These are only generated when you specify {@link com.raizlabs.android.dbflow.annotation.ContainerAdapter}
     * in your model class so it can be used for containers. These are not generated by default as a means to keep app size down.
     */
    @SuppressWarnings("unchecked")
    public static <ModelClass extends Model> ContainerAdapter<ModelClass> getContainerAdapter(Class<ModelClass> modelClass) {
        return FlowManager.getDatabaseForTable(modelClass).getModelContainerAdapterForTable(modelClass);
    }

    /**
     * Returns the model view adapter for a SQLite VIEW. These are only created with the {@link com.raizlabs.android.dbflow.annotation.ModelView} annotation.
     *
     * @param modelViewClass   The class of the VIEW
     * @param <ModelViewClass> The class that extends {@link com.raizlabs.android.dbflow.structure.BaseModelView}
     * @return The model view adapter for the specified model view.
     */
    @SuppressWarnings("unchecked")
    public static <ModelViewClass extends BaseModelView<? extends Model>> ModelViewAdapter<? extends Model, ModelViewClass> getModelViewAdapter(Class<ModelViewClass> modelViewClass) {
        return FlowManager.getDatabaseForTable(modelViewClass).getModelViewAdapterForTable(modelViewClass);
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
     * @param helper The helper to user to look up integrity.
     * @return true if it's integrity is OK.
     */
    public static boolean isDatabaseIntegrityOk(SQLiteOpenHelper helper) {
        boolean integrityOk = true;

        SQLiteStatement prog = null;
        try {
            prog = helper.getWritableDatabase().compileStatement("PRAGMA quick_check(1)");
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

}
