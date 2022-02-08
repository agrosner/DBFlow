@file:JvmName("RXTransactions")

package com.dbflow5.reactivestreams.transaction

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query2.ExecutableQuery
import com.dbflow5.query2.HasAssociatedAdapters
import com.dbflow5.query2.SelectResult
import com.dbflow5.reactivestreams.query.TableChangeOnSubscribe
import com.dbflow5.transaction.Transaction
import com.dbflow5.transaction.TransactionDispatcher
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
fun <DB : DBFlowDatabase, R : Any> Transaction.Builder<DB, R>.asMaybe(): MaybeTransaction<DB, R> =
    MaybeTransaction(this)

/**
 * Description: Returns a [Observable] that executes the [this@beginObservable] when called.
 */
fun <DB : DBFlowDatabase, R : Any> Transaction.Builder<DB, R>.asSingle(): SingleTransaction<DB, R> =
    SingleTransaction(this)

/**
 * Observes any kind of table change from this [ModelQueriable], including individual model and global
 * table changes. The passed [evalFn] is used to determine by you what to run and return on the subscribe
 *  of the [Flowable]. Use the passed [DatabaseWrapper] in your [ModelQueriable] statement.
 *  The [evalFn] runs on the [TransactionDispatcher].
 */
fun <Table : Any, Result : Any, Q> Q.asFlowable(
    db: DBFlowDatabase,
    selectResultFn: suspend SelectResult<Table>.() -> Result,
): Flowable<Result>
    where Q : ExecutableQuery<SelectResult<Table>>,
          Q : HasAssociatedAdapters =
    Flowable.create(TableChangeOnSubscribe(this, selectResultFn, db), BackpressureStrategy.LATEST)

open class TransactionDisposable(private val transaction: Transaction<*, *>) : Disposable {
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
class SingleTransaction<DB : DBFlowDatabase, R : Any>(private val builder: Transaction.Builder<DB, R>) :
    Single<R>() {

    /**
     * The transaction on this [SingleObserver]. Will be null when not running.
     */
    private var _transaction: Transaction<DB, R>? = null

    val transaction: Transaction<DB, R>?
        get() = _transaction

    override fun subscribeActual(observer: SingleObserver<in R>) {
        val transaction = builder.success { _, r -> observer.onSuccess(r) }
            .error { _, throwable -> observer.onError(throwable) }
            .completion { _transaction = null }
            .build()
        observer.onSubscribe(TransactionDisposable(transaction))
        this._transaction = transaction
        transaction.enqueue()
    }
}

/**
 * Description: Wraps a [Transaction.Builder] in a transaction. Please note that the [Transaction.Builder]
 * success will get consumed by the [Observer].
 */
class MaybeTransaction<DB : DBFlowDatabase, R : Any>(private val builder: Transaction.Builder<DB, R>) :
    Maybe<R>() {

    /**
     * The transaction on this [SingleObserver]. Will be null when not running.
     */
    private var _transaction: Transaction<DB, R>? = null
    val transaction: Transaction<DB, R>?
        get() = _transaction

    override fun subscribeActual(observer: MaybeObserver<in R>) {
        val transaction = builder.success { _, r -> observer.onSuccess(r) }
            .completion {
                _transaction = null
                observer.onComplete()
            }
            .error { _, throwable -> observer.onError(throwable) }
            .build()
        observer.onSubscribe(TransactionDisposable(transaction))
        this._transaction = transaction
        transaction.enqueue()
    }
}