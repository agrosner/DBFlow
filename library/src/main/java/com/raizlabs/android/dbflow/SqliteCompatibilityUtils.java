package com.raizlabs.android.dbflow;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;

import com.raizlabs.android.dbflow.sql.Query;

/**
 * Description: Provides some backwards compatibility with database operations in Android.
 */
public class SQLiteCompatibilityUtils {

    /**
     * Performs an {@link android.database.sqlite.SQLiteStatement#executeUpdateDelete()} with support for
     * previous versions.
     *
     * @param database  The database handle
     * @param tableName The name of the table
     * @param query     The query to use.
     * @return The count of rows changed.
     */
    public static long executeUpdateDelete(SQLiteDatabase database, String tableName, Query query) {
        long count = 0;
        SQLiteStatement sqLiteStatement = database.compileStatement(query.getQuery());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            count = sqLiteStatement.executeUpdateDelete();
        } else {
            sqLiteStatement.execute();

            Cursor cursor = null;
            try {
                cursor = database.rawQuery("SELECT changes() AS affected_row_count", null);
                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                    count = cursor.getLong(cursor.getColumnIndex("affected_row_count"));
                }
            } catch (SQLException e) {
                // Handle exception here.
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return count;
    }

    /**
     * Updates the specified table with the specified values. It does not support {@link com.raizlabs.android.dbflow.annotation.ConflictAction}
     * for pre-froyo devices.
     *
     * @param database          The database handle
     * @param tableName         The name of the table
     * @param contentValues     The values to update a {@link com.raizlabs.android.dbflow.structure.Model} with.
     * @param where             The string query to use for WHERE
     * @param whereArgs         The arguments to the WHERE
     * @param conflictAlgorithm The algorithm to use for conflicts.
     * @return The count of rows changed.
     */
    public static long updateWithOnConflict(SQLiteDatabase database, String tableName, ContentValues contentValues, String where, String[] whereArgs, int conflictAlgorithm) {
        long count;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            count = database.updateWithOnConflict(tableName, contentValues, where, whereArgs, conflictAlgorithm);
        } else {
            count = database.update(tableName, contentValues, where, whereArgs);
        }
        return count;
    }

}
