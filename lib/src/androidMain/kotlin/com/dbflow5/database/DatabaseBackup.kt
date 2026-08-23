package com.dbflow5.database

import android.content.Context
import com.dbflow5.config.FlowLog
import java.io.FileInputStream
import java.io.IOException

actual class DatabaseBackup(
    private val context: Context,
    private val generatedDatabase: GeneratedDatabase,
    private val databaseWriter: DatabaseWriter = DatabaseWriter(),
) {

    /**
     * @return the temporary database file name for when we have backups enabled
     * [DBFlowDatabase.getBackupEnabled]
     */
    private val tempDbFileName: String
        get() = getTempDbFileName(generatedDatabase)

    private fun moveDB(databaseName: String, tempDBName: String, prepackagedName: String) {
        val dbPath = context.getDatabasePath(databaseName)

        // If the database already exists, and is ok return
        if (dbPath.exists()) {
            return
        }

        // Make sure we have a path to the file
        dbPath.parentFile.mkdirs()

        // Try to copy database file
        try {
            // check existing and use that as backup
            val existingDb = context.getDatabasePath(tempDBName)
            // if it exists and the integrity is ok we use backup as the main DB is no longer valid
            val inputStream = if (existingDb.exists()) {
                FileInputStream(existingDb)
            } else {
                context.assets.open(prepackagedName)
            }
            databaseWriter.write(dbPath, inputStream)
        } catch (e: IOException) {
            FlowLog.log(FlowLog.Level.W, "Failed to open file", throwable = e)
        }
    }


    actual fun movePrepackaged(databaseName: String, prepackagedName: String) {
        moveDB(databaseName, tempDbFileName, prepackagedName)
    }

    actual fun restoreDatabase(databaseName: String, prepackagedName: String) {
        moveDB(databaseName, generatedDatabase.databaseFileName, prepackagedName)
    }

    actual fun restoreBackup(): Boolean {
        var success = true

        val db = context.getDatabasePath(tempDbFileName)
        val corrupt = context.getDatabasePath(generatedDatabase.databaseName)
        if (corrupt.delete()) {
            try {
                databaseWriter.write(corrupt, FileInputStream(db))
            } catch (e: IOException) {
                FlowLog.logError(e)
                success = false
            }

        } else {
            FlowLog.log(FlowLog.Level.E, "Failed to delete DB")
        }
        return success
    }

    actual fun backupDB() {
        val backup = context.getDatabasePath(tempDbFileName)
        val temp =
            context.getDatabasePath("${TEMP_DB_NAME}-2-${generatedDatabase.databaseFileName}")

        // if exists we want to delete it before rename
        if (temp.exists()) {
            temp.delete()
        }

        backup.renameTo(temp)
        if (backup.exists()) {
            backup.delete()
        }
        val existing = context.getDatabasePath(generatedDatabase.databaseFileName)

        try {
            backup.parentFile.mkdirs()
            databaseWriter.write(backup, FileInputStream(existing))
            temp.delete()
        } catch (e: Exception) {
            FlowLog.logError(e)

        }
    }
}

