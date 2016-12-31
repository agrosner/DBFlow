package com.raizlabs.android.dbflow.structure.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    public SQLiteDatabase getDatabase() {
        return database;
    }

    @Override
    public DatabaseStatement compileStatement(String rawQuery) {
        return AndroidDatabaseStatement.from(database.compileStatement(rawQuery), database);
    }

    @Override
    public Cursor rawQuery(String query, String[] selectionArgs) {
        return database.rawQuery(query, selectionArgs);
    }

    @Override
    public long updateWithOnConflict(String tableName, ContentValues contentValues, String where, String[] whereArgs, int conflictAlgorithm) {
        long count;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            count = database.updateWithOnConflict(tableName, contentValues, where, whereArgs, conflictAlgorithm);
        } else {
            count = database.update(tableName, contentValues, where, whereArgs);
        }
        return count;
    }

    @Override
    public long insertWithOnConflict(String tableName, String nullColumnHack, ContentValues values, int sqLiteDatabaseAlgorithmInt) {
        long count;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            count = database.insertWithOnConflict(tableName, nullColumnHack, values, sqLiteDatabaseAlgorithmInt);
        } else {
            count = database.insert(tableName, nullColumnHack, values);
        }
        return count;
    }

    @Override
    public Cursor query(
            @NonNull String tableName,
            @Nullable String[] columns,
            @Nullable String selection,
            @Nullable String[] selectionArgs,
            @Nullable String groupBy,
            @Nullable String having,
            @Nullable String orderBy) {
        return database.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    @Override
    public int delete(@NonNull String tableName, @Nullable String whereClause, @Nullable String[] whereArgs) {
        return database.delete(tableName, whereClause, whereArgs);
    }
}
