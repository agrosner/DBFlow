package com.dbflow5.database

import com.dbflow5.config.FlowLog
import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.database.migration.Migrator
import com.dbflow5.database.scope.MigrationScopeImpl
import com.dbflow5.mpp.runBlocking


/**
 * Manages creation, updating, and migrating the [GeneratedDatabase].
 */
class DatabaseHelper(
    private val migrator: Migrator,
    val generatedDatabase: GeneratedDatabase
) : DatabaseCallback {

    override fun onConfigure(db: DatabaseWrapper) {
        checkForeignKeySupport(db)
    }

    override fun onCreate(db: DatabaseWrapper) {
        // table creations done first to get tables in db.
        runBlocking {
            migrator.apply {
                MigrationScopeImpl(db).apply {
                    create(db)
                    migrate(db, -1, generatedDatabase.databaseVersion)
                    migrateViews(db)
                }
            }
        }
    }

    override fun onUpgrade(db: DatabaseWrapper, oldVersion: Int, newVersion: Int) {
        // create new tables if not previously created
        runBlocking {
            migrator.apply {
                MigrationScopeImpl(db).apply {
                    create(db)
                    migrate(db, oldVersion, newVersion)
                    migrateViews(db)
                }
            }
        }
    }

    /**
     * If foreign keys are supported, we turn it on the DB specified.
     */
    private fun checkForeignKeySupport(database: DatabaseWrapper) {
        if (generatedDatabase.isForeignKeysSupported) {
            database.execSQL("PRAGMA foreign_keys=ON;")
            FlowLog.log(FlowLog.Level.I, "Foreign Keys supported. Enabling foreign key features.")
        }
    }
}
