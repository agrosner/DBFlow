package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.appendQualifier
import com.raizlabs.dbflow5.sql.Query

/**
 * Description: The condition that represents EXISTS in a SQL statement.
 */
class ExistenceOperator(private val innerWhere: Where<*>) : SQLOperator, Query {

    override val query: String
        get() = appendToQuery()

    override fun appendConditionToQuery(queryBuilder: StringBuilder) {
        queryBuilder.appendQualifier("EXISTS", "(" + innerWhere.query.trim({ it <= ' ' }) + ")")
    }

    override fun columnName(): String {
        throw RuntimeException("Method not valid for ExistenceOperator")
    }

    override fun separator(): String? {
        throw RuntimeException("Method not valid for ExistenceOperator")
    }

    override fun separator(separator: String): SQLOperator {
        // not used.
        throw RuntimeException("Method not valid for ExistenceOperator")
    }

    override fun hasSeparator(): Boolean = false

    override fun operation(): String = ""

    override fun value(): Any? = innerWhere

}
