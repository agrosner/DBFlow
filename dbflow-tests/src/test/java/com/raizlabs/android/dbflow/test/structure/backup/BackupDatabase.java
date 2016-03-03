package com.raizlabs.android.dbflow.test.structure.backup;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Description:
 */
@Database(name = BackupDatabase.NAME, version = BackupDatabase.VERSION, backupEnabled = true)
public class BackupDatabase {

    public static final String NAME = "BackupDB";

    public static final int VERSION = 1;
}
