package com.dbflow5.database

import com.dbflow5.config.FlowLog
import com.dbflow5.database.migration.MigrationFileHelper
import java.io.IOException
import java.io.InputStream

interface JavaMigrationFileHelper : MigrationFileHelper {

    fun openFileStream(fileName: String): InputStream

    override fun executeMigration(fileName: String, dbFunction: (queryString: String) -> Unit) {
        try {
            val input: InputStream = openFileStream(fileName)

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
                    dbFunction(query.toString())
                    query = StringBuffer()
                }
            }

            val queryString = query.toString()
            if (queryString.trim { it <= ' ' }.isNotEmpty()) {
                dbFunction(queryString)
            }
        } catch (e: IOException) {
            FlowLog.log(FlowLog.Level.E, "Failed to execute $fileName. App might be in an inconsistent state!",
                throwable = e)
        }
    }
}
