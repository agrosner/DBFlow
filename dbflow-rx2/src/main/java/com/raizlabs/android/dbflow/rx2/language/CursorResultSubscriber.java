package com.raizlabs.android.dbflow.rx2.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.list.FlowCursorIterator;
import com.raizlabs.android.dbflow.sql.language.CursorResult;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.Flowable;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

/**
 * Description: Wraps a {@link RXModelQueriable} into a {@link Flowable}
 * for each element represented by the query.
 */
public class CursorResultSubscriber<T> extends Flowable<T> {

    private final AtomicLong emitted;
    private final AtomicLong requested;

    @NonNull
    private final RXModelQueriable<T> modelQueriable;

    public CursorResultSubscriber(@NonNull RXModelQueriable<T> modelQueriable) {
        this.modelQueriable = modelQueriable;
        requested = new AtomicLong();
        emitted = new AtomicLong();
    }

    @Override
    protected void subscribeActual(final Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new Subscription() {
            @Override
            public void request(final long n) {
                if (n == Long.MAX_VALUE && requested.compareAndSet(0, Long.MAX_VALUE)) {
                    // emitting all elements
                    modelQueriable.queryResults().subscribe(new SingleObserver<CursorResult<T>>() {

                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(CursorResult<T> ts) {
                            FlowCursorIterator<T> iterator = ts.iterator();
                            try {
                                while (iterator.hasNext()) {
                                    subscriber.onNext(iterator.next());
                                    emitted.incrementAndGet();
                                }

                                subscriber.onComplete();
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

                        @Override
                        public void onError(Throwable e) {
                            subscriber.onError(e);
                        }

                    });

                } else {
                    // emitting with limit/offset

                    modelQueriable.queryResults().subscribe(new SingleObserver<CursorResult<T>>() {

                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(CursorResult<T> tCursorResult) {
                            long count = n;
                            while (count > 0) {
                                FlowCursorIterator<T> iterator =
                                        tCursorResult.iterator(emitted.intValue(), (int) n);
                                try {
                                    long i = 0;
                                    while (iterator.hasNext()) {
                                        if (i++ < count) {
                                            subscriber.onNext(iterator.next());
                                        } else {
                                            break;
                                        }
                                    }
                                    emitted.addAndGet(i);
                                    // no more items
                                    if (i < count) {
                                        subscriber.onComplete();
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

                        @Override
                        public void onError(Throwable e) {
                            subscriber.onError(e);
                        }
                    });
                }
            }

            @Override
            public void cancel() {

            }
        });
    }
}
