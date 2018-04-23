package com.raizlabs.dbflow5.database

actual class PlatformMigrationHelper(private val db: DBFlowDatabase) : MigrationHelper {

    override fun executeMigration(db: DatabaseWrapper, version: Int) {
        TODO("not implemented")
    }
}