package com.raizlabs.android.dbflow.rx.language

import com.raizlabs.android.dbflow.config.FlowLog
import com.raizlabs.android.dbflow.query.CursorResult
import rx.Observable
import rx.Producer
import rx.Subscriber
import rx.functions.Action1
import rx.internal.operators.BackpressureUtils
import java.util.concurrent.atomic.AtomicLong

/**
 * Description: Wraps a [RXModelQueriable] into a [Observable.OnSubscribe]
 * for each element represented by the query.
 */
class CursorResultSubscriber<T : Any>(private val modelQueriable: RXModelQueriable<T>) : Observable.OnSubscribe<T> {

    override fun call(subscriber: Subscriber<in T>) {
        subscriber.setProducer(ElementProducer(subscriber))
    }

    private inner class ElementProducer internal constructor(private val subscriber: Subscriber<in T>) : Producer {
        private val emitted: AtomicLong = AtomicLong()
        private val requested: AtomicLong = AtomicLong()

        override fun request(n: Long) {
            if (n == java.lang.Long.MAX_VALUE
                    && requested.compareAndSet(0, java.lang.Long.MAX_VALUE)
                    || n > 0 && BackpressureUtils.getAndAddRequest(requested, n) == 0L) {
                // emitting all elements
                modelQueriable.queryResults().subscribe(CursorResultAction(n))
            }
        }

        private inner class CursorResultAction
        internal constructor(private val limit: Long) : Action1<CursorResult<T>> {

            override fun call(ts: CursorResult<T>) {
                val starting = when {
                    limit == java.lang.Long.MAX_VALUE
                            && requested.compareAndSet(0, java.lang.Long.MAX_VALUE) -> 0
                    else -> emitted.toInt()
                }
                var limit = this.limit + starting

                while (limit > 0) {
                    val iterator = ts.iterator(starting, limit)
                    try {
                        var i: Long = 0
                        while (!subscriber.isUnsubscribed && iterator.hasNext() && i++ < limit) {
                            subscriber.onNext(iterator.next())
                        }
                        emitted.addAndGet(i)
                        // no more items
                        if (!subscriber.isUnsubscribed && i < limit) {
                            subscriber.onCompleted()
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
        }
    }
}
