package com.raizlabs.dbflow5.database

import android.content.Context
import com.raizlabs.dbflow5.config.DBFlowDatabase
import com.raizlabs.dbflow5.config.FlowLog
import java.io.*

actual class PlatformDatabaseRestoreHelper(private val context: Context) : DatabaseRestoreHelper {

    override fun movePrepackagedDatabase(dbFlowDatabase: DBFlowDatabase,
                                         backupHelper: OpenHelper?,
                                         tempDbFileName: String,
                                         databaseName: String,
                                         prepackagedName: String) {
        val dbPath = context.getDatabasePath(databaseName)

        // If the database already exists, and is ok return
        if (dbPath.exists() && (!dbFlowDatabase.areConsistencyChecksEnabled()
                || dbFlowDatabase.areConsistencyChecksEnabled()
                && backupHelper != null
                && backupHelper.database.isDatabaseIntegrityOk())) {
            return
        }

        // Make sure we have a path to the file
        dbPath.parentFile.mkdirs()

        // Try to copy database file
        try {
            // check existing and use that as backup
            val existingDb = context.getDatabasePath(tempDbFileName)
            val inputStream: InputStream
            // if it exists and the integrity is ok we use backup as the main DB is no longer valid
            inputStream = if (existingDb.exists()
                && (!dbFlowDatabase.backupEnabled() ||
                    (dbFlowDatabase.backupEnabled()
                        && backupHelper != null
                        && backupHelper.database.isDatabaseIntegrityOk()))) {
                FileInputStream(existingDb)
            } else {
                context.assets.open(prepackagedName)
            }
            writeDB(dbPath, inputStream)
        } catch (e: IOException) {
            FlowLog.log(FlowLog.Level.W, "Failed to open file", e)
        }
    }

    override fun restoreBackUp(dbFlowDatabase: DBFlowDatabase): Boolean {
        var success = true
        val db = context.getDatabasePath(DatabaseHelperDelegate.TEMP_DB_NAME + dbFlowDatabase.databaseName)
        val corrupt = context.getDatabasePath(dbFlowDatabase.databaseName)
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

    override fun restoreDatabase(dbFlowDatabase: DBFlowDatabase,
                                 backupHelper: OpenHelper?,
                                 databaseName: String,
                                 prepackagedName: String) {
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
            val existingDb = context.getDatabasePath(dbFlowDatabase.databaseFileName)
            val inputStream: InputStream
            // if it exists and the integrity is ok
            inputStream = if (existingDb.exists()
                && (dbFlowDatabase.backupEnabled() && backupHelper != null
                    && backupHelper.database.isDatabaseIntegrityOk())) {
                FileInputStream(existingDb)
            } else {
                context.assets.open(prepackagedName)
            }
            writeDB(dbPath, inputStream)
        } catch (e: IOException) {
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

    override fun backupDatabase(dbFlowDatabase: DBFlowDatabase, tempDbFileName: String) {
        val backup = context.getDatabasePath(tempDbFileName)
        val temp = context.getDatabasePath("${DatabaseHelperDelegate.TEMP_DB_NAME}-2-${dbFlowDatabase.databaseFileName}")

        // if exists we want to delete it before rename
        if (temp.exists()) {
            temp.delete()
        }

        backup.renameTo(temp)
        if (backup.exists()) {
            backup.delete()
        }
        val existing = context.getDatabasePath(dbFlowDatabase.databaseFileName)

        try {
            backup.parentFile.mkdirs()
            writeDB(backup, FileInputStream(existing))
            temp.delete()
        } catch (e: Exception) {
            FlowLog.logError(e)

        }
    }
}
