package com.raizlabs.dbflow5.migration

import com.raizlabs.dbflow5.database.DatabaseWrapper

/**
 * Description: Provides the base implementation of [Migration] with
 * only [Migration.migrate] needing to be implemented.
 */
abstract class BaseMigration : Migration {


    override fun onPreMigrate() {

    }

    abstract override fun migrate(database: DatabaseWrapper)

    override fun onPostMigrate() {

    }
}
