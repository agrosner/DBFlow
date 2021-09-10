package com.raizlabs.android.dbflow.structure.database;

import android.content.ContentValues;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Description: Provides a base implementation that wraps a database, so other database engines potentially can
 * be used.
 */
public interface DatabaseWrapper {

    void execSQL(@NonNull String query);

    void beginTransaction();

    void setTransactionSuccessful();

    void endTransaction();

    int getVersion();

    @NonNull
    DatabaseStatement compileStatement(@NonNull String rawQuery);

    @NonNull
    FlowCursor rawQuery(@NonNull String query, @Nullable String[] selectionArgs);

    long updateWithOnConflict(@NonNull String tableName,
                              @NonNull ContentValues contentValues,
                              @Nullable String where,
                              @Nullable String[] whereArgs, int conflictAlgorithm);

    long insertWithOnConflict(@NonNull String tableName,
                              @Nullable String nullColumnHack,
                              @NonNull ContentValues values,
                              int sqLiteDatabaseAlgorithmInt);

    @NonNull
    FlowCursor query(@NonNull String tableName, @Nullable String[] columns, @Nullable String selection,
                     @Nullable String[] selectionArgs, @Nullable String groupBy,
                     @Nullable String having, @Nullable String orderBy);

    int delete(@NonNull String tableName, @Nullable String whereClause, @Nullable String[] whereArgs);
}
