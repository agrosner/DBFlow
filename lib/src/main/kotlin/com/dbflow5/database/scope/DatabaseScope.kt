package com.dbflow5.database.scope

import com.dbflow5.config.GeneratedDatabase

/**
 * Description: The main handle into the database.
 */
interface DatabaseScope<DB : GeneratedDatabase> {
    val db: DB
}

interface ReadableDatabaseScope<DB : GeneratedDatabase> : DatabaseScope<DB>, ReadableScope,
    ReadableQueryScope

interface WritableDatabaseScope<DB : GeneratedDatabase> : ReadableDatabaseScope<DB>, WritableScope,
    WritableQueryScope
