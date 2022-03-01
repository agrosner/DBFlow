package com.dbflow5.database

import com.dbflow5.config.GeneratedDatabase

expect class DatabaseBackup {

    fun movePrepackaged(databaseName: String, prepackagedName: String)

    fun restoreDatabase(databaseName: String, prepackagedName: String)

    fun restoreBackup(): Boolean

    fun backupDB()
}

internal val TEMP_DB_NAME = "temp-"

fun getTempDbFileName(databaseDefinition: GeneratedDatabase): String =
    "$TEMP_DB_NAME${databaseDefinition.databaseName}.db"
