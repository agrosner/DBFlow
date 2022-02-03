package com.dbflow5.reactivestreams.query

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.FlowLog
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query.list.FlowCursorList
import com.dbflow5.reactivestreams.transaction.asSingle
import com.dbflow5.transaction.Transaction
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.atomic.AtomicLong

/**
 * Description: Wraps a [ModelQueriable] into a [Flowable] that emits each item from the
 * result of the [ModelQueriable] one at a time.
 */
class CursorListFlowable<DB : DBFlowDatabase, T : Any>(
    private val modelQueriable: ModelQueriable<T>,
    private val database: DB
) : Flowable<T>() {

    override fun subscribeActual(subscriber: Subscriber<in T>) {
        subscriber.onSubscribe(object : Subscription {
            private var transaction: Transaction<DB, FlowCursorList<T>>? = null

            override fun request(n: Long) {
                val single = database
                    .beginTransactionAsync { modelQueriable.cursorList(db) }
                    .asSingle()
                transaction = single.transaction
                single.subscribe(CursorResultObserver(subscriber, n))
            }

            override fun cancel() {
                transaction?.cancel()
            }
        })
    }

    internal class CursorResultObserver<T : Any>(
        private val subscriber: Subscriber<in T>, private val count: Long
    ) : SingleObserver<FlowCursorList<T>> {
        private val emitted: AtomicLong = AtomicLong()
        private val requested: AtomicLong = AtomicLong()
        private var disposable: Disposable? = null

        override fun onSubscribe(disposable: Disposable) {
            this.disposable = disposable
        }

        override fun onSuccess(ts: FlowCursorList<T>) {
            val starting = when {
                this.count == Long.MAX_VALUE
                    && requested.compareAndSet(0, Long.MAX_VALUE) -> 0
                else -> emitted.toLong()
            }
            var limit = this.count + starting

            while (limit > 0) {
                val iterator = ts.iterator(starting, limit)
                try {
                    var i: Long = 0
                    while (disposable?.isDisposed == false && iterator.hasNext() && i++ < limit) {
                        subscriber.onNext(iterator.next())
                    }
                    emitted.addAndGet(i)
                    // no more items
                    if (disposable?.isDisposed == false && i < limit) {
                        subscriber.onComplete()
                        break
                    }
                    limit = requested.addAndGet(-limit)
                } catch (e: Exception) {
                    FlowLog.logError(e)
                    subscriber.onError(e)
                } finally {
                    try {
                        iterator.close()
                    } catch (e: Exception) {
                        FlowLog.logError(e)
                        subscriber.onError(e)
                    }
                }
            }
        }

        override fun onError(e: Throwable) {
            subscriber.onError(e)
        }

    }
}
