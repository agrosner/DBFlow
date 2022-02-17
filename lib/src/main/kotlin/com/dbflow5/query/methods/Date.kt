package com.dbflow5.query.methods

import com.dbflow5.query.operations.Method
import com.dbflow5.query.operations.method
import com.dbflow5.query.operations.sqlLiteralOf

/**
 * Sqlite "date" method. See SQLite documentation on this method.
 */
val date = Date

object Date : StandardMethod {
    override val name: String = "date"

    operator fun invoke(
        timeString: String,
        vararg modifiers: String,
    ): Method<String> = method(
        name,
        sqlLiteralOf(timeString),
        *modifiers.map { sqlLiteralOf(it) }
            .toTypedArray(),
    )
}