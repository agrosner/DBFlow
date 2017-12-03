package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.sql.Query
import com.raizlabs.dbflow5.structure.ChangeAction
import com.raizlabs.dbflow5.structure.Model
import com.raizlabs.dbflow5.database.DatabaseStatement
import com.raizlabs.dbflow5.database.FlowCursor

/**
 * Description: The most basic interface that some of the classes such as [Insert], [ModelQueriable],
 * [Set], and more implement for convenience.
 */
interface Queriable : Query {

    val primaryAction: ChangeAction

    /**
     * @return A cursor from the DB based on this query
     */
    fun query(): FlowCursor?

    /**
     * @return A new [DatabaseStatement] from this query.
     */
    fun compileStatement(): DatabaseStatement

    /**
     * @return the long value of the results of query.
     */
    fun longValue(): Long

    /**
     * @return This may return the number of rows affected from a [Set] or [Delete] statement.
     * If not, returns [Model.INVALID_ROW_ID]
     */
    fun executeUpdateDelete(): Long

    /**
     * @return This may return the number of rows affected from a [Insert]  statement.
     * If not, returns [Model.INVALID_ROW_ID]
     */
    fun executeInsert(): Long

    /**
     * @return True if this query has data. It will run a [.count] greater than 0.
     */
    fun hasData(): Boolean

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    fun execute()

}

inline val Queriable.cursor
    get() = query()

inline val Queriable.hasData
    get() = hasData()

inline val Queriable.statement
    get() = compileStatement()
