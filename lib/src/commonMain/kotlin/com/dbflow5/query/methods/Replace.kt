package com.dbflow5.query.methods

import com.dbflow5.query.operations.AnyProperty
import com.dbflow5.query.operations.Method
import com.dbflow5.query.operations.method
import com.dbflow5.query.operations.sqlLiteralOf

val replace = Replace

object Replace : StandardMethod {
    override val name: String = "REPLACE"

    operator fun invoke(
        property: AnyProperty,
        findString: String,
        replacement: String
    ): Method<String> = method(
        name, property,
        sqlLiteralOf(findString),
        sqlLiteralOf(replacement),
    )
}