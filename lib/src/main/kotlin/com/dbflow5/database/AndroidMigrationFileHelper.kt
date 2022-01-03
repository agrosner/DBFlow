package com.dbflow5.database

import android.content.Context
import com.dbflow5.config.FlowLog
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.InputStream

/**
 * Description: Implements [MigrationFileHelper] for Android targets.
 */
class AndroidMigrationFileHelper(private val context: Context) : MigrationFileHelper {
    override fun getListFiles(dbMigrationPath: String): List<String> =
        context.assets.list(dbMigrationPath)?.toList() ?: listOf()

    override suspend fun executeMigration(
        fileName: String,
        dbFunction: suspend (queryString: String) -> Unit
    ) {
        try {
            val input: InputStream = context.assets.open(fileName)

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
                    runBlocking {
                        dbFunction(query.toString())
                    }
                    query = StringBuffer()
                }
            }

            val queryString = query.toString()
            if (queryString.trim { it <= ' ' }.isNotEmpty()) {
                dbFunction(queryString)
            }
        } catch (e: IOException) {
            FlowLog.log(
                FlowLog.Level.E,
                "Failed to execute $fileName. App might be in an inconsistent state!",
                e
            )
        }
    }
}