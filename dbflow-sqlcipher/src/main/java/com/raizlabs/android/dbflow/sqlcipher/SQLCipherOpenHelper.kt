package com.raizlabs.android.dbflow.sqlcipher

import android.content.Context

import com.raizlabs.android.dbflow.config.DatabaseConfig
import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.database.BaseDatabaseHelper
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperDelegate
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.OpenHelper

import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

/**
 * Description: The replacement [OpenHelper] for SQLCipher. Specify a subclass of this is [DatabaseConfig.getDatabaseClass]
 * of your database to get it to work with specifying the secret you use for the databaseForTable.
 */
abstract class SQLCipherOpenHelper(databaseDefinition: DatabaseDefinition, listener: DatabaseHelperListener?)
    : SQLiteOpenHelper(FlowManager.context, if (databaseDefinition.isInMemory) null else databaseDefinition.databaseFileName, null, databaseDefinition.databaseVersion), OpenHelper {

    final override val delegate: DatabaseHelperDelegate
    private var cipherDatabase: SQLCipherDatabase? = null

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
        SQLiteDatabase.loadLibs(FlowManager.context)

        var backupHelper: OpenHelper? = null
        if (databaseDefinition.backupEnabled()) {
            // Temp database mirrors existing
            backupHelper = BackupHelper(FlowManager.context,
                    DatabaseHelperDelegate.getTempDbFileName(databaseDefinition),
                    databaseDefinition.databaseVersion, databaseDefinition)
        }

        delegate = DatabaseHelperDelegate(listener, databaseDefinition, backupHelper)
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
     * @param helperListener
     */
    override fun setDatabaseListener(helperListener: DatabaseHelperListener?) {
        delegate.setDatabaseHelperListener(helperListener)
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

    override fun closeDB() {
        database
        cipherDatabase?.database?.close()
    }

    /**
     * Simple helper to manage backup.
     */
    private inner class BackupHelper(context: Context, name: String, version: Int, databaseDefinition: DatabaseDefinition) : SQLiteOpenHelper(context, name, null, version), OpenHelper {

        private var sqlCipherDatabase: SQLCipherDatabase? = null
        private val baseDatabaseHelper: BaseDatabaseHelper

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

        init {
            this.baseDatabaseHelper = BaseDatabaseHelper(databaseDefinition)
        }

        override fun performRestoreFromBackup() {}

        override fun backupDB() {}

        override fun closeDB() {}

        override fun setDatabaseListener(helperListener: DatabaseHelperListener?) {}

        override fun onCreate(db: SQLiteDatabase) {
            baseDatabaseHelper.onCreate(SQLCipherDatabase.from(db))
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            baseDatabaseHelper.onUpgrade(SQLCipherDatabase.from(db), oldVersion, newVersion)
        }

        override fun onOpen(db: SQLiteDatabase) {
            baseDatabaseHelper.onOpen(SQLCipherDatabase.from(db))
        }
    }
}
