package com.raizlabs.android.dbflow.structure.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Description: Specifies the android default implementation of a database.
 */
public class AndroidDatabase implements DatabaseWrapper {

    public static AndroidDatabase from(@NonNull SQLiteDatabase database) {
        return new AndroidDatabase(database);
    }

    private final SQLiteDatabase database;

    AndroidDatabase(@NonNull SQLiteDatabase database) {
        this.database = database;
    }

    @Override
    public void execSQL(@NonNull String query) {
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

    @NonNull
    @Override
    public DatabaseStatement compileStatement(@NonNull String rawQuery) {
        return AndroidDatabaseStatement.from(database.compileStatement(rawQuery), database);
    }

    @NonNull
    @Override
    public FlowCursor rawQuery(@NonNull String query, @Nullable String[] selectionArgs) {
        return FlowCursor.from(database.rawQuery(query, selectionArgs));
    }

    @Override
    public long updateWithOnConflict(@NonNull String tableName, @NonNull ContentValues contentValues, @Nullable String where, @Nullable String[] whereArgs, int conflictAlgorithm) {
        long count;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            count = database.updateWithOnConflict(tableName, contentValues, where, whereArgs, conflictAlgorithm);
        } else {
            count = database.update(tableName, contentValues, where, whereArgs);
        }
        return count;
    }

    @Override
    public long insertWithOnConflict(@NonNull String tableName, @Nullable String nullColumnHack, @NonNull ContentValues values, int sqLiteDatabaseAlgorithmInt) {
        long count;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            count = database.insertWithOnConflict(tableName, nullColumnHack, values, sqLiteDatabaseAlgorithmInt);
        } else {
            count = database.insert(tableName, nullColumnHack, values);
        }
        return count;
    }

    @NonNull
    @Override
    public FlowCursor query(@NonNull String tableName,
                            @Nullable String[] columns,
                            @Nullable String selection,
                            @Nullable String[] selectionArgs,
                            @Nullable String groupBy,
                            @Nullable String having,
                            @Nullable String orderBy) {
        return FlowCursor.from(database.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy));
    }

    @Override
    public int delete(@NonNull String tableName, @Nullable String whereClause, @Nullable String[] whereArgs) {
        return database.delete(tableName, whereClause, whereArgs);
    }
}
