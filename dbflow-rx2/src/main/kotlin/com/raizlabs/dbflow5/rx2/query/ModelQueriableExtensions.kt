@file:JvmName("RXModelQueriable")

package com.raizlabs.dbflow5.rx2.query

import com.raizlabs.dbflow5.database.DBFlowDatabase
import com.raizlabs.dbflow5.query.ModelQueriable
import com.raizlabs.dbflow5.transaction.ITransactionQueue
import io.reactivex.Flowable

/**
 * Streams the results of this [ModelQueriable] through the [ITransactionQueue] and emitted one at
 * time.
 */
fun <T : Any> ModelQueriable<T>.queryStreamResults(dbFlowDatabase: DBFlowDatabase): Flowable<T> =
    CursorListFlowable(this, dbFlowDatabase)

