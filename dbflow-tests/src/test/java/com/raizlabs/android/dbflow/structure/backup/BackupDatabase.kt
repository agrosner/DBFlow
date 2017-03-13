package com.raizlabs.android.dbflow.structure.backup

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description:
 */
@Database(name = BackupDatabase.NAME, version = BackupDatabase.VERSION,
    backupEnabled = true, consistencyCheckEnabled = true)
object BackupDatabase {

    const val NAME = "BackupDB"

    const val VERSION = 1
}
