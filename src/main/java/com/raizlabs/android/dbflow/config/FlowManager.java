package com.raizlabs.android.dbflow.config;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.raizlabs.android.dbflow.DatabaseHelperListener;
import com.raizlabs.android.dbflow.cache.ModelCache;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Holds the {@link com.raizlabs.android.dbflow.cache.ModelCache} and provides helper methods
 * for the singleton.
 */
public class FlowManager {

    private static ModelCache cache;

    public static void initialize(DBConfiguration dbConfiguration, DatabaseHelperListener databaseHelperListener) {
        cache = new ModelCache();
        cache.initialize(dbConfiguration, databaseHelperListener);
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
