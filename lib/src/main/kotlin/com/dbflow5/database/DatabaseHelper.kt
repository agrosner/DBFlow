package com.dbflow5.database

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.adapter.create
import com.dbflow5.config.FlowLog
import com.dbflow5.config.DatabaseObjectLookup
import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.config.NaturalOrderComparator
import java.io.IOException

/**
 * Manages creation, updating, and migrating the [GeneratedDatabase].
 */
open class DatabaseHelper(
    private val migrationFileHelper: MigrationFileHelper,
    val generatedDatabase: GeneratedDatabase
) {

    // path to migration for the database.
    private val dbMigrationPath
        get() = "$MIGRATION_PATH/${generatedDatabase.databaseName}"

    open fun onConfigure(db: DatabaseWrapper) {
        checkForeignKeySupport(db)
    }

    open fun onCreate(db: DatabaseWrapper) {
        // table creations done first to get tables in db.
        executeTableCreations(db)

        // execute any initial migrations when DB is first created.
        // use the databaseversion of the definition, since onupgrade is not called oncreate on a version 0
        // then SQLCipher and Android set the DB to that version you choose.
        executeMigrations(db, -1, generatedDatabase.databaseVersion)

        // views reflect current db state.
        executeViewCreations(db)
    }

    open fun onUpgrade(db: DatabaseWrapper, oldVersion: Int, newVersion: Int) {
        // create new tables if not previously created
        executeTableCreations(db)

        // migrations run to get to DB newest version. adjusting any existing tables to new version
        executeMigrations(db, oldVersion, newVersion)

        // views reflect current db state.
        executeViewCreations(db)
    }

    open fun onOpen(db: DatabaseWrapper) {
    }

    open fun onDowngrade(db: DatabaseWrapper, oldVersion: Int, newVersion: Int) {
    }

    /**
     * If foreign keys are supported, we turn it on the DB specified.
     */
    protected fun checkForeignKeySupport(database: DatabaseWrapper) {
        if (generatedDatabase.isForeignKeysSupported) {
            database.execSQL("PRAGMA foreign_keys=ON;")
            FlowLog.log(FlowLog.Level.I, "Foreign Keys supported. Enabling foreign key features.")
        }
    }

    protected fun executeTableCreations(database: DatabaseWrapper) {
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
    protected fun executeViewCreations(database: DatabaseWrapper) {
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

    protected fun executeMigrations(
        db: DatabaseWrapper,
        oldVersion: Int, newVersion: Int
    ) {

        // will try migrations file or execute migrations from code
        try {
            val files: List<String> = migrationFileHelper.getListFiles(dbMigrationPath)
                .sortedWith(NaturalOrderComparator())

            val migrationFileMap = hashMapOf<Int, MutableList<String>>()
            for (file in files) {
                try {
                    val version = Integer.valueOf(file.replace(".sql", ""))
                    val fileList = migrationFileMap.getOrPut(version) { arrayListOf() }
                    fileList.add(file)
                } catch (e: NumberFormatException) {
                    FlowLog.log(FlowLog.Level.W, "Skipping invalidly named file: $file", e)
                }

            }

            val migrationMap = generatedDatabase.migrations

            val curVersion = oldVersion + 1

            try {
                db.beginTransaction()

                // execute migrations in order, migration file first before wrapped migration classes.
                for (i in curVersion..newVersion) {
                    val migrationFiles = migrationFileMap[i]
                    if (migrationFiles != null) {
                        for (migrationFile in migrationFiles) {
                            executeSqlScript(db, migrationFile)
                            FlowLog.log(FlowLog.Level.I, "$migrationFile executed successfully.")
                        }
                    }

                    val migrationsList = migrationMap[i]
                    if (migrationsList != null) {
                        for (migration in migrationsList) {
                            // before migration
                            migration.onPreMigrate()

                            // migrate
                            migration.migrate(db)

                            // after migration cleanup
                            migration.onPostMigrate()
                            FlowLog.log(
                                FlowLog.Level.I,
                                "${migration.javaClass} executed successfully."
                            )
                        }
                    }
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } catch (e: IOException) {
            FlowLog.log(
                FlowLog.Level.E,
                "Failed to execute migrations. App might be in an inconsistent state.",
                e
            )
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
        @JvmStatic
        val MIGRATION_PATH = "migrations"
    }
}
