package com.dbflow5.sqlcipher

import android.content.Context
import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.database.DatabaseCallback
import com.dbflow5.database.DatabaseHelperDelegate
import com.dbflow5.database.DatabasePropertyDelegate
import com.dbflow5.database.OpenHelper
import com.dbflow5.database.OpenHelperCreator
import com.dbflow5.database.OpenHelperDelegate
import com.dbflow5.database.config.DBSettings
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper

/**
 * Description: The replacement [OpenHelper] for SQLCipher. Specify a subclass of this is [DBSettings]
 * of your database to get it to work with specifying the secret you use for the databaseForTable.
 */
class SQLCipherOpenHelper(
    private val context: Context,
    private val generatedDatabase: GeneratedDatabase,
    /**
     * @return The SQLCipher secret for opening this database.
     */
    private val secret: String,
    callback: DatabaseCallback?,
    override val delegate: DatabaseHelperDelegate =
        DatabaseHelperDelegate(context, callback, generatedDatabase).also {
            SQLiteDatabase.loadLibs(context)
        },
) : SQLiteOpenHelper(
    context,
    generatedDatabase.openHelperName,
    null, generatedDatabase.databaseVersion
), OpenHelper, OpenHelperDelegate by delegate {

    private val _databaseName = generatedDatabase.databaseFileName

    override val isDatabaseIntegrityOk: Boolean
        get() = delegate.isDatabaseIntegrityOk

    override val database: SQLCipherDatabase by DatabasePropertyDelegate {
        SQLCipherDatabase.from(getWritableDatabase(secret), generatedDatabase)
    }

    override suspend fun performRestoreFromBackup() {
        delegate.performRestoreFromBackup()
    }

    override suspend fun backupDB() {
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
        delegate.onConfigure(SQLCipherDatabase.from(db, generatedDatabase))
    }

    override fun onCreate(db: SQLiteDatabase) {
        delegate.onCreate(SQLCipherDatabase.from(db, generatedDatabase))
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        delegate.onUpgrade(SQLCipherDatabase.from(db, generatedDatabase), oldVersion, newVersion)
    }

    override fun onOpen(db: SQLiteDatabase) {
        delegate.onOpen(SQLCipherDatabase.from(db, generatedDatabase))
    }

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        database.database.let { db ->
            if (enabled) {
                db.enableWriteAheadLogging()
            } else {
                db.disableWriteAheadLogging()
            }
        }
    }

    override fun closeDB() {
        database.database.close()
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
                SQLCipherOpenHelper(context, db, callback = callback, secret = secret)
            }
    }

}
