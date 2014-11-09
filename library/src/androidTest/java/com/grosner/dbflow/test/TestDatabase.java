package com.grosner.dbflow.test;

import com.grosner.dbflow.annotation.Database;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Database(name = TestDatabase.NAME, version = 1, foreignKeysSupported = true)
public class TestDatabase {

    public static final String NAME = "Test";
}
