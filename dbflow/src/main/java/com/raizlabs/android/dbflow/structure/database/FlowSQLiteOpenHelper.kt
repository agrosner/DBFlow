package com.raizlabs.android.dbflow.structure.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.config.FlowManager

/**
 * Description: Wraps around the [SQLiteOpenHelper] and provides extra features for use in this library.
 */
class FlowSQLiteOpenHelper(
        databaseDefinition: DatabaseDefinition,
        listener: DatabaseHelperListener?)
    : SQLiteOpenHelper(FlowManager.context,
        if (databaseDefinition.isInMemory) null else databaseDefinition.databaseFileName,
        null,
        databaseDefinition.databaseVersion), OpenHelper {

    private val databaseHelperDelegate: DatabaseHelperDelegate
    private var androidDatabase: AndroidDatabase? = null

    init {
        var backupHelper: OpenHelper? = null
        if (databaseDefinition.backupEnabled()) {
            // Temp database mirrors existing
            backupHelper = BackupHelper(FlowManager.context,
                    DatabaseHelperDelegate.getTempDbFileName(databaseDefinition),
                    databaseDefinition.databaseVersion, databaseDefinition)
        }

        databaseHelperDelegate = DatabaseHelperDelegate(listener, databaseDefinition, backupHelper)
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
            if (androidDatabase == null || !androidDatabase!!.database.isOpen) {
                androidDatabase = AndroidDatabase.from(writableDatabase)
            }
            return androidDatabase!!
        }

    /**
     * Set a listener to listen for specific DB events and perform an action before we execute this classes
     * specific methods.
     *
     * @param listener
     */
    override fun setDatabaseListener(listener: DatabaseHelperListener?) {
        databaseHelperDelegate.setDatabaseHelperListener(listener!!)
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

    /**
     * Simple helper to manage backup.
     */
    private inner class BackupHelper(context: Context,
                                     name: String, version: Int,
                                     databaseDefinition: DatabaseDefinition)
        : SQLiteOpenHelper(context, name, null, version), OpenHelper {

        private var androidDatabase: AndroidDatabase? = null
        private val baseDatabaseHelper: BaseDatabaseHelper

        init {
            this.baseDatabaseHelper = BaseDatabaseHelper(databaseDefinition)
        }

        override val database: DatabaseWrapper
            get() {
                if (androidDatabase == null) {
                    androidDatabase = AndroidDatabase.from(writableDatabase)
                }
                return androidDatabase!!
            }

        override fun performRestoreFromBackup() {}

        override val delegate: DatabaseHelperDelegate?
            get() = null

        override val isDatabaseIntegrityOk: Boolean
            get() = false

        override fun backupDB() {}

        override fun setDatabaseListener(helperListener: DatabaseHelperListener?) {}

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

        override fun closeDB() {}
    }

}
