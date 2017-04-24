package com.raizlabs.android.dbflow.structure.database;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;

/**
 * Description:
 */
public class AndroidDatabaseStatement extends BaseDatabaseStatement {

    public static AndroidDatabaseStatement from(SQLiteStatement sqLiteStatement, SQLiteDatabase database) {
        return new AndroidDatabaseStatement(sqLiteStatement, database);
    }

    private final SQLiteStatement statement;
    private final SQLiteDatabase database;

    AndroidDatabaseStatement(SQLiteStatement statement, SQLiteDatabase database) {
        this.statement = statement;
        this.database = database;
    }

    public SQLiteStatement getStatement() {
        return statement;
    }

    @Override
    public long executeUpdateDelete() {
        long count = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            count = statement.executeUpdateDelete();
        } else {
            statement.execute();

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

    @Override
    public void execute() {
        statement.execute();
    }

    @Override
    public void close() {
        statement.close();
    }

    @Override
    public long simpleQueryForLong() {
        return statement.simpleQueryForLong();
    }

    @Override
    public String simpleQueryForString() {
        return statement.simpleQueryForString();
    }

    @Override
    public long executeInsert() {
        return statement.executeInsert();
    }

    @Override
    public void bindString(int index, String s) {
        statement.bindString(index, s);
    }

    @Override
    public void bindNull(int index) {
        statement.bindNull(index);
    }

    @Override
    public void bindLong(int index, long aLong) {
        statement.bindLong(index, aLong);
    }

    @Override
    public void bindDouble(int index, double aDouble) {
        statement.bindDouble(index, aDouble);
    }

    @Override
    public void bindBlob(int index, byte[] bytes) {
        statement.bindBlob(index, bytes);
    }
}
