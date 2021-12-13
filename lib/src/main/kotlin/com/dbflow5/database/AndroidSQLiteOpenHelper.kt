package com.dbflow5.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.OpenHelperCreator

/**
 * Description: Wraps around the [SQLiteOpenHelper] and provides extra features for use in this library.
 */
open class AndroidSQLiteOpenHelper(
    private val context: Context,
    dbFlowDatabase: DBFlowDatabase,
    listener: DatabaseCallback?,
    private val databaseHelperDelegate: DatabaseHelperDelegate = DatabaseHelperDelegate(context, listener, dbFlowDatabase,
        if (dbFlowDatabase.backupEnabled) {
            // Temp database mirrors existing
            BackupHelper(context,
                DatabaseHelperDelegate.getTempDbFileName(dbFlowDatabase),
                dbFlowDatabase.databaseVersion, dbFlowDatabase)
        } else null),
) : SQLiteOpenHelper(
    context,
    if (dbFlowDatabase.isInMemory) null else dbFlowDatabase.databaseFileName,
    null,
    dbFlowDatabase.databaseVersion,
), OpenHelper, OpenHelperDelegate by databaseHelperDelegate {


    private var androidDatabase: AndroidDatabase? = null
    private val _databaseName = dbFlowDatabase.databaseFileName

    override val database: DatabaseWrapper
        get() {
            if (androidDatabase == null || androidDatabase?.database?.isOpen == false) {
                androidDatabase = AndroidDatabase.from(writableDatabase)
            }
            return androidDatabase!!
        }

    /**
     * Set a listener to listen for specific DB events and perform an action before we execute this classes
     * specific methods.
     *
     * @param callback
     */
    override fun setDatabaseListener(callback: DatabaseCallback?) {
        databaseHelperDelegate.setDatabaseHelperListener(callback)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        databaseHelperDelegate.onConfigure(AndroidDatabase.from(db))
    }

    override fun onCreate(db: SQLiteDatabase) {
        databaseHelperDelegate.onCreate(AndroidDatabase.from(db))
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        databaseHelperDelegate.onUpgrade(AndroidDatabase.from(db), oldVersion, newVersion)
    }

    override fun onOpen(db: SQLiteDatabase) {
        databaseHelperDelegate.onOpen(AndroidDatabase.from(db))
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        databaseHelperDelegate.onDowngrade(AndroidDatabase.from(db), oldVersion, newVersion)
    }

    override fun closeDB() {
        androidDatabase?.database?.close()
    }

    override fun deleteDB() {
        context.deleteDatabase(_databaseName)
    }

    /**
     * Simple helper to manage backup.
     */
    private class BackupHelper(private val context: Context,
                               name: String, version: Int,
                               databaseDefinition: DBFlowDatabase)
        : SQLiteOpenHelper(context, name, null, version), OpenHelper {

        private var androidDatabase: AndroidDatabase? = null
        private val databaseHelper: DatabaseHelper = DatabaseHelper(AndroidMigrationFileHelper(context), databaseDefinition)
        private val _databaseName = databaseDefinition.databaseFileName

        override val database: DatabaseWrapper
            get() {
                if (androidDatabase == null) {
                    androidDatabase = AndroidDatabase.from(writableDatabase)
                }
                return androidDatabase!!
            }

        override fun performRestoreFromBackup() = Unit

        override val delegate: DatabaseHelperDelegate?
            get() = null

        override val isDatabaseIntegrityOk: Boolean
            get() = false

        override fun backupDB() {}

        override fun setDatabaseListener(callback: DatabaseCallback?) {}

        override fun onConfigure(db: SQLiteDatabase) {
            databaseHelper.onConfigure(AndroidDatabase.from(db))
        }

        override fun onCreate(db: SQLiteDatabase) {
            databaseHelper.onCreate(AndroidDatabase.from(db))
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            databaseHelper.onUpgrade(AndroidDatabase.from(db), oldVersion, newVersion)
        }

        override fun onOpen(db: SQLiteDatabase) {
            databaseHelper.onOpen(AndroidDatabase.from(db))
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            databaseHelper.onDowngrade(AndroidDatabase.from(db), oldVersion, newVersion)
        }

        override fun closeDB() = Unit

        override fun deleteDB() {
            context.deleteDatabase(_databaseName)
        }
    }

    companion object {
        @JvmStatic
        fun createHelperCreator(context: Context): OpenHelperCreator =
            OpenHelperCreator { db: DBFlowDatabase, databaseCallback: DatabaseCallback? ->
                AndroidSQLiteOpenHelper(context, db, databaseCallback)
            }
    }

}

