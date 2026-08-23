package com.dbflow5.query.methods

import com.dbflow5.query.operations.Method
import com.dbflow5.query.operations.method
import com.dbflow5.query.operations.sqlLiteralOf

/**
 * Sqlite "datetime" method. See SQLite documentation on this method.
 */
val datetime = DateTime

object DateTime : StandardMethod {
    override val name: String = "datetime"

    operator fun invoke(
        timeStamp: Long,
        vararg modifiers: String,
    ): Method<String> = method(
        name,
        sqlLiteralOf(timeStamp),
        *modifiers.map { sqlLiteralOf(it) }
            .toTypedArray(),
    )
}