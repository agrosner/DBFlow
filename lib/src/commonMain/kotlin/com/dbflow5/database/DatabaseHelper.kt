package com.dbflow5.database

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.adapter.create
import com.dbflow5.config.DatabaseObjectLookup
import com.dbflow5.config.FlowLog
import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.database.scope.MigrationScope
import com.dbflow5.database.scope.MigrationScopeImpl
import com.dbflow5.mpp.runBlocking


/**
 * Manages creation, updating, and migrating the [GeneratedDatabase].
 */
class DatabaseHelper(
    private val migrationFileHelper: MigrationFileHelper,
    val generatedDatabase: GeneratedDatabase
) : DatabaseCallback {

    // path to migration for the database.
    private val dbMigrationPath
        get() = "$MIGRATION_PATH/${generatedDatabase.databaseName}"

    override fun onConfigure(db: DatabaseWrapper) {
        checkForeignKeySupport(db)
    }

    override fun onCreate(db: DatabaseWrapper) {
        // table creations done first to get tables in db.
        runBlocking {
            executeTableCreations(db)

            // execute any initial migrations when DB is first created.
            // use the databaseversion of the definition, since onupgrade is not called oncreate on a version 0
            // then SQLCipher and Android set the DB to that version you choose.
            MigrationScopeImpl(db).executeMigrations(db, -1, generatedDatabase.databaseVersion)

            // views reflect current db state.
            executeViewCreations(db)
        }
    }

    override fun onUpgrade(db: DatabaseWrapper, oldVersion: Int, newVersion: Int) {
        // create new tables if not previously created
        runBlocking {
            executeTableCreations(db)

            // migrations run to get to DB newest version. adjusting any existing tables to new version
            MigrationScopeImpl(db).executeMigrations(db, oldVersion, newVersion)

            // views reflect current db state.
            executeViewCreations(db)
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

    private suspend fun executeTableCreations(database: DatabaseWrapper) {
        database.executeTransaction {
            this@DatabaseHelper.generatedDatabase.tables
                .asSequence()
                .map { DatabaseObjectLookup.getModelAdapter(it) }
                .filter { it.createWithDatabase }
                .forEach { it.logOrThrow(this) }
        }
    }

    /**
     * This method executes CREATE TABLE statements as well as CREATE VIEW on the database passed.
     */
    private suspend fun executeViewCreations(database: DatabaseWrapper) {
        database.executeTransaction {
            this@DatabaseHelper.generatedDatabase.views
                .asSequence()
                .map { DatabaseObjectLookup.getModelViewAdapter(it) }
                .filter { it.createWithDatabase }
                .forEach { it.logOrThrow(this) }
        }
    }

    /**
     * Logs or throws an exception depending on db config.
     */
    private fun DBRepresentable<*>.logOrThrow(databaseWrapper: DatabaseWrapper) {
        try {
            create(databaseWrapper)
        } catch (e: SQLiteException) {
            if (generatedDatabase.throwExceptionsOnCreate) {
                throw e
            } else {
                FlowLog.logError(e)
            }
        }
    }

    private suspend fun MigrationScope.executeMigrations(
        db: DatabaseWrapper,
        oldVersion: Int, newVersion: Int
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
                FlowLog.log(FlowLog.Level.W, "Skipping invalidly named file: $file", e)
            }

        }

        val migrationMap = generatedDatabase.migrations

        val curVersion = oldVersion + 1

        db.executeTransaction {
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

    /**
     * Supports multiline sql statements with ended with the standard ";"
     *
     * @param db   The database to run it on
     * @param file the file name in assets/migrations that we read from
     */
    private fun executeSqlScript(db: DatabaseWrapper, file: String) {
        migrationFileHelper.executeMigration("$dbMigrationPath/$file") { queryString ->
            db.execSQL(
                queryString
            )
        }
    }

    companion object {

        /**
         * Location where the migration files should exist.
         */
        val MIGRATION_PATH = "migrations"
    }
}
