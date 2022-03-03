package com.dbflow5.database

import com.dbflow5.config.GeneratedDatabase
import java.sql.DriverManager

class JDBCOpenHelper(
    private val generatedDatabase: GeneratedDatabase,
    callback: DatabaseCallback?,
    private val databaseHelperDelegate: DatabaseHelperDelegate = DatabaseHelperDelegate(
        callback,
        generatedDatabase,
        helper = DatabaseHelper(
            JDBCMigrationFileHelper(), generatedDatabase
        ),
        databaseBackup = DatabaseBackup(generatedDatabase),
    ),
) : OpenHelper, OpenHelperDelegate by databaseHelperDelegate {

    private val connection by lazy {
        DriverManager.getConnection("jdbc:sqlite:${generatedDatabase.openHelperName ?: "memory:"}")
    }

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        connection.prepareStatement(
            if (enabled) {
                "PRAGMA journal_mode=WAL"
            } else {
                "PRAGMA journal_mode=DELETE"
            }
        ).execute()
    }

    override fun setDatabaseListener(callback: DatabaseCallback?) {
        databaseHelperDelegate.setDatabaseHelperListener(callback)
    }

    override fun closeDB() {
        connection.close()
    }

    override fun deleteDB() {
        connection.createStatement()
            .executeUpdate("DROP DATABASE ${generatedDatabase.databaseName}")
    }
}

actual fun OpenHelper(db: GeneratedDatabase, callback: DatabaseCallback?): OpenHelper = JDBCOpenHelper(db, callback)
