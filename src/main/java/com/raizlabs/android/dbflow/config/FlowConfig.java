package com.raizlabs.android.dbflow.config;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.raizlabs.android.dbflow.DatabaseHelperListener;
import com.raizlabs.android.dbflow.cache.ModelCache;
import com.raizlabs.android.singleton.Singleton;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class FlowConfig {

    public static void initialize(DBConfiguration dbConfiguration, DatabaseHelperListener databaseHelperListener) {
        ModelCache modelCache = new ModelCache();
        modelCache.initialize(dbConfiguration, databaseHelperListener);

        // Create and store our singleton instance
        new Singleton<ModelCache>(modelCache);
    }


    public static Singleton<ModelCache> getCacheSingleton() {
        return new Singleton<ModelCache>(ModelCache.class);
    }

    public static ModelCache getCache() {
        return getCacheSingleton().getInstance();
    }

    public static SQLiteOpenHelper getSqlHelper() {
        return getCache().getHelper();
    }

    /**
     * Wraps the runnable around {@link android.database.sqlite.SQLiteDatabase#beginTransaction()} and the other methods.
     * @param runnable
     */
    public static void transact(Runnable runnable) {
        SQLiteDatabase database = getSqlHelper().getWritableDatabase();
        database.beginTransaction();
        try{
            runnable.run();
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }
}
