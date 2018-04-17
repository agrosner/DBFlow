package com.raizlabs.dbflow5.database

import android.content.Context
import com.raizlabs.dbflow5.config.DBFlowDatabase
import com.raizlabs.dbflow5.config.FlowLog
import java.io.IOException
import java.io.InputStream

actual class PlatformMigrationHelper(private val context: Context,
                                     private val database: DBFlowDatabase) : MigrationHelper {

    private val migrationFileMap by lazy {
        val files: List<String> = context.assets.list(
            "${BaseDatabaseHelper.MIGRATION_PATH}/${database.databaseName}")
            .sortedWith(naturalOrder<String>())

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
        migrationFileMap
    }

    override fun executeMigration(db: DatabaseWrapper, version: Int) {
        val migrationFiles = migrationFileMap[version]
        if (migrationFiles != null) {
            for (migrationFile in migrationFiles) {
                executeSqlScript(db, migrationFile)
                FlowLog.log(FlowLog.Level.I, "$migrationFile executed successfully.")
            }
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
            val input: InputStream = context.assets.open("${BaseDatabaseHelper.MIGRATION_PATH}/${database.databaseName}/$file")

            // ends line with SQL
            val querySuffix = ";"

            // standard java comments
            val queryCommentPrefix = "--"
            var query = StringBuffer()

            input.reader().buffered().forEachLine { fileLine ->
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
            FlowLog.log(FlowLog.Level.E, "Failed to execute $file. App might be in an inconsistent state!", e)
        }
    }
}
