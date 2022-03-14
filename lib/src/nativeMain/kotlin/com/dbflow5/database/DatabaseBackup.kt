package com.dbflow5.database

actual class DatabaseBackup(
    private val generatedDatabase: GeneratedDatabase,
) {
    actual fun movePrepackaged(databaseName: String, prepackagedName: String) {
    }

    actual fun restoreDatabase(databaseName: String, prepackagedName: String) {
    }

    actual fun restoreBackup(): Boolean {
        TODO("Not yet implemented")
    }

    actual fun backupDB() {
    }
}