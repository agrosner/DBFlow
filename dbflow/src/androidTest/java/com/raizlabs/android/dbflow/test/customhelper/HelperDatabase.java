package com.raizlabs.android.dbflow.test.customhelper;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Description:
 */
@Database(name = HelperDatabase.NAME, version = HelperDatabase.VERSION,
        sqlHelperClass = CustomOpenHelper.class)
public class HelperDatabase {

    public static final String NAME = "Helper";

    public static final int VERSION = 1;
}
