package com.dbflow5.query.methods

import com.dbflow5.query.operations.AnyOperator
import com.dbflow5.query.operations.Method
import com.dbflow5.query.operations.method

/**
 * Constructs using the "IFNULL" method in SQLite. It will pick the first non null
 * value and set that. If both are NULL then it will use NULL.
 */
val ifNull = IfNull

object IfNull : StandardMethod {
    override val name: String = "IFNULL"

    operator fun invoke(
        first: AnyOperator,
        secondIfFirstNull: AnyOperator,
    ): Method<Any?> = method(
        name,
        first,
        secondIfFirstNull,
    )
}