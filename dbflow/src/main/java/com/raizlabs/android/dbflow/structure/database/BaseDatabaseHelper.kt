package com.raizlabs.android.dbflow.structure.database

import android.database.sqlite.SQLiteException
import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.config.FlowLog
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.config.NaturalOrderComparator
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/**
 * Description:
 */
open class BaseDatabaseHelper(val databaseDefinition: DatabaseDefinition) {

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
        if (databaseDefinition.isForeignKeysSupported) {
            database.execSQL("PRAGMA foreign_keys=ON;")
            FlowLog.log(FlowLog.Level.I, "Foreign Keys supported. Enabling foreign key features.")
        }
    }

    protected fun executeTableCreations(database: DatabaseWrapper) {
        try {
            database.beginTransaction()
            val modelAdapters = databaseDefinition.getModelAdapters()
            for (modelAdapter in modelAdapters) {
                if (modelAdapter.createWithDatabase()) {
                    try {
                        database.execSQL(modelAdapter.creationQuery)
                    } catch (e: SQLiteException) {
                        FlowLog.logError(e)
                    }

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
            val modelViews = databaseDefinition.modelViewAdapters
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
        try {
            val files = Arrays.asList(*FlowManager.context.assets.list(
                    MIGRATION_PATH + "/" + databaseDefinition.databaseName))
            Collections.sort(files, NaturalOrderComparator())

            val migrationFileMap = hashMapOf<Int, MutableList<String>>()
            for (file in files) {
                try {
                    val version = Integer.valueOf(file.replace(".sql", ""))
                    var fileList: MutableList<String>? = migrationFileMap[version]
                    if (fileList == null) {
                        fileList = arrayListOf()
                        migrationFileMap.put(version, fileList)
                    }
                    fileList.add(file)
                } catch (e: NumberFormatException) {
                    FlowLog.log(FlowLog.Level.W, "Skipping invalidly named file: " + file, e)
                }

            }

            val migrationMap = databaseDefinition.migrations

            val curVersion = oldVersion + 1

            try {
                db.beginTransaction()

                // execute migrations in order, migration file first before wrapped migration classes.
                for (i in curVersion..newVersion) {
                    val migrationFiles = migrationFileMap[i]
                    if (migrationFiles != null) {
                        for (migrationFile in migrationFiles) {
                            executeSqlScript(db, migrationFile)
                            FlowLog.log(FlowLog.Level.I, migrationFile + " executed successfully.")
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
                            FlowLog.log(FlowLog.Level.I, migration.javaClass.toString() + " executed successfully.")
                        }
                    }
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        } catch (e: IOException) {
            FlowLog.log(FlowLog.Level.E, "Failed to execute migrations.", e)
        }

    }

    /**
     * Supports multiline sql statements with ended with the standard ";"
     *
     * @param db   The database to run it on
     * @param file the file name in assets/migrations that we read from
     */
    private fun executeSqlScript(db: DatabaseWrapper,
                                 file: String) {
        try {
            val input = FlowManager.context.assets.open("$MIGRATION_PATH/${databaseDefinition.databaseName}/$file")
            val reader = BufferedReader(InputStreamReader(input))

            // ends line with SQL
            val querySuffix = ";"

            // standard java comments
            val queryCommentPrefix = "--"
            var query = StringBuffer()

            reader.forEachLine { fileLine ->
                var line = fileLine.trim { it <= ' ' }
                val isEndOfQuery = line.endsWith(querySuffix)
                if (line.startsWith(queryCommentPrefix)) {
                    return@forEachLine
                }
                if (isEndOfQuery) {
                    line = line.substring(0, line.length - querySuffix.length)
                }
                query.append(" ").append(line)
                if (isEndOfQuery) {
                    db.execSQL(query.toString())
                    query = StringBuffer()
                }
            }

            val queryString = query.toString()
            if (queryString.trim { it <= ' ' }.isNotEmpty()) {
                db.execSQL(queryString)
            }
        } catch (e: IOException) {
            FlowLog.log(FlowLog.Level.E, "Failed to execute " + file, e)
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
