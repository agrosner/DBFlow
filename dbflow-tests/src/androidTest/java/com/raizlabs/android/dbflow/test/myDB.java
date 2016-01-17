package com.raizlabs.android.dbflow.test;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Description:
 */
@Database(name = myDB.NAME, version = myDB.VERSION)
public class myDB {
    public static final String NAME = "test";
    public static final int VERSION = 1;
}
