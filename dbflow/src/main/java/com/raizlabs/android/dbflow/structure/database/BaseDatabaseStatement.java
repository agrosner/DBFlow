package com.raizlabs.android.dbflow.structure.database;

import android.support.annotation.Nullable;

/**
 * Description: Default implementation for some {@link DatabaseStatement} methods.
 */
public abstract class BaseDatabaseStatement implements DatabaseStatement {

    @Override
    public void bindStringOrNull(int index, @Nullable String name) {
        if (name != null) {
            bindString(index, name);
        } else {
            bindNull(index);
        }
    }

    @Override
    public void bindNumberOrNull(int index, @Nullable Number name) {
        if (name != null) {
            bindLong(index, name.longValue());
        } else {
            bindNull(index);
        }
    }

    @Override
    public void bindDoubleOrNull(int index, @Nullable Double name) {
        if (name != null) {
            bindDouble(index, name);
        } else {
            bindNull(index);
        }
    }

}
