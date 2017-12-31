@file:JvmName("RXTransactions")

package com.raizlabs.dbflow5.rx2.transaction

import com.raizlabs.dbflow5.transaction.Transaction
import io.reactivex.Maybe
import io.reactivex.MaybeObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable

/**
 * Description: Returns a [Maybe] that executes the [this@beginMaybe] when called.
 */
fun <R : Any?> Transaction.Builder<R>.asMaybe(): MaybeTransaction<R> = MaybeTransaction(this)

/**
 * Description: Returns a [Observable] that executes the [this@beginObservable] when called.
 */
fun <R : Any> Transaction.Builder<R>.asSingle(): SingleTransaction<R> = SingleTransaction(this)

open class TransactionDisposable(private val transaction: Transaction<*>) : Disposable {
    private var disposed = false

    override fun isDisposed() = disposed
    override fun dispose() {
        transaction.cancel()
        disposed = true
    }
}

/**
 * Description: Wraps a [Transaction.Builder] in a transaction. Please note that the [Transaction.Builder]
 * success will get consumed by the [Observer].
 */
class SingleTransaction<R : Any>(private val builder: Transaction.Builder<R>)
    : Single<R>() {

    /**
     * The transaction on this [SingleObserver]. Will be null when not running.
     */
    var transaction: Transaction<R>? = null

    override fun subscribeActual(observer: SingleObserver<in R>) {
        val transaction = builder.success { _, r -> observer.onSuccess(r) }
            .error { _, throwable -> observer.onError(throwable) }
            .completion { transaction = null }
            .build()
        observer.onSubscribe(TransactionDisposable(transaction))
        this.transaction = transaction
        transaction.execute()
    }
}

/**
 * Description: Wraps a [Transaction.Builder] in a transaction. Please note that the [Transaction.Builder]
 * success will get consumed by the [Observer].
 */
class MaybeTransaction<R : Any?>(private val builder: Transaction.Builder<R>)
    : Maybe<R>() {

    /**
     * The transaction on this [SingleObserver]. Will be null when not running.
     */
    var transaction: Transaction<R>? = null

    override fun subscribeActual(observer: MaybeObserver<in R>) {
        val transaction = builder.success { _, r ->
            observer.onSuccess(r)
            observer.onComplete()
        }
            .completion { transaction = null }
            .error { _, throwable -> observer.onError(throwable) }
            .build()
        observer.onSubscribe(TransactionDisposable(transaction))
        this.transaction = transaction
        transaction.execute()
    }
}