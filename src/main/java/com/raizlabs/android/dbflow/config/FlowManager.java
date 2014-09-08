package com.raizlabs.android.dbflow.config;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.raizlabs.android.dbflow.DatabaseHelperListener;
import com.raizlabs.android.dbflow.cache.ModelCache;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.TableStructure;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Holds the {@link com.raizlabs.android.dbflow.cache.ModelCache} and provides helper methods
 * for the singleton.
 */
public class FlowManager {

    private static boolean isInitialized = false;

    private static ModelCache cache;

    public static void initialize(DBConfiguration dbConfiguration, DatabaseHelperListener databaseHelperListener) {
        if(!isInitialized) {
            getCache().initialize(dbConfiguration, databaseHelperListener);
        } else {
            FlowLog.v(FlowManager.class.getSimpleName(), "DBFlow is already initialized.");
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

    public static <ModelClass extends Model> TableStructure getTableStructureForClass(Class<ModelClass> modelClass) {
        return getCache().getStructure().getTableStructureForClass(modelClass);
    }

    /**
     * Wraps the runnable around {@link android.database.sqlite.SQLiteDatabase#beginTransaction()} and the other methods.
     * @param runnable
     */
    public static void transact(Runnable runnable) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try{
            runnable.run();
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }
}
