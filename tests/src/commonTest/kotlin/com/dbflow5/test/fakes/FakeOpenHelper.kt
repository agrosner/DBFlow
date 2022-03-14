package com.dbflow5.test.fakes

import com.dbflow5.database.DatabaseCallback
import com.dbflow5.database.DatabaseHelperDelegate
import com.dbflow5.database.DatabaseConnection
import com.dbflow5.database.OpenHelper

class FakeOpenHelper : OpenHelper {
    override val database: DatabaseConnection
        get() = TODO("Not yet implemented")
    override val delegate: DatabaseHelperDelegate?
        get() = TODO("Not yet implemented")
    override val isDatabaseIntegrityOk: Boolean
        get() = TODO("Not yet implemented")

    override suspend fun performRestoreFromBackup() {
    }

    override suspend fun backupDB() {
    }

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
    }

    override fun setDatabaseListener(callback: DatabaseCallback?) {
    }

    override fun close() {
    }

    override fun delete() {
    }
}