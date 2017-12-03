package com.raizlabs.dbflow5.database

import com.raizlabs.dbflow5.database.DatabaseHelperDelegate
import com.raizlabs.dbflow5.database.DatabaseHelperListener
import com.raizlabs.dbflow5.database.DatabaseWrapper

/**
 * Description: Abstracts out the [DatabaseHelperDelegate] into the one used in this library.
 */
interface OpenHelper {

    val database: DatabaseWrapper

    val delegate: DatabaseHelperDelegate?

    val isDatabaseIntegrityOk: Boolean

    fun performRestoreFromBackup()

    fun backupDB()

    fun setDatabaseListener(helperListener: DatabaseHelperListener?)

    fun closeDB()
}
