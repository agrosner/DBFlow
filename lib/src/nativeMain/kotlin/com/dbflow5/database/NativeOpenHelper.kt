package com.dbflow5.database

import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.DatabaseFileContext
import co.touchlab.sqliter.JournalMode
import co.touchlab.sqliter.createDatabaseManager
import co.touchlab.sqliter.deleteDatabase
import co.touchlab.sqliter.updateJournalMode
import com.dbflow5.database.migration.DefaultMigrator
import com.dbflow5.database.migration.Migrator
import com.dbflow5.delegates.databaseProperty

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

    private val configuration = DatabaseConfiguration(
        name = generatedDatabase.openHelperName,
        version = generatedDatabase.databaseVersion,
        extendedConfig = DatabaseConfiguration.Extended(
            foreignKeyConstraints = generatedDatabase.isForeignKeysSupported,
        ),
        create = { connection ->
            databaseHelperDelegate.onCreate(
                NativeDatabaseConnection(generatedDatabase, connection)
            )
        },
        upgrade = { connection, upgrade, version ->
            databaseHelperDelegate.onUpgrade(
                NativeDatabaseConnection(generatedDatabase, connection), version, upgrade
            )
        },
        inMemory = generatedDatabase.isInMemory,
    )
    private val manager = createDatabaseManager(
        configuration
    )

    override val database by databaseProperty {
        NativeDatabaseConnection(generatedDatabase, manager.createSingleThreadedConnection())
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
        DatabaseFileContext.deleteDatabase(configuration)
    }
}

actual fun OpenHelper(db: GeneratedDatabase, callback: DatabaseCallback?): OpenHelper =
    NativeOpenHelper(db, callback)
