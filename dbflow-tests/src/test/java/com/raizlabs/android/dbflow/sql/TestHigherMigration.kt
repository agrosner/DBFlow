package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.sql.migration.Migration
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
@com.raizlabs.android.dbflow.annotation.Migration(version = 1, database = TestDatabase::class, priority = 2)
class TestHigherMigration : Migration {
    override fun onPreMigrate() {

    }

    override fun migrate(database: DatabaseWrapper) {

    }

    override fun onPostMigrate() {

    }
}
