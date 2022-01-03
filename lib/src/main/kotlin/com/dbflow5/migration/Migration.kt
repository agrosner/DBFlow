package com.dbflow5.migration

import com.dbflow5.database.DatabaseWrapper

/**
 * Description: Called when the Database is migrating. We can perform custom migrations here. A [com.dbflow5.annotation.Migration]
 * is required for registering this class to automatically be called in an upgrade of the DB.
 */
interface Migration {

    /**
     * Called before we migrate data. Instantiate migration data before releasing it in [.onPostMigrate]
     */
    fun onPreMigrate()

    /**
     * Perform your migrations here
     *
     * @param database The database to operate on
     */
    suspend fun migrate(database: DatabaseWrapper)

    /**
     * Called after the migration completes. Release migration data here.
     */
    fun onPostMigrate()
}
