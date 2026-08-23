package com.dbflow5.query.methods

import com.dbflow5.query.operations.Method
import com.dbflow5.query.operations.emptyMethod

val random by lazy { Random() }

object Random : StandardMethod {
    override val name: String = "RANDOM"

    operator fun invoke(): Method<Unit> = emptyMethod(
        name,
    )
}