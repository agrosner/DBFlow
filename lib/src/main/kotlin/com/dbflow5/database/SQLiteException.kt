package com.dbflow5.database

/**
 * Description: DBFlow mirror to an Android SQLiteException.
 */
class SQLiteException : RuntimeException {
    constructor()

    constructor(error: String) : super(error)

    constructor(error: String, cause: Throwable) : super(error, cause)
}

