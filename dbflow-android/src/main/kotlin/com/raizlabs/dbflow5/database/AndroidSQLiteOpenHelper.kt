package com.raizlabs.dbflow5.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.config.OpenHelperCreator

actual typealias PlatformOpenHelper = AndroidSQLiteOpenHelper

/**
 * Description: Wraps around the [SQLiteOpenHelper] and provides extra features for use in this library.
 */
open class AndroidSQLiteOpenHelper(
    private val context: Context,
    dbFlowDatabase: DBFlowDatabase,
    callback: DatabaseCallback?)
    : SQLiteOpenHelper(context,
    if (dbFlowDatabase.isInMemory) null else dbFlowDatabase.databaseFileName,
    null,
    dbFlowDatabase.databaseVersion), OpenHelper {

    /**
     * Used for default platform constructor compatibility.
     */
    constructor(db: DBFlowDatabase, callback: DatabaseCallback?) : this(
        FlowManager.context,
        db, callback)

    private val databaseHelperDelegate: DatabaseHelperDelegate
    private var androidDatabase: AndroidDatabase? = null
    private val _databaseName = dbFlowDatabase.databaseFileName

    init {
        val restoreHelper = PlatformDatabaseRestoreHelper(context)
        val migrationHelper = PlatformMigrationHelper(context, dbFlowDatabase)
        var backupHelper: OpenHelper? = null
        if (dbFlowDatabase.backupEnabled()) {
            // Temp database mirrors existing
            backupHelper = BackupHelper(migrationHelper,
                DatabaseHelperDelegate.getTempDbFileName(dbFlowDatabase),
                dbFlowDatabase.databaseVersion, dbFlowDatabase)
        }

        databaseHelperDelegate = DatabaseHelperDelegate(migrationHelper, restoreHelper, callback, dbFlowDatabase, backupHelper)
    }

    override fun performRestoreFromBackup() {
        databaseHelperDelegate.performRestoreFromBackup()
    }

    override val delegate: DatabaseHelperDelegate?
        get() = databaseHelperDelegate

    override val isDatabaseIntegrityOk: Boolean
        get() = databaseHelperDelegate.isDatabaseIntegrityOk

    override fun backupDB() {
        databaseHelperDelegate.backupDB()
    }

    override val database: DatabaseWrapper
        get() {
            if (androidDatabase == null || androidDatabase?.database?.isOpen == false) {
                androidDatabase = AndroidDatabase.from(writableDatabase)
            }
            return androidDatabase!!
        }

    /**
     * Set a callback to listen for specific DB events and perform an action before we execute this classes
     * specific methods.
     *
     * @param callback
     */
    override fun setDatabaseCallback(callback: DatabaseCallback?) {
        databaseHelperDelegate.setDatabaseCallback(callback)
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
    private inner class BackupHelper(migrationHelper: MigrationHelper,
                                     name: String, version: Int,
                                     databaseDefinition: DBFlowDatabase)
        : SQLiteOpenHelper(context, name, null, version), OpenHelper {

        private var androidDatabase: AndroidDatabase? = null
        private val baseDatabaseHelper: BaseDatabaseHelper = BaseDatabaseHelper(migrationHelper, databaseDefinition)
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

        override fun setDatabaseCallback(callback: DatabaseCallback?) {}

        override fun onCreate(db: SQLiteDatabase) {
            baseDatabaseHelper.onCreate(AndroidDatabase.from(db))
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            baseDatabaseHelper.onUpgrade(AndroidDatabase.from(db), oldVersion, newVersion)
        }

        override fun onOpen(db: SQLiteDatabase) {
            baseDatabaseHelper.onOpen(AndroidDatabase.from(db))
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            baseDatabaseHelper.onDowngrade(AndroidDatabase.from(db), oldVersion, newVersion)
        }

        override fun closeDB() = Unit

        override fun deleteDB() {
            context.deleteDatabase(_databaseName)
        }
    }

    companion object {
        @JvmStatic
        fun createHelperCreator(context: Context): OpenHelperCreator =
            { db: DBFlowDatabase, databaseCallback: DatabaseCallback? ->
                AndroidSQLiteOpenHelper(context, db, databaseCallback)
            }
    }

}

