package com.raizlabs.dbflow5.database

import com.raizlabs.dbflow5.JvmStatic
import com.raizlabs.dbflow5.config.DBFlowDatabase
import com.raizlabs.dbflow5.config.FlowLog

/**
 * Description:
 */
open class BaseDatabaseHelper(protected val migrationHelper: MigrationHelper,
                              val database: DBFlowDatabase) {

    open fun onCreate(db: DatabaseWrapper) {
        checkForeignKeySupport(db)
        executeTableCreations(db)
        executeMigrations(db, -1, db.version)
        executeViewCreations(db)
    }

    open fun onUpgrade(db: DatabaseWrapper, oldVersion: Int, newVersion: Int) {
        checkForeignKeySupport(db)
        executeTableCreations(db)
        executeMigrations(db, oldVersion, newVersion)
        executeViewCreations(db)
    }

    open fun onOpen(db: DatabaseWrapper) {
        checkForeignKeySupport(db)
    }

    open fun onDowngrade(db: DatabaseWrapper, oldVersion: Int, newVersion: Int) {
        checkForeignKeySupport(db)
    }

    /**
     * If foreign keys are supported, we turn it on the DB specified.
     */
    protected fun checkForeignKeySupport(database: DatabaseWrapper) {
        if (this.database.isForeignKeysSupported) {
            database.execSQL("PRAGMA foreign_keys=ON;")
            FlowLog.log(FlowLog.Level.I, "Foreign Keys supported. Enabling foreign key features.")
        }
    }

    protected fun executeTableCreations(database: DatabaseWrapper) {
        try {
            database.beginTransaction()
            val modelAdapters = this.database.getModelAdapters()
            modelAdapters
                .asSequence()
                .filter { it.createWithDatabase() }
                .forEach {
                    try {
                        database.execSQL(it.creationQuery)
                    } catch (e: SQLiteException) {
                        FlowLog.logError(e)
                    }
                }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    /**
     * This method executes CREATE TABLE statements as well as CREATE VIEW on the database passed.
     */
    protected fun executeViewCreations(database: DatabaseWrapper) {
        try {
            database.beginTransaction()
            val modelViews = this.database.modelViewAdapters
            modelViews
                .asSequence()
                .map { "CREATE VIEW IF NOT EXISTS ${it.viewName} AS ${it.getCreationQuery(database)}" }
                .forEach {
                    try {
                        database.execSQL(it)
                    } catch (e: SQLiteException) {
                        FlowLog.logError(e)
                    }
                }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    protected fun executeMigrations(db: DatabaseWrapper,
                                    oldVersion: Int, newVersion: Int) {

        // will try migrations file or execute migrations from code
        val migrationMap = database.migrations

        val curVersion = oldVersion + 1
        db.executeTransaction {
            // execute migrations in order, migration file first before wrapped migration classes.
            for (i in curVersion..newVersion) {
                migrationHelper.executeMigration(db, i)

                val migrationsList = migrationMap[i]
                if (migrationsList != null) {
                    for (migration in migrationsList) {
                        // before migration
                        migration.onPreMigrate()

                        // migrate
                        migration.migrate(db)

                        // after migration cleanup
                        migration.onPostMigrate()
                        FlowLog.log(FlowLog.Level.I, "$migration executed successfully.")
                    }
                }
            }
        }
    }

    companion object {

        /**
         * Location where the migration files should exist.
         */
        @JvmStatic
        val MIGRATION_PATH = "migrations"
    }
}
