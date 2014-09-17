package com.grosner.dbflow;

import android.database.sqlite.SQLiteDatabase;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public interface DatabaseHelperListener {

    public void onOpen(SQLiteDatabase database);

    public void onCreate(SQLiteDatabase db);

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);


}
