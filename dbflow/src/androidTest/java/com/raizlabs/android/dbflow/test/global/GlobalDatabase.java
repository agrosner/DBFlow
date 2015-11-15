package com.raizlabs.android.dbflow.test.global;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Description:
 */
@Database(name = GlobalDatabase.NAME, version = GlobalDatabase.VERSION,
        insertConflict = ConflictAction.REPLACE,
        updateConflict = ConflictAction.REPLACE)
public class GlobalDatabase {

    public static final String NAME = "GlobalDatabase";

    public static final int VERSION = 1;
}
