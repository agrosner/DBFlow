package com.raizlabs.dbflow5.android.support

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.raizlabs.dbflow5.config.DatabaseDefinition
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.database.AndroidDatabase
import com.raizlabs.dbflow5.database.BaseDatabaseHelper
import com.raizlabs.dbflow5.database.DatabaseHelperDelegate
import com.raizlabs.dbflow5.database.DatabaseHelperListener
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.OpenHelper

/**
 * Description:
 */
class SupportAndroidSQLiteOpenHelper(
    databaseDefinition: DatabaseDefinition,
    listener: DatabaseHelperListener?)
    : OpenHelper {

    private val databaseHelperDelegate: DatabaseHelperDelegate
    private var androidDatabase: SupportAndroidDatabase? = null

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
                androidDatabase = SupportAndroidDatabase.from(writableDatabase)
            }
            return androidDatabase!!
        }

    /**
     * Set a listener to listen for specific DB events and perform an action before we execute this classes
     * specific methods.
     *
     * @param helperListener
     */
    override fun setDatabaseListener(helperListener: DatabaseHelperListener?) {
        databaseHelperDelegate.setDatabaseHelperListener(helperListener)
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

        private var androidDatabase: SupportAndroidDatabase? = null
        private val baseDatabaseHelper: BaseDatabaseHelper = BaseDatabaseHelper(databaseDefinition)

        override val database: DatabaseWrapper
            get() {
                if (androidDatabase == null) {
                    androidDatabase = SupportAndroidDatabase.from(writableDatabase)
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
