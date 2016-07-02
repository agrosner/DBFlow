package com.raizlabs.android.dbflow.test.prepackaged;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Description:
 */
@Database(version = TestDB.VERSION, name = TestDB.NAME)
public class TestDB {

    public static final String NAME = "testdb";

    public static final int VERSION = 1;
}
