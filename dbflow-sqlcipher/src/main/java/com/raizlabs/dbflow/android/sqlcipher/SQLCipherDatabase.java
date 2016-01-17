package com.raizlabs.dbflow.android.sqlcipher;

import android.content.ContentValues;
import android.database.Cursor;

import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import net.sqlcipher.database.SQLiteDatabase;

/**
 * Description: Implements the code necessary to use a {@link SQLiteDatabase} in dbflow.
 */
public class SQLCipherDatabase implements DatabaseWrapper {

    private final SQLiteDatabase database;

    public static SQLCipherDatabase from(SQLiteDatabase database) {
        return new SQLCipherDatabase(database);
    }

    SQLCipherDatabase(SQLiteDatabase database) {
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

    @Override
    public DatabaseStatement compileStatement(String rawQuery) {
        return SQLCipherStatement.from(database.compileStatement(rawQuery));
    }

    @Override
    public Cursor rawQuery(String query, String[] selectionArgs) {
        return database.rawQuery(query, selectionArgs);
    }

    @Override
    public long updateWithOnConflict(String tableName, ContentValues contentValues, String where, String[] whereArgs, int conflictAlgorithm) {
        return database.updateWithOnConflict(tableName, contentValues, where, whereArgs, conflictAlgorithm);
    }

    @Override
    public long insertWithOnConflict(String tableName, String nullColumnHack, ContentValues values, int sqLiteDatabaseAlgorithmInt) {
        return database.insertWithOnConflict(tableName, nullColumnHack, values, sqLiteDatabaseAlgorithmInt);
    }
}
