package com.raizlabs.dbflow5.sqlcipher

import android.content.Context
import com.raizlabs.dbflow5.database.DBFlowDatabase
import com.raizlabs.dbflow5.config.DatabaseConfig
import com.raizlabs.dbflow5.database.*
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

/**
 * Description: The replacement [OpenHelper] for SQLCipher. Specify a subclass of this is [DatabaseConfig.getDatabaseClass]
 * of your database to get it to work with specifying the secret you use for the databaseForTable.
 */
abstract class SQLCipherOpenHelper(
    private val context: Context,
    databaseDefinition: DBFlowDatabase, listener: DatabaseCallback?)
    : SQLiteOpenHelper(context,
    if (databaseDefinition.isInMemory) null else databaseDefinition.databaseFileName,
    null, databaseDefinition.databaseVersion), OpenHelper {

    final override val delegate: DatabaseHelperDelegate
    private var cipherDatabase: SQLCipherDatabase? = null
    private val _databaseName = databaseDefinition.databaseFileName

    override val isDatabaseIntegrityOk: Boolean
        get() = delegate.isDatabaseIntegrityOk

    override val database: DatabaseWrapper
        get() {
            if (cipherDatabase == null || !cipherDatabase!!.database.isOpen) {
                cipherDatabase = SQLCipherDatabase.from(getWritableDatabase(cipherSecret))
            }
            return cipherDatabase!!
        }

    /**
     * @return The SQLCipher secret for opening this database.
     */
    protected abstract val cipherSecret: String

    init {
        SQLiteDatabase.loadLibs(context)

        val restoreHelper = PlatformDatabaseRestoreHelper(context)
        val migrationHelper = PlatformMigrationHelper(context, databaseDefinition)
        var backupHelper: OpenHelper? = null
        if (databaseDefinition.backupEnabled()) {
            // Temp database mirrors existing
            backupHelper = BackupHelper(migrationHelper,
                DatabaseHelperDelegate.getTempDbFileName(databaseDefinition),
                databaseDefinition.databaseVersion, databaseDefinition)
        }

        delegate = DatabaseHelperDelegate(migrationHelper, restoreHelper, listener, databaseDefinition, backupHelper)
    }

    override fun performRestoreFromBackup() {
        delegate.performRestoreFromBackup()
    }

    override fun backupDB() {
        delegate.backupDB()
    }

    /**
     * Set a listener to listen for specific DB events and perform an action before we execute this classes
     * specific methods.
     *
     * @param callback
     */
    override fun setDatabaseCallback(callback: DatabaseCallback?) {
        delegate.setDatabaseCallback(callback)
    }

    override fun onCreate(db: SQLiteDatabase) {
        delegate.onCreate(SQLCipherDatabase.from(db))
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        delegate.onUpgrade(SQLCipherDatabase.from(db), oldVersion, newVersion)
    }

    override fun onOpen(db: SQLiteDatabase) {
        delegate.onOpen(SQLCipherDatabase.from(db))
    }

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        throw UnsupportedOperationException("Not supported in SQLCipher")
    }

    override fun closeDB() {
        database
        cipherDatabase?.database?.close()
    }

    override fun deleteDB() {
        context.deleteDatabase(_databaseName)
    }

    /**
     * Simple helper to manage backup.
     */
    private inner class BackupHelper(migrationHelper: MigrationHelper,
                                     name: String,
                                     version: Int,
                                     databaseDefinition: DBFlowDatabase)
        : SQLiteOpenHelper(context, name, null, version), OpenHelper {

        private var sqlCipherDatabase: SQLCipherDatabase? = null
        private val baseDatabaseHelper: BaseDatabaseHelper = BaseDatabaseHelper(migrationHelper, databaseDefinition)
        private val _databaseName = databaseDefinition.databaseFileName

        override val database: DatabaseWrapper
            get() {
                if (sqlCipherDatabase == null) {
                    sqlCipherDatabase = SQLCipherDatabase.from(getWritableDatabase(cipherSecret))
                }
                return sqlCipherDatabase!!
            }

        override val delegate: DatabaseHelperDelegate?
            get() = null

        override val isDatabaseIntegrityOk: Boolean
            get() = false

        override fun performRestoreFromBackup() = Unit

        override fun backupDB() = Unit

        override fun closeDB() = Unit

        override fun setDatabaseCallback(callback: DatabaseCallback?) = Unit

        override fun onCreate(db: SQLiteDatabase) {
            baseDatabaseHelper.onCreate(SQLCipherDatabase.from(db))
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            baseDatabaseHelper.onUpgrade(SQLCipherDatabase.from(db), oldVersion, newVersion)
        }

        override fun onOpen(db: SQLiteDatabase) {
            baseDatabaseHelper.onOpen(SQLCipherDatabase.from(db))
        }

        override fun setWriteAheadLoggingEnabled(enabled: Boolean) = Unit

        override fun deleteDB() {
            context.deleteDatabase(_databaseName)
        }
    }

}
