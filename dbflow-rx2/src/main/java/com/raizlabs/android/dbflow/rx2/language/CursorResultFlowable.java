package com.raizlabs.android.dbflow.rx2.language;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

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
public class CursorResultFlowable<T> extends Flowable<T> {


    @NonNull
    private final RXModelQueriable<T> modelQueriable;

    public CursorResultFlowable(@NonNull RXModelQueriable<T> modelQueriable) {
        this.modelQueriable = modelQueriable;

    }

    @Override
    protected void subscribeActual(final Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new Subscription() {
            @Override
            public void request(final long n) {
                modelQueriable.queryResults().subscribe(new CursorResultObserver<>(subscriber, n));
            }

            @Override
            public void cancel() {

            }
        });
    }

    @VisibleForTesting
    static class CursorResultObserver<T> implements SingleObserver<CursorResult<T>> {

        private final Subscriber<? super T> subscriber;
        private final long count;
        private final AtomicLong emitted;
        private final AtomicLong requested;
        private Disposable disposable;

        CursorResultObserver(Subscriber<? super T> subscriber, long count) {
            this.subscriber = subscriber;
            this.count = count;
            requested = new AtomicLong();
            emitted = new AtomicLong();
        }

        @Override
        public void onSubscribe(Disposable disposable) {
            this.disposable = disposable;
        }

        @Override
        public void onSuccess(CursorResult<T> ts) {
            long limit = this.count;
            int starting = limit == Long.MAX_VALUE && requested.compareAndSet(0, Long.MAX_VALUE)
                ? 0 : emitted.intValue();
            while (limit > 0) {
                FlowCursorIterator<T> iterator = ts.iterator(starting, limit);
                try {
                    long i = 0;
                    while (!disposable.isDisposed() && iterator.hasNext() && i++ < limit) {
                        subscriber.onNext(iterator.next());
                    }
                    emitted.addAndGet(i);
                    // no more items
                    if (!disposable.isDisposed() && i < limit) {
                        subscriber.onComplete();
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

        @Override
        public void onError(Throwable e) {
            subscriber.onError(e);
        }

    }
}
