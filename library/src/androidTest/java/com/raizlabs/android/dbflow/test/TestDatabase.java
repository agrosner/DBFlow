package com.raizlabs.android.dbflow.test;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Database(name = TestDatabase.NAME, version = 3, foreignKeysSupported = true)
public class TestDatabase {

    public static final String NAME = "Test";
}
