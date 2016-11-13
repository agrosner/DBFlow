package com.raizlabs.android.dbflow.test;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Description:
 */
@Database(name = TestDatabase.NAME, version = 3, foreignKeyConstraintsEnforced = true)
public class TestDatabase {

    public static final String NAME = "Test";
}
