package com.dbflow5.database.scope

import com.dbflow5.config.DBFlowDatabase

/**
 * Description: The main handle into the database.
 */
interface DatabaseScope<DB : DBFlowDatabase>: WritableScope {

    /**
     * Database handle.
     */
    val db: DBFlowDatabase
}

