package com.raizlabs.android.dbflow.migration

import com.raizlabs.android.dbflow.database.DatabaseWrapper

/**
 * Description: Called when the Database is migrating. We can perform custom migrations here. A [com.raizlabs.android.dbflow.annotation.Migration]
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
    fun migrate(database: DatabaseWrapper)

    /**
     * Called after the migration completes. Release migration data here.
     */
    fun onPostMigrate()
}
