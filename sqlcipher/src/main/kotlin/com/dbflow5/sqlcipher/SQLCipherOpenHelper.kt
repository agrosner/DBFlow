package com.dbflow5.sqlcipher

import android.content.Context
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.DatabaseConfig
import com.dbflow5.config.OpenHelperCreator
import com.dbflow5.database.DatabaseCallback
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
    private val databaseDefinition: DBFlowDatabase,
    listener: DatabaseCallback?,
) : SQLiteOpenHelper(
    context,
    if (databaseDefinition.isInMemory) null else databaseDefinition.databaseFileName,
    null, databaseDefinition.databaseVersion
), OpenHelper {

    final override val delegate: DatabaseHelperDelegate
    private var cipherDatabase: SQLCipherDatabase? = null
    private val _databaseName = databaseDefinition.databaseFileName

    override val isDatabaseIntegrityOk: Boolean
        get() = delegate.isDatabaseIntegrityOk

    override val database: DatabaseWrapper
        get() {
            if (cipherDatabase == null || !cipherDatabase!!.database.isOpen) {
                cipherDatabase = SQLCipherDatabase.from(
                    getWritableDatabase(cipherSecret),
                    databaseDefinition
                )
            }
            return cipherDatabase!!
        }

    /**
     * @return The SQLCipher secret for opening this database.
     */
    protected abstract val cipherSecret: String

    init {
        SQLiteDatabase.loadLibs(context)
        delegate = DatabaseHelperDelegate(context, listener, databaseDefinition)
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
        delegate.onConfigure(SQLCipherDatabase.from(db, databaseDefinition))
    }

    override fun onCreate(db: SQLiteDatabase) {
        delegate.onCreate(SQLCipherDatabase.from(db, databaseDefinition))
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        delegate.onUpgrade(SQLCipherDatabase.from(db, databaseDefinition), oldVersion, newVersion)
    }

    override fun onOpen(db: SQLiteDatabase) {
        delegate.onOpen(SQLCipherDatabase.from(db, databaseDefinition))
    }

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        if (enabled) {
            cipherDatabase?.database?.enableWriteAheadLogging()
        } else {
            cipherDatabase?.database?.disableWriteAheadLogging()
        }
    }

    override fun closeDB() {
        database
        cipherDatabase?.database?.close()
    }

    override fun deleteDB() {
        context.deleteDatabase(_databaseName)
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
