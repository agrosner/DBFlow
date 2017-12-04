package com.raizlabs.dbflow5.rx.query

import com.raizlabs.dbflow5.database.DatabaseStatement
import com.raizlabs.dbflow5.database.FlowCursor
import com.raizlabs.dbflow5.query.Delete
import com.raizlabs.dbflow5.query.Insert
import com.raizlabs.dbflow5.query.Queriable
import com.raizlabs.dbflow5.query.Set
import com.raizlabs.dbflow5.structure.Model
import rx.Single

/**
 * Description: Mirrors [Queriable] with RX constructs.
 */
interface RXQueriable {

    /**
     * @return An [Single] from the DB based on this query
     */
    fun query(): Single<FlowCursor>

    /**
     * @return An [Single] of [DatabaseStatement] from this query.
     */
    fun compileStatement(): Single<DatabaseStatement>

    /**
     * @return the long value of this query.
     */
    fun longValue(): Single<Long>

    /**
     * @return This may return the number of rows affected from a [Insert]  statement.
     * If not, returns [Model.INVALID_ROW_ID]
     */
    fun executeInsert(): Single<Long>

    /**
     * @return This may return the number of rows affected from a [Set] or [Delete] statement.
     * If not, returns [Model.INVALID_ROW_ID]
     */
    fun executeUpdateDelete(): Single<Long>

    /**
     * @return True if this query has data. It will run a [.count] greater than 0.
     */
    fun hasData(): Single<Boolean>

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    fun execute(): Single<Void>

}

inline val RXQueriable.cursor
    get() = query()

inline val RXQueriable.hasData
    get() = hasData()

inline val RXQueriable.statement
    get() = compileStatement()