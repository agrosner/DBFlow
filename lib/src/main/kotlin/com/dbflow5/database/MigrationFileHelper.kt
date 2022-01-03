package com.dbflow5.database

/**
 * Description:
 */
interface MigrationFileHelper {

    fun getListFiles(dbMigrationPath: String): List<String>

    suspend fun executeMigration(fileName: String, dbFunction: suspend (queryString: String) -> Unit)
}