@file:JvmName("RXModelQueriable")

package com.dbflow5.reactivestreams.query

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.query.ModelQueriable
import com.dbflow5.transaction.TransactionDispatcher
import io.reactivex.rxjava3.core.Flowable

/**
 * Streams the results of this [ModelQueriable] through the [TransactionDispatcher] and emitted one at
 * time.
 */
fun <T : Any> ModelQueriable<T>.queryStreamResults(dbFlowDatabase: DBFlowDatabase): Flowable<T> =
    CursorListFlowable(this, dbFlowDatabase)

