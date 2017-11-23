package com.raizlabs.android.dbflow.structure.database.transaction

import com.raizlabs.android.dbflow.sql.language.CursorResult
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: Provides an easy way to query for data asynchronously.
 */
class QueryTransaction<T : Any>(
        private val modelQueriable: ModelQueriable<T>,
        private val queryResultCallback: QueryResultCallback<T>? = null,
        private val queryResultListCallback: QueryResultListCallback<T>? = null,
        private val queryResultSingleCallback: QueryResultSingleCallback<T>? = null,
        private val runResultCallbacksOnSameThread: Boolean = true) : ITransaction {

    /**
     * Simple interface that provides callback on result.
     *
     * @param <T> The result that we got from querying.
     */
    interface QueryResultCallback<T : Any> {

        /**
         * Called when the query completes.
         *
         * @param transaction The transaction that ran.
         * @param tResult     The result of the query. Use this object to get data that you need.
         */
        fun onQueryResult(transaction: QueryTransaction<T>,
                          tResult: CursorResult<T>)
    }

    /**
     * Simple interface that provides [List] callback on result.
     *
     * @param <T> The result that we got from querying.
     */
    interface QueryResultListCallback<TResult> {

        /**
         * Called when the query completes.
         *
         * @param transaction The transaction that ran.
         * @param tResult     The [List] result of the query.
         */
        fun onListQueryResult(transaction: QueryTransaction<*>, tResult: List<TResult>)
    }

    /**
     * Simple interface that provides single [T] callback on result.
     *
     * @param <T> The result that we got from querying.
     */
    interface QueryResultSingleCallback<TResult> {

        /**
         * Called when the query completes.
         *
         * @param transaction The transaction that ran.
         * @param tResult     The single result of the query.
         */
        fun onSingleQueryResult(transaction: QueryTransaction<*>, tResult: TResult?)
    }

    internal constructor(builder: Builder<T>) : this(
            modelQueriable = builder.modelQueriable,
            queryResultCallback = builder.queryResultCallback,
            queryResultListCallback = builder.queryResultListCallback,
            queryResultSingleCallback = builder.queryResultSingleCallback,
            runResultCallbacksOnSameThread = builder.runResultCallbacksOnSameThread
    )

    override fun execute(databaseWrapper: DatabaseWrapper) {
        val cursorResult = modelQueriable.queryResults()
        if (queryResultCallback != null) {
            if (runResultCallbacksOnSameThread) {
                queryResultCallback.onQueryResult(this, cursorResult)
            } else {
                Transaction.transactionHandler.post {
                    queryResultCallback.onQueryResult(this@QueryTransaction, cursorResult)
                }
            }
        }

        if (queryResultListCallback != null) {
            val resultList = cursorResult.toListClose()
            if (runResultCallbacksOnSameThread) {
                queryResultListCallback.onListQueryResult(this, resultList)
            } else {
                Transaction.transactionHandler.post {
                    queryResultListCallback.onListQueryResult(this@QueryTransaction, resultList)
                }
            }
        }

        if (queryResultSingleCallback != null) {
            val result = cursorResult.toModelClose()
            if (runResultCallbacksOnSameThread) {
                queryResultSingleCallback.onSingleQueryResult(this, result)
            } else {
                Transaction.transactionHandler.post {
                    queryResultSingleCallback.onSingleQueryResult(this@QueryTransaction, result)
                }
            }
        }
    }

    /**
     * Provides easy way to build a [QueryTransaction].
     *
     * @param <T>
     */
    class Builder<T : Any>(internal val modelQueriable: ModelQueriable<T>) {
        internal var queryResultCallback: QueryResultCallback<T>? = null
        internal var queryResultListCallback: QueryResultListCallback<T>? = null
        internal var queryResultSingleCallback: QueryResultSingleCallback<T>? = null
        internal var runResultCallbacksOnSameThread: Boolean = false

        /**
         * Called when transaction completes and use this to get results.
         */
        fun queryResult(queryResultCallback: QueryResultCallback<T>?) = apply {
            this.queryResultCallback = queryResultCallback
        }

        /**
         * Called when transaction completes, which returns a [List] result.
         */
        fun queryListResult(queryResultListCallback: QueryResultListCallback<T>?) = apply {
            this.queryResultListCallback = queryResultListCallback
        }

        /**
         * Called when transaction completes, which returns a single [T].
         */
        fun querySingleResult(queryResultSingleCallback: QueryResultSingleCallback<T>?) = apply {
            this.queryResultSingleCallback = queryResultSingleCallback
        }

        /**
         * Runs result callback on UI thread by default. setting this to true would run the callback
         * synchrously on the same thread this transaction executes from.
         */
        fun runResultCallbacksOnSameThread(runResultCallbacksOnSameThread: Boolean) = apply {
            this.runResultCallbacksOnSameThread = runResultCallbacksOnSameThread
        }

        /**
         * @return A new [QueryTransaction]. Subsequent calls to this method produce new
         * instances.
         */
        fun build(): QueryTransaction<T> = QueryTransaction(this)
    }
}
