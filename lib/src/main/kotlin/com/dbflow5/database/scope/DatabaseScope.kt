package com.dbflow5.database.scope

import com.dbflow5.config.DBFlowDatabase

/**
 * Description: The main handle into the database.
 */
interface DatabaseScope<DB : DBFlowDatabase> {
    val db: DBFlowDatabase
}

interface ReadableDatabaseScope<DB : DBFlowDatabase> : DatabaseScope<DB>, ReadableScope,
    ReadableQueriableScope

interface WritableDatabaseScope<DB : DBFlowDatabase> : ReadableDatabaseScope<DB>, WritableScope,
    WritableQueriableScope
