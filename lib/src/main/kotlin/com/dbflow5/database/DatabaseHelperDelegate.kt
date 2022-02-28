package com.dbflow5.database

import android.content.Context
import com.dbflow5.config.FlowLog
import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.config.beginTransactionAsync
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Description: An abstraction from some parts of the Android SQLiteOpenHelper where this can be
 * used in other helper class definitions.
 */
class DatabaseHelperDelegate(
    private val context: Context,
    private var databaseCallback: DatabaseCallback?,
    private val generatedDatabase: GeneratedDatabase,
    private val helper: DatabaseHelper = DatabaseHelper(
        AndroidMigrationFileHelper(context),
        generatedDatabase
    )
) : DatabaseCallback, OpenHelperDelegate {

    /**
     * @return the temporary database file name for when we have backups enabled
     * [DBFlowDatabase.getBackupEnabled]
     */
    private val tempDbFileName: String
        get() = getTempDbFileName(generatedDatabase)

    /**
     * Pulled partially from code, it runs a "PRAGMA quick_check(1)" to see if the database is ok.
     * This method will [.restoreBackUp] if they are enabled on the database if this check fails. So
     * use with caution and ensure that you backup the database often!
     *
     * @return true if the database is ok, false if the consistency has been compromised.
     */
    override val isDatabaseIntegrityOk: Boolean
        get() = isDatabaseIntegrityOk(database)

    override val database: DatabaseWrapper
        get() = generatedDatabase

    override suspend fun performRestoreFromBackup() {
        movePrepackagedDB(
            generatedDatabase.databaseFileName,
            generatedDatabase.databaseFileName
        )
    }

    override val delegate: DatabaseHelperDelegate = this

    /**
     * @param databaseCallback Listens for operations the DB and allow you to provide extra
     * functionality.
     */
    fun setDatabaseHelperListener(databaseCallback: DatabaseCallback?) {
        this.databaseCallback = databaseCallback
    }

    override fun onConfigure(db: DatabaseWrapper) {
        databaseCallback?.onConfigure(db)
        helper.onConfigure(db)
    }

    override fun onCreate(db: DatabaseWrapper) {
        databaseCallback?.onCreate(db)
        helper.onCreate(db)
    }

    override fun onUpgrade(db: DatabaseWrapper, oldVersion: Int, newVersion: Int) {
        databaseCallback?.onUpgrade(db, oldVersion, newVersion)
        helper.onUpgrade(db, oldVersion, newVersion)
    }

    override fun onOpen(db: DatabaseWrapper) {
        databaseCallback?.onOpen(db)
        helper.onOpen(db)
    }

    override fun onDowngrade(db: DatabaseWrapper, oldVersion: Int, newVersion: Int) {
        databaseCallback?.onDowngrade(db, oldVersion, newVersion)
        helper.onDowngrade(db, oldVersion, newVersion)
    }

    /**
     * Copies over the prepackaged DB into the main DB then deletes the existing DB to save storage space. If
     * we have a backup that exists
     *
     * @param databaseName    The name of the database to copy over
     * @param prepackagedName The name of the prepackaged db file
     */
    fun movePrepackagedDB(databaseName: String, prepackagedName: String) {
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


    /**
     * Will use the already existing app database if [DBFlowDatabase.getBackupEnabled] is true. If the existing
     * is not there we will try to use the prepackaged database for that purpose.
     *
     * @param databaseName    The name of the database to restore
     * @param prepackagedName The name of the prepackaged db file
     */
    fun restoreDatabase(databaseName: String, prepackagedName: String) {
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

    /**
     * Pulled partially from code, it runs a "PRAGMA quick_check(1)" to see if the database is ok.
     *
     * @return true if the database is ok, false if the consistency has been compromised.
     */
    fun isDatabaseIntegrityOk(databaseWrapper: DatabaseWrapper): Boolean {
        return databaseWrapper.compileStatement("PRAGMA quick_check(1)").use { statement ->
            val result = statement.simpleQueryForString()
            if (result == null || !result.equals("ok", ignoreCase = true)) {
                // integrity_checker failed on main or attached databases
                FlowLog.log(
                    FlowLog.Level.E,
                    "PRAGMA integrity_check on ${generatedDatabase.databaseName} returned: $result"
                )
                false
            } else {
                true
            }
        }
    }

    /**
     * If integrity check fails, this method will use the backup db to fix itself. In order to prevent
     * loss of data, please backup often!
     */
    fun restoreBackUp(): Boolean {
        var success = true

        val db = context.getDatabasePath(TEMP_DB_NAME + generatedDatabase.databaseName)
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

    /**
     * Saves the database as a backup on the [DefaultTransactionQueue].
     * This will create a THIRD database to use as a backup to the backup in case somehow the overwrite fails.
     */
    override suspend fun backupDB() {
        generatedDatabase.beginTransactionAsync {
            val backup = context.getDatabasePath(tempDbFileName)
            val temp =
                context.getDatabasePath("$TEMP_DB_NAME-2-${generatedDatabase.databaseFileName}")

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
        }.execute()
    }

    companion object {

        val TEMP_DB_NAME = "temp-"

        fun getTempDbFileName(databaseDefinition: GeneratedDatabase): String =
            "$TEMP_DB_NAME${databaseDefinition.databaseName}.db"
    }
}
