package com.raizlabs.android.dbflow.rx.language;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.list.FlowCursorIterator;
import com.raizlabs.android.dbflow.sql.language.CursorResult;

import java.util.concurrent.atomic.AtomicLong;

import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.functions.Action1;
import rx.internal.operators.BackpressureUtils;

/**
 * Description: Wraps a {@link RXModelQueriable} into a {@link Observable.OnSubscribe}
 * for each element represented by the query.
 */
public class CursorResultSubscriber<T> implements Observable.OnSubscribe<T> {

    @NonNull
    private final RXModelQueriable<T> modelQueriable;

    public CursorResultSubscriber(@NonNull RXModelQueriable<T> modelQueriable) {
        this.modelQueriable = modelQueriable;
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        subscriber.setProducer(new ElementProducer(subscriber));
    }

    private class ElementProducer implements Producer {

        private final Subscriber<? super T> subscriber;
        private final AtomicLong emitted;
        private final AtomicLong requested;

        ElementProducer(Subscriber<? super T> subscriber) {
            this.subscriber = subscriber;
            requested = new AtomicLong();
            emitted = new AtomicLong();
        }

        @Override
        public void request(final long n) {
            if (n == Long.MAX_VALUE && requested.compareAndSet(0, Long.MAX_VALUE)
                || n > 0 && BackpressureUtils.getAndAddRequest(requested, n) == 0) {
                // emitting all elements
                modelQueriable.queryResults().subscribe(new CursorResultAction(n));
            }
        }

        private class CursorResultAction implements Action1<CursorResult<T>> {

            private final long limit;

            private CursorResultAction(long limit) {
                this.limit = limit;
            }

            @Override
            public void call(CursorResult<T> ts) {
                final int starting = limit == Long.MAX_VALUE && requested.compareAndSet(0, Long.MAX_VALUE)
                    ? 0 : emitted.intValue();
                long limit = this.limit + starting;

                while (limit > 0) {
                    FlowCursorIterator<T> iterator = ts.iterator(starting, limit);
                    try {
                        long i = 0;
                        while (!subscriber.isUnsubscribed() && iterator.hasNext() && i++ < limit) {
                            subscriber.onNext(iterator.next());
                        }
                        emitted.addAndGet(i);
                        // no more items
                        if (!subscriber.isUnsubscribed() && i < limit) {
                            subscriber.onCompleted();
                            break;
                        }
                        limit = requested.addAndGet(-limit);
                    } catch (Exception e) {
                        FlowLog.logError(e);
                        subscriber.onError(e);
                    } finally {
                        try {
                            iterator.close();
                        } catch (Exception e) {
                            FlowLog.logError(e);
                            subscriber.onError(e);
                        }
                    }
                }
            }
        }
    }
}
