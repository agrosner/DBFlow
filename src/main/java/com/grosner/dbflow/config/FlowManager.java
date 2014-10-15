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
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.runtime.observer.ModelObserver;
import com.grosner.dbflow.structure.DBStructure;
import com.grosner.dbflow.structure.InvalidDBConfiguration;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.ModelPathManager;
import com.grosner.dbflow.structure.TableStructure;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
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
     * The multiple table setup when enabled will not by default scan for model classes. It will require
     * manual model addition.
     */
    private static boolean multipleDatabases;

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

    private static HashMap<Class<? extends Model>, FlowManager> mManagerMap = new HashMap<Class<? extends Model>, FlowManager>();

    /**
     * Returns the corresponding {@link com.grosner.dbflow.config.FlowManager} for the specified model
     * @param table
     * @return
     */
    public static FlowManager getManagerForTable(Class<? extends Model> table) {
        FlowManager flowManager;
        if(isMultipleDatabases()) {
            flowManager = mManagerMap.get(table);
        } else {
            flowManager = FlowManager.getInstance();
        }

        if(flowManager == null) {
            throw new InvalidDBConfiguration(table.getSimpleName());
        }

        return flowManager;
    }

    /**
     * Returns the table name for the specific model class
     *
     * @param table The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return The table name, which can be different than the {@link com.grosner.dbflow.structure.Model} class name
     */
    public static String getTableName(Class<? extends Model> table) {
        return FlowManager.getManagerForTable(table).getStructure().getTableStructure().get(table).getTableName();
    }

    /**
     * Puts which manager corresponds to the model class.
     * @param modelClass
     * @param manager
     */
    public static void putManagerForTable(Class<? extends Model> modelClass, FlowManager manager) {
        mManagerMap.put(modelClass, manager);
    }

    private static Context context;

    /**
     * The default {@link com.grosner.dbflow.converter.TypeConverter} for this manager.
     */
    private static Map<Class<?>, TypeConverter> mTypeConverters = new HashMap<Class<?>, TypeConverter>() {
        {
            put(Calendar.class, new CalendarConverter());
            put(java.sql.Date.class, new SqlDateConverter());
            put(java.util.Date.class, new DateConverter());
            put(Location.class, new LocationConverter());
            put(JSONObject.class, new JsonConverter());
        }
    };

    /**
     * Holds onto {@link com.grosner.dbflow.runtime.observer.ModelObserver} for each {@link com.grosner.dbflow.structure.Model}
     */
    private final Map<Class<? extends Model>, List<ModelObserver<? extends Model>>> mModelObserverMap
            = new HashMap<Class<? extends Model>, List<ModelObserver<? extends Model>>>();

    public static void setMultipleDatabases(boolean multipleDatabases) {
        FlowManager.multipleDatabases = multipleDatabases;
    }

    public static boolean isMultipleDatabases() {
        return multipleDatabases;
    }


    /**
     * Will throw an exception if this class is not initialized yet in {@link #initialize(android.content.Context, DBConfiguration)}
     *
     * @return
     */
    public static Context getContext() {
        if (context == null) {
            throw new IllegalStateException("Context cannot be null for FlowManager");
        }
        return context;
    }

    public static void setContext(Context context) {
        FlowManager.context = context;
        ModelPathManager.addPath(context.getPackageName());
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



    /**
     * Call this in your application's {@link android.app.Application#onCreate()} method.
     *
     * @param context
     * @param dbConfiguration
     * @see #initialize(android.content.Context, DBConfiguration, com.grosner.dbflow.DatabaseHelperListener)
     */
    public synchronized void initialize(DBConfiguration dbConfiguration) {
        initialize(dbConfiguration, null);
    }

    /**
     * Call this in your application's {@link android.app.Application#onCreate()} method. Initializes the database,
     * the structure cache, and opens the database.
     *
     * @param context
     * @param dbConfiguration
     * @param databaseHelperListener
     */
    public synchronized void initialize(DBConfiguration dbConfiguration, DatabaseHelperListener databaseHelperListener) {
        if (!isInitialized) {
            isInitialized = true;
            mDbConfiguration = dbConfiguration;

            mStructure = new DBStructure(this);

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
    public synchronized void destroy() {
        mDbConfiguration = null;
        mStructure = null;
        if(mHelper != null) {
            mHelper.getWritableDatabase().close();
            mHelper = null;
        }
        context = null;
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

    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Returns a {@link com.grosner.dbflow.structure.TableStructure} for a specific model class
     *
     * @param modelClass   The table class we want to retrieve
     * @param <ModelClass> The class that implements {@link com.grosner.dbflow.structure.Model}
     * @return the table structure for this model class
     */
    public <ModelClass extends Model> TableStructure<ModelClass> getTableStructureForClass(Class<ModelClass> modelClass) {
        TableStructure<ModelClass> tableStructure = getStructure().getTableStructureForClass(modelClass);
        if(tableStructure == null) {
            throw new RuntimeException("The table structure for : " + modelClass.getSimpleName() + " was not found. Have you " +
                    "added it to the DBConfiguration?");
        }
        return tableStructure;
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
    public static TypeConverter getTypeConverterForClass(Class<?> modelClass) {
        return mTypeConverters.get(modelClass);
    }

    // endregion


    /**
     * Adds a new {@link com.grosner.dbflow.converter.TypeConverter} to this map
     *
     * @param typeConverterClass
     */
    public static void putTypeConverterForClass(Class typeConverterClass) {
        try {
            TypeConverter typeConverter = (TypeConverter) typeConverterClass.newInstance();
            mTypeConverters.put(typeConverter.getModelType(), typeConverter);
        } catch (Throwable e) {
            FlowLog.logError(e);
        }
    }

    /**
     * Adds a {@link com.grosner.dbflow.runtime.observer.ModelObserver} to the structure
     *
     * @param modelObserver A model observer to listen for model changes/updates
     */
    @SuppressWarnings("unchecked")
    public void addModelObserverForClass(ModelObserver<? extends Model> modelObserver) {
        synchronized (mModelObserverMap) {
            List<ModelObserver<? extends Model>> modelObservers = getModelObserverListForClass(modelObserver.getModelClass());
            if (modelObservers == null) {
                modelObservers = new ArrayList<ModelObserver<? extends Model>>();
                getModelObserverMap().put(modelObserver.getModelClass(), modelObservers);
            }
            if (!modelObservers.contains(modelObserver)) {
                modelObservers.add(modelObserver);
            }
        }
    }

    /**
     * Removes a {@link com.grosner.dbflow.runtime.observer.ModelObserver} from this structure.
     *
     * @param modelObserver A model observer to listen for model changes/updates
     */
    @SuppressWarnings("unchecked")
    public void removeModelObserverForClass(ModelObserver<? extends Model> modelObserver) {
        synchronized (mModelObserverMap) {
            List<ModelObserver<? extends Model>> modelObservers = getModelObserverListForClass(modelObserver.getModelClass());
            if (modelObservers != null) {
                modelObservers.remove(modelObserver);

                if (modelObservers.isEmpty()) {
                    getModelObserverMap().remove(modelObserver.getModelClass());
                }
            }
        }
    }

    /**
     * Returns the associated {@link com.grosner.dbflow.runtime.observer.ModelObserver}s for the class
     *
     * @param modelClass
     * @return
     */
    public List<ModelObserver<? extends Model>> getModelObserverListForClass(Class<? extends Model> modelClass) {
        return getModelObserverMap().get(modelClass);
    }

    public Map<Class<? extends Model>, List<ModelObserver<? extends Model>>> getModelObserverMap() {
        return mModelObserverMap;
    }


    /**
     * Performs the listener event
     *
     * @param model
     */
    @SuppressWarnings("unchecked")
    public void fireModelChanged(final Model model, final ModelObserver.Mode mode) {
        synchronized (mModelObserverMap) {
            final List<ModelObserver<? extends Model>> modelObserverList = getModelObserverListForClass(model.getClass());
            if (!modelObserverList.isEmpty()) {
                TransactionManager.getInstance().processOnRequestHandler(new Runnable() {
                    @Override
                    public void run() {
                        for (ModelObserver modelObserver : modelObserverList) {
                            modelObserver.onModelChanged(FlowManager.getManagerForTable(model.getClass()), model, mode);
                        }
                    }
                });
            }
        }
    }

    /**
     * This will delete and recreate the whole stored database. WARNING: all data stored will be lost.
     * @param context The applications context
     */
    public void reset(Context context) {
        mStructure.reset(context);
    }
}
