package com.dbflow5.sqlcipher

import android.content.Context
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.DatabaseConfig
import com.dbflow5.config.OpenHelperCreator
import com.dbflow5.database.AndroidMigrationFileHelper
import com.dbflow5.database.DatabaseCallback
import com.dbflow5.database.DatabaseHelper
import com.dbflow5.database.DatabaseHelperDelegate
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.OpenHelper
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

        var backupHelper: OpenHelper? = null
        if (databaseDefinition.backupEnabled()) {
            // Temp database mirrors existing
            backupHelper = BackupHelper(context,
                DatabaseHelperDelegate.getTempDbFileName(databaseDefinition),
                databaseDefinition.databaseVersion, databaseDefinition)
        }

        delegate = DatabaseHelperDelegate(context, listener, databaseDefinition, backupHelper)
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
    override fun setDatabaseListener(callback: DatabaseCallback?) {
        delegate.setDatabaseHelperListener(callback)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        delegate.onConfigure(SQLCipherDatabase.from(db))
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
    private inner class BackupHelper(private val context: Context,
                                     name: String,
                                     version: Int,
                                     databaseDefinition: DBFlowDatabase)
        : SQLiteOpenHelper(context, name, null, version), OpenHelper {

        private var sqlCipherDatabase: SQLCipherDatabase? = null
        private val databaseHelper: DatabaseHelper = DatabaseHelper(AndroidMigrationFileHelper(context), databaseDefinition)
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

        override fun setDatabaseListener(callback: DatabaseCallback?) = Unit

        override fun onConfigure(db: SQLiteDatabase) {
            databaseHelper.onConfigure(SQLCipherDatabase.from(db))
        }

        override fun onCreate(db: SQLiteDatabase) {
            databaseHelper.onCreate(SQLCipherDatabase.from(db))
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            databaseHelper.onUpgrade(SQLCipherDatabase.from(db), oldVersion, newVersion)
        }

        override fun onOpen(db: SQLiteDatabase) {
            databaseHelper.onOpen(SQLCipherDatabase.from(db))
        }

        override fun setWriteAheadLoggingEnabled(enabled: Boolean) = Unit

        override fun deleteDB() {
            context.deleteDatabase(_databaseName)
        }
    }

    companion object {
        /**
         * Provides a handy interface for [OpenHelperCreator] usage.
         */
        @JvmStatic
        fun createHelperCreator(context: Context, secret: String): OpenHelperCreator =
            OpenHelperCreator { db, callback ->
                object : SQLCipherOpenHelper(context, db, callback) {
                    override val cipherSecret: String = secret
                }
            }
    }

}
