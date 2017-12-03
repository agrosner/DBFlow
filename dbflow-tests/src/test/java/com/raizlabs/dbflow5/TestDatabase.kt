package com.raizlabs.dbflow5

import com.raizlabs.dbflow5.annotation.Database
import com.raizlabs.dbflow5.annotation.Migration
import com.raizlabs.dbflow5.migration.UpdateTableMigration
import com.raizlabs.dbflow5.models.SimpleModel

/**
 * Description:
 */
@Database(version = TestDatabase.VERSION)
object TestDatabase {

    const val VERSION = 1

    @Migration(version = 1, database = TestDatabase::class)
    class TestMigration : UpdateTableMigration<SimpleModel>(SimpleModel::class.java) {
        override fun onPostMigrate() {
            super.onPostMigrate()
        }
    }

}