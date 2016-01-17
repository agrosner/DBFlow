package com.raizlabs.android.dbflow.structure.database;

import android.database.sqlite.SQLiteOpenHelper;

/**
 * Description: Abstracts out the {@link SQLiteOpenHelper} into the one used in this library.
 */
public interface OpenHelper {

    DatabaseWrapper getDatabase();

    DatabaseHelperDelegate getDelegate();

    boolean isDatabaseIntegrityOk();

    void backupDB();
}
