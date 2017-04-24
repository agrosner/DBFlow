package com.raizlabs.android.dbflow.structure.database;

import android.support.annotation.Nullable;

/**
 * Description: Default implementation for some {@link DatabaseStatement} methods.
 */
public abstract class BaseDatabaseStatement implements DatabaseStatement {

    @Override
    public void bindStringOrNull(int index, @Nullable String s) {
        if (s != null) {
            bindString(index, s);
        } else {
            bindNull(index);
        }
    }

    @Override
    public void bindNumber(int index, @Nullable Number number) {
        bindNumberOrNull(index, number);
    }

    @Override
    public void bindNumberOrNull(int index, @Nullable Number number) {
        if (number != null) {
            bindLong(index, number.longValue());
        } else {
            bindNull(index);
        }
    }

    @Override
    public void bindDoubleOrNull(int index, @Nullable Double aDouble) {
        if (aDouble != null) {
            bindDouble(index, aDouble);
        } else {
            bindNull(index);
        }
    }

    @Override
    public void bindBlobOrNull(int index, @Nullable byte[] bytes) {
        if (bytes != null) {
            bindBlob(index, bytes);
        } else {
            bindNull(index);
        }
    }
}
