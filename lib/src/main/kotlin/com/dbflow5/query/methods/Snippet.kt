package com.dbflow5.query.methods

import com.dbflow5.adapter.makeLazySQLObjectAdapter
import com.dbflow5.query.operations.Method
import com.dbflow5.query.operations.method
import com.dbflow5.query.operations.sqlLiteralOf
import com.dbflow5.query.operations.tableNameLiteral

val snippet = Snippet

object Snippet : StandardMethod {
    override val name: String = "snippet"

    inline operator fun <reified Table : Any> invoke(
        start: String? = null,
        end: String? = null,
        ellipses: String? = null,
        index: Int? = null,
        approximateTokens: Int? = null,
    ): Method<String> {
        val args = listOfNotNull(start, end, ellipses, index, approximateTokens).map {
            sqlLiteralOf(it)
        }
        return method(
            name,
            makeLazySQLObjectAdapter(Table::class).tableNameLiteral(),
            *args.toTypedArray(),
        )
    }
}