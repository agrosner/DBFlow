package com.dbflow5

import com.dbflow5.annotation.Database
import com.dbflow5.annotation.Migration
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.migration.BaseMigration
import com.dbflow5.migration.UpdateTableMigration
import com.dbflow5.models.SimpleModel

/**
 * Description:
 */
@Database(version = 1)
abstract class TestDatabase : DBFlowDatabase() {

    @Migration(version = 1, database = TestDatabase::class, priority = 5)
    class TestMigration : UpdateTableMigration<SimpleModel>(SimpleModel::class.java)

    @Migration(version = 1, database = TestDatabase::class, priority = 1)
    class SecondMigration : BaseMigration() {
        override fun migrate(database: DatabaseWrapper) {

        }
    }
}