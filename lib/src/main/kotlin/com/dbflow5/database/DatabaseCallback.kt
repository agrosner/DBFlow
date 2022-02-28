package com.dbflow5.database

/**
 * Description: Provides callbacks for [OpenHelper] methods
 */
interface DatabaseCallback {

    /**
     * Called when the DB is opened
     *
     * @param db The database that is opened
     */
    fun onOpen(db: DatabaseWrapper) = Unit

    /**
     * Called when the DB is created
     *
     * @param db The database that is created
     */
    fun onCreate(db: DatabaseWrapper) = Unit

    /**
     * Called when the DB is upgraded.
     *
     * @param db   The database that is upgraded
     * @param oldVersion The previous DB version
     * @param newVersion The new DB version
     */
    fun onUpgrade(db: DatabaseWrapper, oldVersion: Int, newVersion: Int) = Unit

    /**
     * Called when DB is downgraded. Note that this may not be supported by all implementations of the DB.
     *
     * @param db The database downgraded.
     * @param oldVersion      The old. higher version.
     * @param newVersion      The new lower version.
     */
    fun onDowngrade(db: DatabaseWrapper, oldVersion: Int, newVersion: Int) = Unit

    /**
     * Called when DB connection is being configured. Useful for checking foreign key support or enabling
     * write-ahead-logging.
     */
    fun onConfigure(db: DatabaseWrapper) = Unit
}
