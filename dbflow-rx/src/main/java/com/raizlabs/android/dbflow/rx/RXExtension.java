package com.raizlabs.android.dbflow.rx;

import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;

import rx.Observable;

/**
 * Description: Supplies RXJava extensions to DBFlow queries
 */
public class RXExtension {

    /**
     * Takes a query and converts it to a {@link Observable}. As objects are retrieved,
     * events get emitted.
     */
    public static <T> Observable<T> toObservable(ModelQueriable<T> queriable) {
        return Observable.create(new ModelQueriableSubscriber<>(queriable));
    }
}
