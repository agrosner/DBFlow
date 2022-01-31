package com.dbflow5.database.scope

import com.dbflow5.config.DBFlowDatabase

/**
 * Description: The main handle into the database.
 */
interface DatabaseScope<DB : DBFlowDatabase> {

    /**
     * Database handle.
     */
    val db: DB
}

interface WritableDatabaseScope<DB : DBFlowDatabase> : DatabaseScope<DB>, WritableScope
