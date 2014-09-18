package com.grosner.dbflow.config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import com.grosner.dbflow.DatabaseHelperListener;
import com.grosner.dbflow.converter.CalendarConverter;
import com.grosner.dbflow.converter.DateConverter;
import com.grosner.dbflow.converter.JsonConverter;
import com.grosner.dbflow.converter.LocationConverter;
import com.grosner.dbflow.converter.SqlDateConverter;
import com.grosner.dbflow.converter.TypeConverter;
import com.grosner.dbflow.structure.DBStructure;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.ModelPathManager;
import com.grosner.dbflow.structure.TableStructure;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Holds information about the database and wraps some of the methods.
 */
public class FlowManager {

    /**
     * Whether this database has already been initialized. This is to prevent multiple instantiations.
     */
    private boolean isInitialized = false;

    /**
     * The shared manager for the application. Most use cases will only require one DB
     */
    private static FlowManager manager;

    /**
     * Returns the shared manager for this app. It exists for most use cases as the only DB, but to define
     * more DB, create another one.
     *
     * @return
     */
    public static FlowManager getInstance() {
        if (manager == null) {
            manager = new FlowManager();
        }
        return manager;
    }

    /**
     * The configuration of this {@link com.grosner.dbflow.config.FlowManager} database
     */
    private DBConfiguration mDbConfiguration;

    /**
     * This holds the inherit structure of the database
     */
    private DBStructure mStructure;

    /**
     * This adds extra functionality to the {@link android.database.sqlite.SQLiteOpenHelper}
     */
    private FlowSQLiteOpenHelper mHelper;

    private Context context;

    /**
     * The default {@link com.grosner.dbflow.converter.TypeConverter} for this manager.
     */
    private Map<Class<?>, TypeConverter> mTypeConverters = new HashMap<Class<?>, TypeConverter>() {
        {
            put(Calendar.class, new CalendarConverter());
            put(java.sql.Date.class, new SqlDateConverter());
            put(java.util.Date.class, new DateConverter());
            put(Location.class, new LocationConverter());
            put(JSONObject.class, new JsonConverter());
        }
    };

    /**
     * Will throw an exception if this class is not initialized yet in {@link #initialize(android.content.Context, DBConfiguration)}
     *
     * @return
     */
    public Context getContext() {
        if (context == null) {
            throw new IllegalStateException("Context cannot be null for FlowManager");
        }
        return context;
    }

    /**
     * Call this in your application's {@link android.app.Application#onCreate()} method.
     *
     * @param context
     * @param dbConfiguration
     * @see #initialize(android.content.Context, DBConfiguration, com.grosner.dbflow.DatabaseHelperListener)
     */
    public void initialize(Context context, DBConfiguration dbConfiguration) {
        initialize(context, dbConfiguration, null);
    }

    /**
     * Call this in your application's {@link android.app.Application#onCreate()} method. Initializes the database,
     * the structure cache, and opens the database.
     *
     * @param context
     * @param dbConfiguration
     * @param databaseHelperListener
     */
    public void initialize(Context context, DBConfiguration dbConfiguration, DatabaseHelperListener databaseHelperListener) {
        if (!isInitialized) {
            this.context = context;
            ModelPathManager.addPath(this.context.getPackageName());

            mDbConfiguration = dbConfiguration;

            mStructure = new DBStructure(this, dbConfiguration);

            mHelper = new FlowSQLiteOpenHelper(this, dbConfiguration);
            mHelper.setDatabaseListener(databaseHelperListener);
            mHelper.getWritableDatabase();
        } else {
            FlowLog.log(FlowLog.Level.V, "DBFlow is already initialized.");
        }
    }

    /**
     * Releases references to the structure, configuration, and closes the DB.
     */
    public void destroy() {
        mDbConfiguration = null;
        mStructure = null;
        mHelper.getWritableDatabase().close();
        mHelper = null;
        isInitialized = false;
    }

    // region Getters

    public SQLiteOpenHelper getSqlHelper() {
        return mHelper;
    }

    public SQLiteDatabase getWritableDatabase() {
        return getSqlHelper().getWritableDatabase();
    }

    public DBStructure getStructure() {
        return mStructure;
    }

    /**
     * Returns a {@link com.grosner.dbflow.structure.TableStructure} for a specific model class
     *
     * @param modelClass   The table class we want to retrieve
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return the table structure for this model class
     */
    public <ModelClass extends Model> TableStructure<ModelClass> getTableStructureForClass(Class<ModelClass> modelClass) {
        return getStructure().getTableStructureForClass(modelClass);
    }

    /**
     * Returns the table name for the specific model class
     *
     * @param modelClass The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return The table name, which can be different than the {@link com.grosner.dbflow.structure.Model} class name
     */
    public String getTableName(Class<? extends Model> modelClass) {
        return mStructure.getTableStructure().get(modelClass).getTableName();
    }

    public DBConfiguration getDbConfiguration() {
        return mDbConfiguration;
    }

    public FlowSQLiteOpenHelper getHelper() {
        return mHelper;
    }

    /**
     * Returns the specific {@link com.grosner.dbflow.converter.TypeConverter} for this model. It defines
     * how the class is stored in the DB
     *
     * @param modelClass   The class that implements {@link com.grosner.dbflow.structure.Model}
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return
     */
    @SuppressWarnings("unchecked")
    public <ModelClass> TypeConverter<?, ModelClass> getTypeConverterForClass(Class<ModelClass> modelClass) {
        return mTypeConverters.get(modelClass);
    }

    // endregion


    /**
     * Adds a new {@link com.grosner.dbflow.converter.TypeConverter} to this map
     *
     * @param typeConverterClass
     */
    public void putTypeConverterForClass(Class typeConverterClass) {
        try {
            TypeConverter typeConverter = (TypeConverter) typeConverterClass.newInstance();
            mTypeConverters.put(typeConverter.getModelType(), typeConverter);
        } catch (Throwable e) {
            FlowLog.logError(e);
        }
    }

    /**
     * Wraps the runnable around {@link android.database.sqlite.SQLiteDatabase#beginTransaction()} and the other methods.
     *
     * @param runnable
     */
    public static void transact(SQLiteDatabase database, Runnable runnable) {
        database.beginTransaction();
        try {
            runnable.run();
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Wraps the runnable around {@link android.database.sqlite.SQLiteDatabase#beginTransaction()} and the other methods.
     *
     * @param runnable
     */
    public static void transact(Runnable runnable) {
        transact(getInstance().getWritableDatabase(), runnable);
    }
}
