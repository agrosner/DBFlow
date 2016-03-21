package com.raizlabs.android.dbflow.test.sqlcipher;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Description:
 */
@Database(name = CipherDatabase.NAME, version = CipherDatabase.VERSION)
public class CipherDatabase {

    public static final String NAME = "CipherDatabase";
    public static final int VERSION = 1;
}
