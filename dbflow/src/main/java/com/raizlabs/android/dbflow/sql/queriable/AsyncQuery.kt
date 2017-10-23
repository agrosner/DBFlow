package com.raizlabs.android.dbflow.sql.queriable

import com.raizlabs.android.dbflow.sql.BaseAsyncObject
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction.*

/**
 * Description: Adds async methods to a [ModelQueriable]
 */
class AsyncQuery<TModel>
/**
 * Constructs an instance of this async query.
 *
 * @param queriable The queriable object to use to query data.
 */
(private val modelQueriable: ModelQueriable<TModel>) : BaseAsyncObject<AsyncQuery<TModel>>(modelQueriable.table) {

    private var queryResultCallback: QueryResultCallback<TModel>? = null
    private var queryResultListCallback: QueryResultListCallback<TModel>? = null
    private var queryResultSingleCallback: QueryResultSingleCallback<TModel>? = null

    /**
     * @param queryResultCallback Called when query is executed and has a result.
     */
    fun queryResultCallback(queryResultCallback: QueryResultCallback<TModel>) = apply {
        this.queryResultCallback = queryResultCallback
    }

    /**
     * @param queryResultSingleCallback Called when query is executed and has a result.
     */
    fun querySingleResultCallback(queryResultSingleCallback: QueryResultSingleCallback<TModel>) = apply {
        this.queryResultSingleCallback = queryResultSingleCallback
    }

    /**
     * @param queryResultListCallback Called when query is executed and has a result.
     */
    fun queryListResultCallback(queryResultListCallback: QueryResultListCallback<TModel>) = apply {
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
    override val table: Class<TModel>
        get() = modelQueriable.table

}
