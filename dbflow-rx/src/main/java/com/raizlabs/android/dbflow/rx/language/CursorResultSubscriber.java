package com.raizlabs.android.dbflow.rx.language;

import android.support.annotation.NonNull;

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
            if (n == Long.MAX_VALUE && requested.compareAndSet(0, Long.MAX_VALUE)) {
                // emitting all elements
                modelQueriable.queryResults().subscribe(new Action1<CursorResult<T>>() {
                    @Override
                    public void call(CursorResult<T> ts) {
                        FlowCursorIterator<T> iterator = ts.iterator();
                        try {
                            while (!subscriber.isUnsubscribed()) {
                                if (iterator.hasNext()) {
                                    subscriber.onNext(iterator.next());
                                    emitted.incrementAndGet();
                                } else {
                                    subscriber.onCompleted();
                                    break;
                                }
                            }
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
                });

            } else if (n > 0 && BackpressureUtils.getAndAddRequest(requested, n) == 0) {
                // emitting with limit/offset

                modelQueriable.queryResults().subscribe(new Action1<CursorResult<T>>() {
                    @Override
                    public void call(CursorResult<T> tCursorResult) {
                        long count = n;
                        while (count > 0) {
                            FlowCursorIterator<T> iterator =
                                tCursorResult.iterator(emitted.intValue(), (int) n);
                            try {
                                long i = 0;
                                while (!subscriber.isUnsubscribed() && iterator.hasNext()) {
                                    if (i++ < count) {
                                        subscriber.onNext(iterator.next());
                                    } else {
                                        break;
                                    }
                                }
                                emitted.addAndGet(i);
                                // no more items
                                if (!subscriber.isUnsubscribed() && i < count) {
                                    subscriber.onCompleted();
                                    break;
                                }
                                count = requested.addAndGet(-count);
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
                });
            }
        }
    }
}
