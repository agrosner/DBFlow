package com.raizlabs.android.dbflow;

import android.database.sqlite.SQLiteDatabase;

/**
 * Description: Provides callbacks for {@link android.database.sqlite.SQLiteOpenHelper} methods
 */
public interface DatabaseHelperListener {

    /**
     * Called when the DB is opened
     *
     * @param database The database that is opened
     */
    void onOpen(SQLiteDatabase database);

    /**
     * Called when the DB is created
     *
     * @param database The database that is created
     */
    void onCreate(SQLiteDatabase database);

    /**
     * Called when the DB is upgraded.
     *
     * @param database   The database that is upgraded
     * @param oldVersion The previous DB version
     * @param newVersion The new DB version
     */
    void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion);


}
