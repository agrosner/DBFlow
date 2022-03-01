package com.dbflow5.database

import android.content.Context
import com.dbflow5.config.FlowLog
import com.dbflow5.config.GeneratedDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

actual class DatabaseBackup(
    private val context: Context,
    private val generatedDatabase: GeneratedDatabase,
) {

    /**
     * @return the temporary database file name for when we have backups enabled
     * [DBFlowDatabase.getBackupEnabled]
     */
    private val tempDbFileName: String
        get() = getTempDbFileName(generatedDatabase)


    actual fun movePrepackaged(databaseName: String, prepackagedName: String) {
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
            val existingDb = context.getDatabasePath(tempDbFileName)
            // if it exists and the integrity is ok we use backup as the main DB is no longer valid
            val inputStream = if (existingDb.exists()) {
                FileInputStream(existingDb)
            } else {
                context.assets.open(prepackagedName)
            }
            writeDB(dbPath, inputStream)
        } catch (e: IOException) {
            FlowLog.log(FlowLog.Level.W, "Failed to open file", e)
        }
    }

    actual fun restoreDatabase(databaseName: String, prepackagedName: String) {
        val dbPath = context.getDatabasePath(databaseName)

        // If the database already exists, return
        if (dbPath.exists()) {
            return
        }

        // Make sure we have a path to the file
        dbPath.parentFile.mkdirs()

        // Try to copy database file
        try {
            // check existing and use that as backup
            val existingDb = context.getDatabasePath(generatedDatabase.databaseFileName)
            // if it exists and the integrity is ok
            val inputStream = if (existingDb.exists()) {
                FileInputStream(existingDb)
            } else {
                context.assets.open(prepackagedName)
            }
            writeDB(dbPath, inputStream)
        } catch (e: IOException) {
            FlowLog.logError(e)
        }

    }

    actual fun restoreBackup(): Boolean {
        var success = true

        val db = context.getDatabasePath(tempDbFileName)
        val corrupt = context.getDatabasePath(generatedDatabase.databaseName)
        if (corrupt.delete()) {
            try {
                writeDB(corrupt, FileInputStream(db))
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
            writeDB(backup, FileInputStream(existing))
            temp.delete()
        } catch (e: Exception) {
            FlowLog.logError(e)

        }
    }

    /**
     * Writes the [InputStream] of the existing db to the file specified.
     *
     * @param dbPath     The file to write to.
     * @param existingDB The existing database file's input stream¬
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeDB(dbPath: File, existingDB: InputStream) {
        val output = FileOutputStream(dbPath)

        val buffer = ByteArray(1024)
        var length: Int = existingDB.read(buffer)
        while (length > 0) {
            output.write(buffer, 0, length)
            length = existingDB.read(buffer)
        }

        output.flush()
        output.close()
        existingDB.close()
    }

}
