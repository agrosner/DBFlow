package com.raizlabs.android.dbflow.test.structure.backup

import com.raizlabs.android.dbflow.annotation.Database

/**
 * Description:
 */
@Database(name = BackupDatabase.NAME, version = BackupDatabase.VERSION, backupEnabled = true, consistencyCheckEnabled = true)
object BackupDatabase {

    val NAME = "BackupDB"

    val VERSION = 1
}
