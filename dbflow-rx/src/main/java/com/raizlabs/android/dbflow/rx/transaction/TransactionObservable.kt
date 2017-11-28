@file:JvmName("RXTransactions")

package com.raizlabs.android.dbflow.rx.transaction

import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.transaction.ITransaction
import rx.Observable
import rx.Single

/**
 * Description: Returns a [Maybe] that executes the [transaction] when called.
 */
fun <R : Any?> DatabaseDefinition.beginSingle(transaction: ITransaction<R>) =
        Single.fromCallable { transaction.execute(this) }

/**
 * Description: Returns a [Observable] that executes the [transaction] when called.
 */
fun <R : Any> DatabaseDefinition.beginObservable(transaction: ITransaction<R>) =
        Observable.fromCallable { transaction.execute(this) }
