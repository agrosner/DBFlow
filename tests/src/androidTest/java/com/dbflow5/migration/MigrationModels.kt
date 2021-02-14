package com.dbflow5.migration

import com.dbflow5.TestDatabase
import com.dbflow5.annotation.Migration
import com.dbflow5.database.DatabaseWrapper

@Migration(database = TestDatabase::class, priority = 1, version = 1)
class FirstMigration : BaseMigration() {
    override fun migrate(database: DatabaseWrapper) {

    }
}

@Migration(database = TestDatabase::class, priority = 2, version = 1)
class SecondMigration : BaseMigration() {
    override fun migrate(database: DatabaseWrapper) {

    }
}
