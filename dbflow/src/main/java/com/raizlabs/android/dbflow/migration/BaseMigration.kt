package com.raizlabs.android.dbflow.migration

import com.raizlabs.android.dbflow.database.DatabaseWrapper

/**
 * Description: Provides the base implementation of [com.raizlabs.android.dbflow.sql.migration.Migration] with
 * only [Migration.migrate] needing to be implemented.
 */
abstract class BaseMigration : Migration {


    override fun onPreMigrate() {

    }

    abstract override fun migrate(database: DatabaseWrapper)

    override fun onPostMigrate() {

    }
}
