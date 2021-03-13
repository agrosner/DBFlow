package com.dbflow5.database

/**
 * Description:
 */
interface MigrationFileHelper {

    fun getListFiles(dbMigrationPath: String): List<String>

    fun executeMigration(fileName: String, dbFunction: (queryString: String) -> Unit)
}