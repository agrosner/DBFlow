@file:JvmName("RXTransactions")

package com.raizlabs.dbflow5.rx.transaction

import com.raizlabs.dbflow5.config.DBFlowDatabase
import com.raizlabs.dbflow5.transaction.ITransaction
import rx.Observable
import rx.Single

/**
 * Description: Returns a [Maybe] that executes the [transaction] when called.
 */
fun <R : Any?> DBFlowDatabase.beginSingle(transaction: ITransaction<R>) =
        Single.fromCallable { transaction.execute(this) }

/**
 * Description: Returns a [Observable] that executes the [transaction] when called.
 */
fun <R : Any> DBFlowDatabase.beginObservable(transaction: ITransaction<R>) =
        Observable.fromCallable { transaction.execute(this) }
