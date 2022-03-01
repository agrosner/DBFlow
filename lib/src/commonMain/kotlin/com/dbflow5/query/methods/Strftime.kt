package com.dbflow5.query.methods

import com.dbflow5.query.operations.Method
import com.dbflow5.query.operations.method
import com.dbflow5.query.operations.sqlLiteralOf

/**
 * SQLite standard "strftime()" method.
 * See SQLite documentation on this method.
 */
val strftime = Strftime

object Strftime : StandardMethod {
    override val name: String = "strftime"

    operator fun invoke(
        formatString: String,
        timeString: String,
        vararg modifiers: String
    ): Method<String> = method(
        name,
        sqlLiteralOf(formatString),
        sqlLiteralOf(timeString),
        *modifiers.map { sqlLiteralOf(it) }.toTypedArray(),
    )
}