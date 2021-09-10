package com.raizlabs.android.dbflow.rx.language;

import androidx.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;

/**
 * Description: Main entry point to wrap our queries in RX.
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
}
