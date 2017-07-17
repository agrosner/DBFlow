package com.raizlabs.android.dbflow

import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.Migration
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.sql.migration.UpdateTableMigration

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