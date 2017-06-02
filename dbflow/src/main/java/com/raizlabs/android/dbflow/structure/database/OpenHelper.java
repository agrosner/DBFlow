package com.raizlabs.android.dbflow.structure.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Description: Abstracts out the {@link DatabaseHelperDelegate} into the one used in this library.
 */
public interface OpenHelper {

    void performRestoreFromBackup();

    @NonNull
    DatabaseWrapper getDatabase();

    @Nullable
    DatabaseHelperDelegate getDelegate();

    boolean isDatabaseIntegrityOk();

    void backupDB();

    void setDatabaseListener(@Nullable DatabaseHelperListener helperListener);

    void closeDB();
}
