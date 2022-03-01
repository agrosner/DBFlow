package com.dbflow5.query.methods

import com.dbflow5.adapter.makeLazyDBRepresentable
import com.dbflow5.query.operations.Method
import com.dbflow5.query.operations.method
import com.dbflow5.query.operations.tableNameLiteral

val offsets = Offsets

object Offsets : StandardMethod {
    override val name: String = "offsets"

    inline operator fun <reified Table : Any> invoke(): Method<String> =
        method(
            name,
            makeLazyDBRepresentable(Table::class).tableNameLiteral(),
        )
}