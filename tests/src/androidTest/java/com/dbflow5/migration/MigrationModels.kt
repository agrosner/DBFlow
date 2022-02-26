package com.dbflow5.migration

import com.dbflow5.annotation.Migration
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.scope.MigrationScope

@Migration(priority = 1, version = 1)
class FirstMigration : com.dbflow5.database.Migration {
    override suspend fun MigrationScope.migrate(database: DatabaseWrapper) {

    }
}

@Migration(priority = 2, version = 1)
class SecondMigration : com.dbflow5.database.Migration {
    override suspend fun MigrationScope.migrate(database: DatabaseWrapper) {

    }
}
