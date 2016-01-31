package com.raizlabs.android.dbflow.test.customseparator;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Description:
 */
@Database(name = CustomSeparatorDatabase.NAME, version = CustomSeparatorDatabase.VERSION, generatedClassSeparator = "$$")
public class CustomSeparatorDatabase {

    public static final String NAME = "Custom";

    public static final int VERSION = 1;
}
