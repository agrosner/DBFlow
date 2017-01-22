package com.raizlabs.android.dbflow.rx;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.list.FlowCursorIterator;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;

import java.util.concurrent.atomic.AtomicLong;

import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.internal.operators.BackpressureUtils;

/**
 * Description:
 */
public class ModelQueriableSubscriber<T> implements Observable.OnSubscribe<T> {

    @NonNull
    private final ModelQueriable<T> modelQueriable;

    public ModelQueriableSubscriber(@NonNull ModelQueriable<T> modelQueriable) {
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
        public void request(long n) {
            if (n == Long.MAX_VALUE && requested.compareAndSet(0, Long.MAX_VALUE)) {
                // emitting all elements
                try {
                    try (FlowCursorIterator<T> iterator = modelQueriable.cursorList().iterator()) {
                        while (!subscriber.isUnsubscribed()) {
                            if (iterator.hasNext()) {
                                subscriber.onNext(iterator.next());
                                emitted.incrementAndGet();
                            } else {
                                subscriber.onCompleted();
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    FlowLog.logError(e);
                }
            } else if (n > 0 && BackpressureUtils.getAndAddRequest(requested, n) == 0) {
                // emitting with limit/offset
                // TODO: activate
                /*long count = n;
                while (count > 0) {
                    try (FlowCursorIterator<T> iterator =
                                 modelQueriable.iterator(emitted.intValue(), (int) n)) {
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
                    }
                }*/
            }
        }
    }
}
