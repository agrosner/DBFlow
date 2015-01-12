package com.raizlabs.android.dbflow;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Description:
 */
@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION, foreignKeysSupported = true)
public class AppDatabase {

    public static final String NAME = "App";

    public static final int VERSION = 1;
}
