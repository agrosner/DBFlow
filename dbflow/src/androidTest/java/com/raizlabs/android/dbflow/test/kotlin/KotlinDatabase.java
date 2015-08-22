package com.raizlabs.android.dbflow.test.kotlin;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Description: Shows example for support for Kotlin
 */
@Database(version = KotlinDatabase.VERSION, name = KotlinDatabase.NAME,
        generatedClassSeparator = "_")
public class KotlinDatabase {

    public static final String NAME = "KotlinDatabase";

    public static final int VERSION = 1;
}
