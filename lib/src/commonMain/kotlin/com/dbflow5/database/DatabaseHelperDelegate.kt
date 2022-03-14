package com.dbflow5.database

import com.dbflow5.config.FlowLog
import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.mpp.use

/**
 * Description: An abstraction from some parts of the Android SQLiteOpenHelper where this can be
 * used in other helper class definitions.
 */
class DatabaseHelperDelegate(
    private var databaseCallback: DatabaseCallback?,
    private val generatedDatabase: GeneratedDatabase,
    private val helper: DatabaseHelper,
    private val databaseBackup: DatabaseBackup,
) : DatabaseCallback, OpenHelperDelegate {

    /**
     * Pulled partially from code, it runs a "PRAGMA quick_check(1)" to see if the database is ok.
     * This method will [.restoreBackUp] if they are enabled on the database if this check fails. So
     * use with caution and ensure that you backup the database often!
     *
     * @return true if the database is ok, false if the consistency has been compromised.
     */
    override val isDatabaseIntegrityOk: Boolean
        get() = isDatabaseIntegrityOk(database)

    override val database: DatabaseConnection
        get() = generatedDatabase

    override suspend fun performRestoreFromBackup() {
        movePrepackagedDB(
            generatedDatabase.databaseFileName, generatedDatabase.databaseFileName
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

    override fun onConfigure(db: DatabaseConnection) {
        databaseCallback?.onConfigure(db)
        helper.onConfigure(db)
    }

    override fun onCreate(db: DatabaseConnection) {
        databaseCallback?.onCreate(db)
        helper.onCreate(db)
    }

    override fun onUpgrade(db: DatabaseConnection, oldVersion: Int, newVersion: Int) {
        databaseCallback?.onUpgrade(db, oldVersion, newVersion)
        helper.onUpgrade(db, oldVersion, newVersion)
    }

    override fun onOpen(db: DatabaseConnection) {
        databaseCallback?.onOpen(db)
        helper.onOpen(db)
    }

    override fun onDowngrade(db: DatabaseConnection, oldVersion: Int, newVersion: Int) {
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
        databaseBackup.movePrepackaged(databaseName, prepackagedName)
    }


    /**
     * Will use the already existing app database if [DBFlowDatabase.getBackupEnabled] is true. If the existing
     * is not there we will try to use the prepackaged database for that purpose.
     *
     * @param databaseName    The name of the database to restore
     * @param prepackagedName The name of the prepackaged db file
     */
    fun restoreDatabase(databaseName: String, prepackagedName: String) {
        databaseBackup.restoreDatabase(databaseName, prepackagedName)
    }

    /**
     * Pulled partially from code, it runs a "PRAGMA quick_check(1)" to see if the database is ok.
     *
     * @return true if the database is ok, false if the consistency has been compromised.
     */
    fun isDatabaseIntegrityOk(databaseConnection: DatabaseConnection): Boolean {
        return databaseConnection.compileStatement("PRAGMA quick_check(1)").use { statement ->
            val result = statement.simpleQueryForString()
            if (result == null || !result.equals("ok", ignoreCase = true)) {
                // integrity_checker failed on main or attached databases
                FlowLog.log(
                    FlowLog.Level.E, "PRAGMA integrity_check on ${generatedDatabase.databaseName} returned: $result"
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
        return databaseBackup.restoreBackup()
    }

    /**
     * Saves the database as a backup on the [DefaultTransactionQueue].
     * This will create a THIRD database to use as a backup to the backup in case somehow the overwrite fails.
     */
    override suspend fun backupDB() {
        generatedDatabase.beginTransactionAsync {
            databaseBackup.backupDB()
        }.execute()
    }
}
