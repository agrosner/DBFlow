package com.raizlabs.android.dbflow.rx2.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;

import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

/**
 * Description: The RX implementation of SQLite language queries. Meant to be interchangeable
 * with {@link SQLite}.
 */
public class RXSQLite {
    @NonNull
    public static <T> RXModelQueriableImpl<T> rx(ModelQueriable<T> modelQueriable) {
        return new RXModelQueriableImpl<>(modelQueriable);
    }

    @NonNull
    public static RXQueriableImpl rx(Class<?> table, Queriable queriable) {
        return new RXQueriableImpl(table, queriable);
    }

    @NonNull
    static <T> Callable<T> nullToNse(@NonNull final Callable<T> call) {
        return new Callable<T>() {
            @Override
            public T call() throws Exception {
                T t = call.call();
                if (t == null) {
                    throw new NoSuchElementException();
                }
                return t;
            }
        };
    }
}
