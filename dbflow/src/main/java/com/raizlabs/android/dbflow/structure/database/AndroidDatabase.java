package com.raizlabs.android.dbflow.structure.database;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

/**
 * Description: Specifies the android default implementation of a database.
 */
public class AndroidDatabase implements DatabaseWrapper {

    public static AndroidDatabase from(SQLiteDatabase database) {
        return new AndroidDatabase(database);
    }

    private final SQLiteDatabase database;

    AndroidDatabase(@NonNull SQLiteDatabase database) {
        this.database = database;
    }

    @Override
    public void execSQL(String query) {
        database.execSQL(query);
    }

    @Override
    public void beginTransaction() {
        database.beginTransaction();
    }

    @Override
    public void setTransactionSuccessful() {
        database.setTransactionSuccessful();
    }

    @Override
    public void endTransaction() {
        database.endTransaction();
    }

    @Override
    public int getVersion() {
        return database.getVersion();
    }
}
