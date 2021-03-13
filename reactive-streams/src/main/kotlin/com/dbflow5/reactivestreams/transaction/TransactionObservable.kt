@file:JvmName("RXTransactions")

package com.dbflow5.reactivestreams.transaction

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.ModelQueriable
import com.dbflow5.reactivestreams.query.TableChangeOnSubscribe
import com.dbflow5.transaction.ITransactionQueue
import com.dbflow5.transaction.Transaction
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.MaybeObserver
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Description: Returns a [Maybe] that executes the [this@beginMaybe] when called.
 */
fun <R : Any?> Transaction.Builder<R>.asMaybe(): MaybeTransaction<R> = MaybeTransaction(this)

/**
 * Description: Returns a [Observable] that executes the [this@beginObservable] when called.
 */
fun <R : Any> Transaction.Builder<R>.asSingle(): SingleTransaction<R> = SingleTransaction(this)

/**
 * Observes any kind of table change from this [ModelQueriable], including individual model and global
 * table changes. The passed [evalFn] is used to determine by you what to run and return on the subscribe
 *  of the [Flowable]. Use the passed [DatabaseWrapper] in your [ModelQueriable] statement.
 *  The [evalFn] runs on the [ITransactionQueue].
 */
fun <T : Any, R> ModelQueriable<T>.asFlowable(
        evalFn: ModelQueriable<T>.(DatabaseWrapper) -> R): Flowable<R> =
        Flowable.create(TableChangeOnSubscribe(this, evalFn), BackpressureStrategy.LATEST)

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
        val transaction = builder.success { _, r -> observer.onSuccess(r) }
                .completion {
                    transaction = null
                    observer.onComplete()
                }
                .error { _, throwable -> observer.onError(throwable) }
                .build()
        observer.onSubscribe(TransactionDisposable(transaction))
        this.transaction = transaction
        transaction.execute()
    }
}