package com.dbflow5.migration

import com.dbflow5.database.DatabaseWrapper

/**
 * Description: Provides the base implementation of [Migration] with
 * only [Migration.migrate] needing to be implemented.
 */
abstract class BaseMigration : Migration {


    override fun onPreMigrate() {

    }

    abstract override suspend fun migrate(database: DatabaseWrapper)

    override fun onPostMigrate() {

    }
}
