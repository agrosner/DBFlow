package com.dbflow5.database.migration

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.adapter.create
import com.dbflow5.config.DatabaseObjectLookup
import com.dbflow5.config.FlowLog
import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.database.DatabaseConnection
import com.dbflow5.database.SQLiteException
import com.dbflow5.database.scope.MigrationScope

/**
 * Runs migrations on the DB.
 */
interface Migrator {

    suspend fun MigrationScope.create(db: DatabaseConnection)

    suspend fun MigrationScope.migrate(db: DatabaseConnection, oldVersion: Int, newVersion: Int)

    suspend fun MigrationScope.migrateViews(db: DatabaseConnection)
}


/**
 * Migrates DB using
 */
class DefaultMigrator(
    private val migrationFileHelper: MigrationFileHelper,
    private val generatedDatabase: GeneratedDatabase,
    /**
     * If false, the driver implementation may already use transactions.
     */
    private val useTransactions: Boolean = true,
) : Migrator {
    // path to migration for the database.
    private val dbMigrationPath
        get() = "${MIGRATION_PATH}/${generatedDatabase.databaseName}"

    override suspend fun MigrationScope.create(db: DatabaseConnection) {
        maybeTransact(db) {
            generatedDatabase.tables
                .asSequence()
                .map { DatabaseObjectLookup.getModelAdapter(it) }
                .filter { it.createWithDatabase }
                .forEach { it.logOrThrow(this) }
        }
    }

    override suspend fun MigrationScope.migrate(
        db: DatabaseConnection,
        oldVersion: Int,
        newVersion: Int
    ) {
        // will try migrations file or execute migrations from code
        val files: List<String> = migrationFileHelper.getListFiles(dbMigrationPath)
            .sortedWith(naturalOrder())

        val migrationFileMap = hashMapOf<Int, MutableList<String>>()
        for (file in files) {
            try {
                val version = file.replace(".sql", "").toInt()
                val fileList = migrationFileMap.getOrPut(version) { arrayListOf() }
                fileList.add(file)
            } catch (e: NumberFormatException) {
                FlowLog.log(FlowLog.Level.W, "Skipping invalidly named file: $file", throwable = e)
            }

        }

        val migrationMap = generatedDatabase.migrations

        val curVersion = oldVersion + 1

        maybeTransact(db) {
            // execute migrations in order, migration file first before wrapped migration classes.
            for (i in curVersion..newVersion) {
                migrationFileMap[i]?.forEach { migrationFile ->
                    executeSqlScript(db, migrationFile)
                    FlowLog.log(FlowLog.Level.I, "$migrationFile executed successfully.")
                }

                migrationMap[i]?.forEach { migration ->
                    migration.apply { migrate(db) }
                    FlowLog.log(
                        FlowLog.Level.I,
                        "${migration::class} executed successfully."
                    )
                }
            }
        }
    }

    override suspend fun MigrationScope.migrateViews(db: DatabaseConnection) {
        maybeTransact(db) {
            generatedDatabase.views
                .asSequence()
                .map { DatabaseObjectLookup.getModelViewAdapter(it) }
                .filter { it.createWithDatabase }
                .forEach { it.logOrThrow(this) }
        }
    }

    private suspend fun <R> maybeTransact(
        db: DatabaseConnection,
        fn: suspend DatabaseConnection.() -> R
    ) =
        if (useTransactions) {
            db.executeTransaction(fn)
        } else {
            fn(db)
        }

    /**
     * Supports multiline sql statements with ended with the standard ";"
     *
     * @param db   The database to run it on
     * @param file the file name in assets/migrations that we read from
     */
    private fun executeSqlScript(db: DatabaseConnection, file: String) {
        migrationFileHelper.executeMigration("$dbMigrationPath/$file") { queryString ->
            db.execSQL(
                queryString
            )
        }
    }

    /**
     * Logs or throws an exception depending on db config.
     */
    private fun DBRepresentable<*>.logOrThrow(databaseConnection: DatabaseConnection) {
        try {
            create(databaseConnection)
        } catch (e: SQLiteException) {
            if (generatedDatabase.throwExceptionsOnCreate) {
                throw e
            } else {
                FlowLog.logError(e)
            }
        }
    }

    private companion object {

        /**
         * Location where the migration files should exist.
         */
        const val MIGRATION_PATH = "migrations"
    }
}