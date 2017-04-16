package com.raizlabs.android.dbflow.structure.database;

import android.database.sqlite.SQLiteStatement;
import android.support.annotation.Nullable;

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

    void bindString(int index, String s);

    void bindStringOrNull(int index, @Nullable String s);

    void bindNull(int index);

    void bindLong(int index, long aLong);

    void bindNumber(int index, @Nullable Number number);

    void bindNumberOrNull(int index, @Nullable Number number);

    void bindDouble(int index, double aDouble);

    void bindDoubleOrNull(int index, @Nullable Double aDouble);

    void bindBlob(int index, byte[] bytes);

    void bindBlobOrNull(int index, @Nullable byte[] bytes);

}
