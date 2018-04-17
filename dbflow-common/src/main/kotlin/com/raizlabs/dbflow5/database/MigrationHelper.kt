package com.raizlabs.dbflow5.database

interface MigrationHelper {

    fun executeMigration(db: DatabaseWrapper, version: Int)
}

expect class PlatformMigrationHelper : MigrationHelper
