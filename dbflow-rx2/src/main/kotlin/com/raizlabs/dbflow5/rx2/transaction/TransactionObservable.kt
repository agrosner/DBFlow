@file:JvmName("RXTransactions")

package com.raizlabs.dbflow5.rx2.transaction

import com.raizlabs.dbflow5.config.DBFlowDatabase
import com.raizlabs.dbflow5.transaction.ITransaction
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable

/**
 * Description: Returns a [Maybe] that executes the [transaction] when called.
 */
fun <R : Any?> DBFlowDatabase.beginMaybe(transaction: ITransaction<R>) =
        Maybe.fromCallable { transaction.execute(this) }

/**
 * Description: Returns a [Observable] that executes the [transaction] when called.
 */
fun <R : Any> DBFlowDatabase.beginObservable(transaction: ITransaction<R>) =
        Observable.fromCallable { transaction.execute(this) }

/**
 * Description: Returns a [Flowable] that executes the [transaction] when called.
 */
fun <R : Any?> DBFlowDatabase.beginFlowable(transaction: ITransaction<R>) =
        Flowable.fromCallable { transaction.execute(this) }