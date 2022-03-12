package com.dbflow5.database

import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.JournalMode
import co.touchlab.sqliter.createDatabaseManager
import co.touchlab.sqliter.updateJournalMode
import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.database.migration.DefaultMigrator
import com.dbflow5.database.migration.Migrator
import com.dbflow5.delegates.databaseProperty
import okio.FileSystem
import okio.Path.Companion.toPath

class NativeOpenHelper(
    private val generatedDatabase: GeneratedDatabase,
    callback: DatabaseCallback?,
    migrator: Migrator = DefaultMigrator(
        NativeMigrationFileHelper(), generatedDatabase,
        useTransactions = false
    ),
    private val databaseHelperDelegate: DatabaseHelperDelegate = DatabaseHelperDelegate(
        callback,
        generatedDatabase,
        helper = DatabaseHelper(migrator, generatedDatabase),
        databaseBackup = DatabaseBackup(generatedDatabase),
    ),
) : OpenHelper, OpenHelperDelegate by databaseHelperDelegate {

    private val manager = createDatabaseManager(
        DatabaseConfiguration(
            name = generatedDatabase.openHelperName,
            version = generatedDatabase.databaseVersion,
            create = { connection ->
                val db = NativeDatabase(generatedDatabase, connection)
                databaseHelperDelegate.onConfigure(db)
                databaseHelperDelegate.onCreate(db)
            },
            upgrade = { connection, upgrade, version ->
                databaseHelperDelegate.onUpgrade(
                    NativeDatabase(generatedDatabase, connection), version, upgrade
                )
            },
            inMemory = generatedDatabase.isInMemory,
        )
    )

    override val database by databaseProperty {
        NativeDatabase(generatedDatabase, manager.createSingleThreadedConnection())
    }

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        database.db.updateJournalMode(if (enabled) JournalMode.WAL else JournalMode.DELETE)
    }

    override fun setDatabaseListener(callback: DatabaseCallback?) {
        databaseHelperDelegate.setDatabaseHelperListener(callback)
    }

    override fun close() {
        database.db.close()
    }

    override fun delete() {
        generatedDatabase.openHelperName?.let { name ->
            FileSystem.SYSTEM.delete(name.toPath())
        }
    }
}

actual fun OpenHelper(db: GeneratedDatabase, callback: DatabaseCallback?): OpenHelper =
    NativeOpenHelper(db, callback)
