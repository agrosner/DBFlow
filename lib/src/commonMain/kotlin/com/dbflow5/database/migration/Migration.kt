package com.dbflow5.database.migration

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.scope.MigrationScope

/**
 * Description: Called when the Database is migrating. We can perform custom migrations here. A [com.dbflow5.annotation.Migration]
 * is required for registering this class to automatically be called in an upgrade of the DB.
 */
fun interface Migration {

    /**
     * Perform your migrations here
     *
     * @param database The database to operate on
     */
    suspend fun MigrationScope.migrate(database: DatabaseWrapper)

}
