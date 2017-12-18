@file:JvmName("RXTransactions")

package com.raizlabs.dbflow5.rx2.transaction

import com.raizlabs.dbflow5.transaction.Transaction
import io.reactivex.Maybe
import io.reactivex.Observable

/**
 * Description: Returns a [Maybe] that executes the [this@beginMaybe] when called.
 */
fun <R : Any?> Transaction.Builder<R>.asMaybe(): Maybe<R> = MaybeTransaction(this)

/**
 * Description: Returns a [Observable] that executes the [this@beginObservable] when called.
 */
fun <R : Any> Transaction.Builder<R>.asObservable(): Observable<R> = ObservableTransaction(this)