package com.dbflow5.database

interface OpenHelperDelegate {
    val database: DatabaseConnection
    val delegate: DatabaseHelperDelegate?

    val isDatabaseIntegrityOk: Boolean

    suspend fun performRestoreFromBackup()

    suspend fun backupDB()
}


/**
 * Description: Abstracts out the [DatabaseHelperDelegate] into the one used in this library.
 */
interface OpenHelper : OpenHelperDelegate {

    fun setWriteAheadLoggingEnabled(enabled: Boolean)

    fun setDatabaseListener(callback: DatabaseCallback?)

    fun close()

    fun delete()

    val isOpen: Boolean
        get() = database.isOpen
}

/**
 * Creates default open helper.
 */
@Suppress("FunctionName")
expect fun OpenHelper(db: GeneratedDatabase, callback: DatabaseCallback?): OpenHelper

