package com.grosner.dbflow;

import android.database.sqlite.SQLiteDatabase;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides callbacks for {@link android.database.sqlite.SQLiteOpenHelper} methods
 */
public interface DatabaseHelperListener {

    /**
     * Called when the DB is opened
     * @param database The database that is opened
     */
    public void onOpen(SQLiteDatabase database);

    /**
     * Called when the DB is created
     * @param database The database that is created
     */
    public void onCreate(SQLiteDatabase database);

    /**
     * Called when the DB is upgraded.
     * @param database The database that is upgraded
     * @param oldVersion The previous DB version
     * @param newVersion The new DB version
     */
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion);


}
