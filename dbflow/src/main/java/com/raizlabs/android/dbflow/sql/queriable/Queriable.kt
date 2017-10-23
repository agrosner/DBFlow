package com.raizlabs.android.dbflow.sql.queriable

import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.Insert
import com.raizlabs.android.dbflow.sql.language.Set
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.Model
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.FlowCursor

/**
 * Description: The most basic interface that some of the classes such as [Insert], [ModelQueriable],
 * [Set], and more implement for convenience.
 */
interface Queriable : Query {

    val primaryAction: BaseModel.Action

    /**
     * @return A cursor from the DB based on this query
     */
    fun query(): FlowCursor?

    /**
     * Allows you to pass in a [DatabaseWrapper] manually.
     *
     * @param databaseWrapper The wrapper to pass in.
     * @return A cursor from the DB based on this query
     */
    fun query(databaseWrapper: DatabaseWrapper): FlowCursor?


    /**
     * @return A new [DatabaseStatement] from this query.
     */
    fun compileStatement(): DatabaseStatement

    /**
     * @param databaseWrapper The wrapper to use.
     * @return A new [DatabaseStatement] from this query with database specified.
     */
    fun compileStatement(databaseWrapper: DatabaseWrapper): DatabaseStatement

    /**
     * @return the count of the results of the query.
     */
    @Deprecated("use {@link #longValue()}")
    fun count(): Long

    /**
     * @return the long value of the results of query.
     */
    fun longValue(): Long

    /**
     * @return the long value of the results of query.
     */
    fun longValue(databaseWrapper: DatabaseWrapper): Long

    /**
     * Allows you to pass in a [DatabaseWrapper] manually.
     *
     * @return the count of the results of the query.
     */
    @Deprecated("use {@link #longValue(DatabaseWrapper)}")
    fun count(databaseWrapper: DatabaseWrapper): Long

    /**
     * @return This may return the number of rows affected from a [Set] or [Delete] statement.
     * If not, returns [Model.INVALID_ROW_ID]
     */
    fun executeUpdateDelete(databaseWrapper: DatabaseWrapper): Long

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
     * @return This may return the number of rows affected from a [Insert]  statement.
     * If not, returns [Model.INVALID_ROW_ID]
     */
    fun executeInsert(databaseWrapper: DatabaseWrapper): Long

    /**
     * @return True if this query has data. It will run a [.count] greater than 0.
     */
    fun hasData(): Boolean

    /**
     * Allows you to pass in a [DatabaseWrapper] manually.
     *
     * @return True if this query has data. It will run a [.count] greater than 0.
     */
    fun hasData(databaseWrapper: DatabaseWrapper): Boolean

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    fun execute()

    /**
     * Will not return a result, rather simply will execute a SQL statement. Use this for non-SELECT statements or when
     * you're not interested in the result.
     */
    fun execute(databaseWrapper: DatabaseWrapper)
}
