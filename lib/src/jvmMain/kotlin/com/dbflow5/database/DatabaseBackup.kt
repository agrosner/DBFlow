package com.dbflow5.database

import com.dbflow5.config.FlowLog
import com.dbflow5.config.GeneratedDatabase
import java.io.FileInputStream
import java.io.IOException

actual class DatabaseBackup(
    private val generatedDatabase: GeneratedDatabase,
    private val databaseWriter: DatabaseWriter = DatabaseWriter(),
) {

    actual fun movePrepackaged(databaseName: String, prepackagedName: String) {
        val database = resourceFile<DatabaseBackup>(databaseName)
            ?: run {
                // TODO: where do we find existing database file connection?
                FlowLog.log(FlowLog.Level.W, "DatabaseBackup", "Could not retrieve file for $databaseName")
                //throw IllegalStateException("Could not retrieve file for $databaseName")
                return
            }
        if (database.exists()) {
            // if file already exists, skip
            return
        }
        try {
            val prepackaged = resourceFile<DatabaseBackup>(prepackagedName)
            val existingDB = resourceFile<DatabaseBackup>(getTempDbFileName(generatedDatabase))
            when {
                existingDB != null && existingDB.exists() -> FileInputStream(existingDB)
                prepackaged != null -> FileInputStream(prepackaged)
                else -> null
            }?.let { databaseWriter.write(database, it) }
        } catch (e: IOException) {
            FlowLog.log(FlowLog.Level.W, "Failed to open file", throwable = e)
        }
    }

    actual fun restoreDatabase(databaseName: String, prepackagedName: String) {
    }

    actual fun restoreBackup(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun backupDB() {
    }
}
