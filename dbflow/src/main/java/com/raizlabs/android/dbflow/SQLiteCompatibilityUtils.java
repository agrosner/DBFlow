package com.raizlabs.android.dbflow;

import android.content.ContentValues;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.structure.database.AndroidDatabase;
import com.raizlabs.android.dbflow.structure.database.AndroidDatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Provides some backwards compatibility with database operations in Android.
 */
@Deprecated
public class SQLiteCompatibilityUtils {

    /**
     * Performs an {@link android.database.sqlite.SQLiteStatement#executeUpdateDelete()} with support for
     * previous versions. This method is no longer necessary due to {@link AndroidDatabaseStatement}
     * providing support.
     *
     * @deprecated Use {@link DatabaseWrapper#compileStatement(String)},
     * then {@link DatabaseStatement#executeUpdateDelete()}
     */
    @Deprecated
    public static long executeUpdateDelete(DatabaseWrapper database, String rawQuery) {
        return database.compileStatement(rawQuery).executeUpdateDelete();
    }

    /**
     * Updates the specified table with the specified values. It does not support {@link ConflictAction}
     * for pre-froyo devices. This method is no longer necessary due to {@link AndroidDatabase}
     * providing support.
     */
    @Deprecated
    public static long updateWithOnConflict(DatabaseWrapper database, String tableName, ContentValues contentValues, String where, String[] whereArgs, int conflictAlgorithm) {
        return database.updateWithOnConflict(tableName, contentValues, where, whereArgs, conflictAlgorithm);
    }

}
