package com.raizlabs.dbflow5.rx2.transaction

import com.raizlabs.dbflow5.transaction.Transaction
import io.reactivex.Maybe
import io.reactivex.MaybeObserver
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * Description: Wraps a [Transaction.Builder] in a transaction. Please note that the [Transaction.Builder]
 * success will get consumed by the [Observer].
 */
class MaybeTransaction<R : Any?>(private val builder: Transaction.Builder<R>)
    : Maybe<R>() {

    override fun subscribeActual(observer: MaybeObserver<in R>) {
        val transaction = builder.success { _, r ->
            observer.onSuccess(r)
            observer.onComplete()
        }
            .error { _, throwable -> observer.onError(throwable) }
            .build()
        observer.onSubscribe(object : Disposable {
            private var disposed = false

            override fun isDisposed() = disposed
            override fun dispose() {
                transaction.cancel()
                disposed = true
            }
        })

        transaction.execute()
    }
}