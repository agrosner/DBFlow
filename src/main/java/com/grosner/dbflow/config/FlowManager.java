package com.grosner.dbflow.config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.grosner.dbflow.DatabaseHelperListener;
import com.grosner.dbflow.cache.ModelCache;
import com.grosner.dbflow.structure.Model;
import com.grosner.dbflow.structure.ModelPathManager;
import com.grosner.dbflow.structure.TableStructure;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Holds the {@link com.grosner.dbflow.cache.ModelCache} and provides helper methods
 * for the singleton.
 */
public class FlowManager {

    private static boolean isInitialized = false;

    private static ModelCache cache;

    private static Context sContext;

    public static Context getContext() {
        if(sContext == null) {
            throw new IllegalStateException("Context cannot be null for FlowManager");
        }
        return sContext;
    }

    public static void initialize(Context context, DBConfiguration dbConfiguration) {
        initialize(context, dbConfiguration, null);
    }

    public static void initialize(Context context, DBConfiguration dbConfiguration, DatabaseHelperListener databaseHelperListener) {
        if(!isInitialized) {
            sContext = context;
            ModelPathManager.addPath(sContext.getPackageName());
            getCache().initialize(dbConfiguration, databaseHelperListener);
        } else {
            FlowLog.log(FlowLog.Level.V, "DBFlow is already initialized.");
        }
    }

    public static void destroy() {
        getCache().destroy();
        cache = null;
        isInitialized = false;
    }

    public static ModelCache getCache() {
        if(cache == null) {
            cache = new ModelCache();
        }
        return cache;
    }

    /**
     * Gets the {@link android.database.sqlite.SQLiteOpenHelper} from the cache.
     * @return
     */
    public static SQLiteOpenHelper getSqlHelper() {
        return getCache().getHelper();
    }

    public static SQLiteDatabase getWritableDatabase() {
        return getSqlHelper().getWritableDatabase();
    }

    public static <ModelClass extends Model> TableStructure<ModelClass> getTableStructureForClass(Class<ModelClass> modelClass) {
        return getCache().getStructure().getTableStructureForClass(modelClass);
    }

    /**
     * Wraps the runnable around {@link android.database.sqlite.SQLiteDatabase#beginTransaction()} and the other methods.
     * @param runnable
     */
    public static void transact(SQLiteDatabase database, Runnable runnable) {
        database.beginTransaction();
        try{
            runnable.run();
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    /**
     * Wraps the runnable around {@link android.database.sqlite.SQLiteDatabase#beginTransaction()} and the other methods.
     * @param runnable
     */
    public static void transact(Runnable runnable) {
       transact(getWritableDatabase(), runnable);
    }
}
