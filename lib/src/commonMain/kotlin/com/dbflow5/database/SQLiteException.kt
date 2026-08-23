package com.dbflow5.database

/**
 * Called when there is a low-level DB error reported.
 */
class SQLiteException : RuntimeException {

    constructor(error: String) : super(error)

    constructor(error: String, cause: Throwable) : super(error, cause)
}

