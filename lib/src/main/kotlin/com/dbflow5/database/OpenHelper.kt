package com.dbflow5.database

/**
 * Description: Abstracts out the [DatabaseHelperDelegate] into the one used in this library.
 */
interface OpenHelper {

    val database: DatabaseWrapper

    val delegate: DatabaseHelperDelegate?

    val isDatabaseIntegrityOk: Boolean

    fun setWriteAheadLoggingEnabled(enabled: Boolean)

    fun performRestoreFromBackup()

    fun backupDB()

    fun setDatabaseListener(callback: DatabaseCallback?)

    fun closeDB()

    fun deleteDB()
}

/**
 * Ignores if the underlying DB supports it or not.
 */
fun OpenHelper.trySetWriteAheadLoggingEnabled(enabled: Boolean) {
    try {
        setWriteAheadLoggingEnabled(enabled)
    } catch (e: UnsupportedOperationException) {
        // ignore
    }
}