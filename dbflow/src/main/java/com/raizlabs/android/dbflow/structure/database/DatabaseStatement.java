package com.raizlabs.android.dbflow.structure.database;

import android.database.sqlite.SQLiteStatement;

/**
 * Description: Abstracts out a {@link SQLiteStatement}.
 */
public interface DatabaseStatement {

    long executeUpdateDelete();

    void execute();

    void close();

    long simpleQueryForLong();

    String simpleQueryForString();

    long executeInsert();

    void bindString(int index, String name);

    void bindNull(int index);

    void bindLong(int index, long aLong);

    void bindDouble(int index, double aDouble);

    void bindBlob(int index, byte[] bytes);
}
