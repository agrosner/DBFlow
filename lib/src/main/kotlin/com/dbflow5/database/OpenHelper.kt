package com.dbflow5.database

interface OpenHelperDelegate {
    val database: DatabaseWrapper

    val delegate: DatabaseHelperDelegate?

    val isDatabaseIntegrityOk: Boolean

    fun performRestoreFromBackup()

    fun backupDB()
}


/**
 * Description: Abstracts out the [DatabaseHelperDelegate] into the one used in this library.
 */
interface OpenHelper : OpenHelperDelegate {

    fun setWriteAheadLoggingEnabled(enabled: Boolean)

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