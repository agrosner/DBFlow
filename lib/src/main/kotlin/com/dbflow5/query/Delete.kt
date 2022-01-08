package com.dbflow5.query

import com.dbflow5.sql.Query
import kotlin.reflect.KClass

/**
 * Description: Constructs the beginning of a SQL DELETE query
 */
class Delete internal constructor() : Query {

    override val query: String
        get() = "DELETE "

    /**
     * Returns the new SQL FROM statement wrapper
     *
     * @param table    The table we want to run this query from
     * @param [T] The table class
     * @return [T]
     **/
    infix fun <T : Any> from(table: KClass<T>): From<T> = From(this, table)

}

