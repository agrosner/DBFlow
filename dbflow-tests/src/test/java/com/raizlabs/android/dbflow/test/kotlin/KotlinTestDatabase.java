package com.raizlabs.android.dbflow.test.kotlin;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Description:
 */
@Database(name = KotlinTestDatabase.NAME, version = KotlinTestDatabase.VERSION)
public class KotlinTestDatabase {

    public static final String NAME = "KotlinTest";

    public static final int VERSION = 1;
}
