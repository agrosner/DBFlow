package com.raizlabs.android.dbflow.test.example;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Description:
 */
@Database(name = ColonyDatabase.NAME, version = ColonyDatabase.VERSION)
public class ColonyDatabase {

    public static final String NAME = "Colonies";

    public static final int VERSION = 1;
}
