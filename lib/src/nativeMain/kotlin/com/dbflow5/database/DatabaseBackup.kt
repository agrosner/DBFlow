package com.dbflow5.database

import co.touchlab.sqliter.DatabaseFileContext
import com.dbflow5.config.FlowLog
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import platform.Foundation.NSProcessInfo

actual class DatabaseBackup(
    private val generatedDatabase: GeneratedDatabase,
) {
    actual fun movePrepackaged(databaseName: String, prepackagedName: String) {
        val dest = DatabaseFileContext.databasePath(databaseName, null).toPath()
        val fileSystem = FileSystem.SYSTEM
        if (fileSystem.exists(dest) && (fileSystem.metadata(dest).size ?: 0L) > 0L) {
            return
        }
        val source = findResource(prepackagedName) ?: run {
            FlowLog.log(
                FlowLog.Level.W,
                "DatabaseBackup",
                "Could not retrieve file for $prepackagedName",
            )
            return
        }
        dest.parent?.let { fileSystem.createDirectories(it) }
        fileSystem.source(source).use { input ->
            fileSystem.sink(dest).buffer().use { output ->
                output.writeAll(input)
            }
        }
    }

    actual fun restoreDatabase(databaseName: String, prepackagedName: String) {
    }

    actual fun restoreBackup(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun backupDB() {
    }

    private fun findResource(name: String): Path? {
        val fileSystem = FileSystem.SYSTEM
        val envDir = (NSProcessInfo.processInfo.environment["DBFLOW_RESOURCES_DIR"] as? String)
            ?.toPath()
        val start = fileSystem.canonicalize(".".toPath())
        val searchRoots = listOfNotNull(envDir) + generateSequence(start) { it.parent }.take(8)
        return searchRoots
            .flatMap { dir ->
                listOf(
                    dir / name,
                    dir / "src" / "commonMain" / "resources" / name,
                    dir / "tests" / "src" / "commonMain" / "resources" / name,
                )
            }
            .firstOrNull(fileSystem::exists)
    }
}
