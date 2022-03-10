package com.dbflow5.database

import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * Description:
 */
class NativeMigrationFileHelper : MigrationFileHelper {
    override fun getListFiles(dbMigrationPath: String): List<String> {
        return FileSystem.SYSTEM.list(dbMigrationPath.toPath()).map { it.toString() }
    }

    override fun executeMigration(fileName: String, dbFunction: (queryString: String) -> Unit) {
        // TODO: execute
    }
}