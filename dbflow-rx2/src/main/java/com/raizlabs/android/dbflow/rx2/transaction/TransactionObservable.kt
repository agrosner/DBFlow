@file:JvmName("RXTransactions")

package com.raizlabs.android.dbflow.rx2.transaction

import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.transaction.ITransaction
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable

/**
 * Description: Returns a [Maybe] that executes the [transaction] when called.
 */
fun <R : Any?> DatabaseDefinition.beginMaybe(transaction: ITransaction<R>) =
        Maybe.fromCallable { transaction.execute(this) }

/**
 * Description: Returns a [Observable] that executes the [transaction] when called.
 */
fun <R : Any> DatabaseDefinition.beginObservable(transaction: ITransaction<R>) =
        Observable.fromCallable { transaction.execute(this) }

/**
 * Description: Returns a [Flowable] that executes the [transaction] when called.
 */
fun <R : Any?> DatabaseDefinition.beginFlowable(transaction: ITransaction<R>) =
        Flowable.fromCallable { transaction.execute(this) }