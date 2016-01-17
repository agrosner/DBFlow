package com.raizlabs.android.dbflow.structure.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

/**
 * Description: Provides a base implementation that wraps a database, so other database engines potentially can
 * be used.
 */
public interface DatabaseWrapper {

    void execSQL(String query);

    void beginTransaction();

    void setTransactionSuccessful();

    void endTransaction();

    int getVersion();

    DatabaseStatement compileStatement(String rawQuery);

    Cursor rawQuery(String query, String[] selectionArgs);

    long updateWithOnConflict(String tableName, ContentValues contentValues, String where, String[] whereArgs, int conflictAlgorithm);

    long update(String tableName, ContentValues contentValues, String where, String[] whereArgs);
}
