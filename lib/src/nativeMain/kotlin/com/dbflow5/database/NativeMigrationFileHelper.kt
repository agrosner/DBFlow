package com.dbflow5.database

import com.dbflow5.database.migration.MigrationFileHelper
import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * Description:
 */
class NativeMigrationFileHelper : MigrationFileHelper {
    override fun getListFiles(dbMigrationPath: String): List<String> {
        return FileSystem.SYSTEM.listOrNull(dbMigrationPath.toPath())?.map { it.toString() } ?: listOf()
    }

    override fun executeMigration(fileName: String, dbFunction: (queryString: String) -> Unit) {
        // TODO: execute
    }
}