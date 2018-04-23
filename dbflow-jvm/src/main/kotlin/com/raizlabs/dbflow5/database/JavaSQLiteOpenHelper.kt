package com.raizlabs.dbflow5.database

import java.sql.Connection

actual typealias PlatformOpenHelper = JavaSQLiteOpenHelper

open class JavaSQLiteOpenHelper(db: DBFlowDatabase,
                                callback: DatabaseCallback?) : OpenHelper {

    private var connection: Connection? = null
    private val _databaseName = db.databaseName

    private val databaseHelperDelegate: DatabaseHelperDelegate

    init {
        val restoreHelper = PlatformDatabaseRestoreHelper()
        val migrationHelper = PlatformMigrationHelper(db)
        var backupHelper: OpenHelper? = null
        if (db.backupEnabled()) {
            // Temp database mirrors existing
            backupHelper = BackupHelper(migrationHelper,
                DatabaseHelperDelegate.getTempDbFileName(db),
                db.databaseVersion, db)
        }

        databaseHelperDelegate = DatabaseHelperDelegate(migrationHelper, restoreHelper, callback, db, backupHelper)
    }

    override val database: DatabaseWrapper
        get() = TODO("not implemented")
    override val delegate: DatabaseHelperDelegate?
        get() = TODO("not implemented")
    override val isDatabaseIntegrityOk: Boolean
        get() = TODO("not implemented")

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        TODO("not implemented")
    }

    override fun performRestoreFromBackup() {
        TODO("not implemented")
    }

    override fun backupDB() {
        TODO("not implemented")
    }

    override fun setDatabaseCallback(callback: DatabaseCallback?) {
        TODO("not implemented")
    }

    override fun closeDB() {
        TODO("not implemented")
    }

    override fun deleteDB() {
        TODO("not implemented")
    }

    /**
     * Simple helper to manage backup.
     */
    private inner class BackupHelper(migrationHelper: MigrationHelper,
                                     name: String, version: Int,
                                     databaseDefinition: DBFlowDatabase)
        : OpenHelper {

        private val baseDatabaseHelper: BaseDatabaseHelper = BaseDatabaseHelper(migrationHelper, databaseDefinition)
        private val _databaseName = databaseDefinition.databaseFileName

        override val database: DatabaseWrapper
            get() = TODO("not implemented")

        override fun performRestoreFromBackup() = Unit

        override val delegate: DatabaseHelperDelegate?
            get() = null

        override val isDatabaseIntegrityOk: Boolean
            get() = false

        override fun backupDB() {}

        override fun setDatabaseCallback(callback: DatabaseCallback?) {}

       /* override fun onCreate(db: SQLiteDatabase) {
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
        }*/

        override fun closeDB() = Unit
        override fun deleteDB() {
            TODO("not implemented")
        }

        override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
            TODO("not implemented")
        }
    }
}