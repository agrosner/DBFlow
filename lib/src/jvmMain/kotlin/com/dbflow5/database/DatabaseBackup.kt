package com.dbflow5.database

import com.dbflow5.config.FlowLog
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

actual class DatabaseBackup(
    private val generatedDatabase: GeneratedDatabase,
    private val databaseWriter: DatabaseWriter = DatabaseWriter(),
) {

    actual fun movePrepackaged(databaseName: String, prepackagedName: String) {
        val database = File(databaseName)
        if (database.exists()) {
            return
        }
        try {
            val existingDB = File(getTempDbFileName(generatedDatabase))
            val input = when {
                existingDB.exists() -> FileInputStream(existingDB)
                else -> openResource(prepackagedName)
            } ?: return
            databaseWriter.write(database, input)
        } catch (e: IOException) {
            FlowLog.log(FlowLog.Level.W, "Failed to open file", throwable = e)
        }
    }

    actual fun restoreDatabase(databaseName: String, prepackagedName: String) {
    }

    actual fun restoreBackup(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun backupDB() {
    }

    private fun openResource(name: String): InputStream? {
        val loaders = listOfNotNull(
            Thread.currentThread().contextClassLoader,
            DatabaseBackup::class.java.classLoader,
        )
        return loaders.firstNotNullOfOrNull { loader -> loader.getResourceAsStream(name) }
            ?: File(name).takeIf { it.exists() }?.let(::FileInputStream)
    }
}
