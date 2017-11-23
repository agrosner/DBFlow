package com.raizlabs.android.dbflow.sql.queriable

import com.raizlabs.android.dbflow.sql.BaseAsyncObject
import com.raizlabs.android.dbflow.sql.language.CursorResult
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction.*

/**
 * Description: Adds async methods to a [ModelQueriable]
 */
class AsyncQuery<T : Any>
/**
 * Constructs an instance of this async query.
 *
 * @param queriable The queriable object to use to query data.
 */
(private val modelQueriable: ModelQueriable<T>) : BaseAsyncObject<AsyncQuery<T>>(modelQueriable.table) {

    private var queryResultCallback: QueryResultCallback<T>? = null
    private var queryResultListCallback: QueryResultListCallback<T>? = null
    private var queryResultSingleCallback: QueryResultSingleCallback<T>? = null

    /**
     * @param queryResultCallback Called when query is executed and has a result.
     */
    fun queryResultCallback(queryResultCallback: QueryResultCallback<T>) = apply {
        this.queryResultCallback = queryResultCallback
    }

    /**
     * @param queryResultSingleCallback Called when query is executed and has a result.
     */
    fun querySingleResultCallback(queryResultSingleCallback: QueryResultSingleCallback<T>) = apply {
        this.queryResultSingleCallback = queryResultSingleCallback
    }

    /**
     * @param queryResultListCallback Called when query is executed and has a result.
     */
    fun queryListResultCallback(queryResultListCallback: QueryResultListCallback<T>) = apply {
        this.queryResultListCallback = queryResultListCallback
    }

    /**
     * Runs the specified query in the background.
     */
    fun execute() {
        executeTransaction(QueryTransaction.Builder(modelQueriable)
                .queryResult(queryResultCallback)
                .queryListResult(queryResultListCallback)
                .querySingleResult(queryResultSingleCallback)
                .build())
    }

    /**
     * @return The table this Query is associated with.
     */
    override val table: Class<T>
        get() = modelQueriable.table

}

infix inline fun <T : Any> AsyncQuery<T>.list(crossinline callback: (QueryTransaction<*>, List<T>) -> Unit)
        = queryListResultCallback(object : QueryResultListCallback<T> {
    override fun onListQueryResult(transaction: QueryTransaction<*>, tResult: List<T>) {
        callback(transaction, tResult)
    }
}).execute()

infix inline fun <T : Any> AsyncQuery<T>.result(crossinline callback: (QueryTransaction<*>, T?) -> Unit)
        = querySingleResultCallback(object : QueryResultSingleCallback<T> {
    override fun onSingleQueryResult(transaction: QueryTransaction<*>, tResult: T?) {
        callback(transaction, tResult)
    }
}).execute()

infix inline fun <T : Any> AsyncQuery<T>.cursorResult(crossinline callback: (QueryTransaction<*>, CursorResult<T>) -> Unit)
        = queryResultCallback(object : QueryResultCallback<T> {
    override fun onQueryResult(transaction: QueryTransaction<T>, tResult: CursorResult<T>) {
        callback(transaction, tResult)
    }
}).execute()