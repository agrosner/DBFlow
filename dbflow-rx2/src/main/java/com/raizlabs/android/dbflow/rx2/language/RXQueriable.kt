package com.raizlabs.android.dbflow.rx2.language

import android.database.Cursor
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.Insert
import com.raizlabs.android.dbflow.sql.language.Set
import com.raizlabs.android.dbflow.sql.queriable.Queriable
import com.raizlabs.android.dbflow.structure.Model
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

/**
 * Description: Mirrors [Queriable] with RX constructs.
 */
interface RXQueriable {

    /**
     * @return An [Single] from the DB based on this query
     */
    fun query(): Maybe<Cursor>

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
    fun execute(): Completable

}


inline val RXQueriable.cursor
    get() = query()

inline val RXQueriable.hasData
    get() = hasData()

inline val RXQueriable.statement
    get() = compileStatement()
