package com.dbflow5.query.methods

import com.dbflow5.query.operations.AnyOperator
import com.dbflow5.query.operations.Method
import com.dbflow5.query.operations.method

/**
 * Constructs using the "NULLIF" method in SQLite. If both expressions are equal, then
 * NULL is set into the DB.
 */
val nullIf = NullIf

object NullIf : StandardMethod {
    override val name: String = "NULLIF"

    operator fun invoke(
        first: AnyOperator,
        second: AnyOperator
    ): Method<Any?> = method(
        name,
        first,
        second,
    )
}