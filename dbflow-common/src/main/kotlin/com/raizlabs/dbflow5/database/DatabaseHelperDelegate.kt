package com.raizlabs.dbflow5.database

import com.raizlabs.dbflow5.transaction.DefaultTransactionQueue

/**
 * Description: An abstraction from some parts of the Android SQLiteOpenHelper where this can be
 * used in other helper class definitions.
 */
class DatabaseHelperDelegate(
    migrationHelper: MigrationHelper,
    private val databaseRestoreHelper: DatabaseRestoreHelper,
    private var databaseCallback: DatabaseCallback?,
    databaseDefinition: DBFlowDatabase,
    private val backupHelper: OpenHelper?)
    : BaseDatabaseHelper(migrationHelper, databaseDefinition) {

    /**
     * @return the temporary database file name for when we have backups enabled
     * [DBFlowDatabase.backupEnabled]
     */
    private val tempDbFileName: String
        get() = getTempDbFileName(database)

    /**
     * Pulled partially from code, it runs a "PRAGMA quick_check(1)" to see if the database is ok.
     * This method will [.restoreBackUp] if they are enabled on the database if this check fails. So
     * use with caution and ensure that you backup the database often!
     *
     * @return true if the database is ok, false if the consistency has been compromised.
     */
    val isDatabaseIntegrityOk: Boolean
        get() = isDatabaseIntegrityOk(writableDatabase)

    val writableDatabase: DatabaseWrapper
        get() = database

    fun performRestoreFromBackup() {
        movePrepackagedDB(database.databaseFileName,
            database.databaseFileName)

        if (database.backupEnabled()) {
            if (backupHelper == null) {
                throw IllegalStateException("the passed backup helper was null, even though backup" +
                    " is enabled. Ensure that its passed in.")
            }
            restoreDatabase(tempDbFileName, database.databaseFileName)
            backupHelper.database
        }
    }

    /**
     * @param databaseCallback Listens for operations the DB and allow you to provide extra
     * functionality.
     */
    fun setDatabaseCallback(databaseCallback: DatabaseCallback?) {
        this.databaseCallback = databaseCallback
    }

    override fun onCreate(db: DatabaseWrapper) {
        databaseCallback?.onCreate(db)
        super.onCreate(db)
    }

    override fun onUpgrade(db: DatabaseWrapper, oldVersion: Int, newVersion: Int) {
        databaseCallback?.onUpgrade(db, oldVersion, newVersion)
        super.onUpgrade(db, oldVersion, newVersion)
    }

    override fun onOpen(db: DatabaseWrapper) {
        databaseCallback?.onOpen(db)
        super.onOpen(db)
    }

    override fun onDowngrade(db: DatabaseWrapper, oldVersion: Int, newVersion: Int) {
        databaseCallback?.onDowngrade(db, oldVersion, newVersion)
        super.onDowngrade(db, oldVersion, newVersion)
    }

    fun movePrepackagedDB(databaseName: String, prepackagedName: String) {
        databaseRestoreHelper.movePrepackagedDatabase(
            dbFlowDatabase = database,
            backupHelper = backupHelper,
            tempDbFileName = tempDbFileName,
            databaseName = databaseName,
            prepackagedName = prepackagedName
        )
    }

    fun isDatabaseIntegrityOk(databaseWrapper: DatabaseWrapper): Boolean {
        return databaseWrapper.isDatabaseIntegrityOk {
            if (database.backupEnabled()) restoreBackUp() else false
        }
    }

    /**
     * If integrity check fails, this method will use the backup db to fix itself. In order to prevent
     * loss of data, please backup often!
     */
    fun restoreBackUp(): Boolean = databaseRestoreHelper.restoreBackUp(database)

    fun restoreDatabase(databaseName: String, prepackagedName: String) {
        databaseRestoreHelper.restoreDatabase(
            dbFlowDatabase = database,
            backupHelper = backupHelper,
            databaseName = databaseName,
            prepackagedName = prepackagedName
        )
    }

    /**
     * Saves the database as a backup on the [DefaultTransactionQueue].
     * This will create a THIRD database to use as a backup to the backup in case somehow the overwrite fails.
     */
    fun backupDB() {
        if (!database.backupEnabled() || !database.areConsistencyChecksEnabled()) {
            throw IllegalStateException("Backups are not enabled for : " +
                "${database.databaseName}. Please consider adding both backupEnabled " +
                "and consistency checks enabled to the Database annotation")
        }

        database.executeTransactionAsync({
            databaseRestoreHelper.backupDatabase(database, tempDbFileName)
        })

    }

    companion object {

        const val TEMP_DB_NAME = "temp-"

        fun getTempDbFileName(databaseDefinition: DBFlowDatabase): String =
            "$TEMP_DB_NAME${databaseDefinition.databaseName}.db"
    }
}
