@file:JvmName("RXModelQueriable")

package com.raizlabs.dbflow5.rx2.query

import com.raizlabs.dbflow5.config.DBFlowDatabase
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.ModelQueriable
import com.raizlabs.dbflow5.transaction.ITransactionQueue
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

fun <T : Any> ModelQueriable<T>.queryStreamResults(dbFlowDatabase: DBFlowDatabase): Flowable<T> =
    CursorResultFlowable(this, dbFlowDatabase)

/**
 * Observes any kind of table change from this [ModelQueriable], including individual model and global
 * table changes. The passed [evalFn] is used to determine by you what to run and return on the subscribe
 *  of the [Flowable]. Use the passed [DatabaseWrapper] in your [ModelQueriable] statement.
 *  The [evalFn] runs on the [ITransactionQueue].
 */
fun <T : Any, R> ModelQueriable<T>.observeOnTableChanges(
    evalFn: (DatabaseWrapper, ModelQueriable<T>) -> R): Flowable<R> =
    Flowable.create(TableChangeOnSubscribe(this, evalFn), BackpressureStrategy.LATEST)