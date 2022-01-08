package com.dbflow5.migration

import com.dbflow5.annotation.Migration
import com.dbflow5.database.DatabaseWrapper

@Migration(priority = 1, version = 1)
class FirstMigration : BaseMigration() {
    override fun migrate(database: DatabaseWrapper) {

    }
}

@Migration(priority = 2, version = 1)
class SecondMigration : BaseMigration() {
    override fun migrate(database: DatabaseWrapper) {

    }
}
