package com.raizlabs.android.dbflow.rx.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;

/**
 * Description: Main entry point to wrap our queries in RX.
 */
public class RXSQLite {

    @NonNull
    public static <T> BaseRXModelQueriable<T> rx(ModelQueriable<T> modelQueriable) {
        return new BaseRXModelQueriable<>(modelQueriable);
    }

    @NonNull
    public static <T> BaseRXQueriable<T> rx(Class<T> table, Queriable queriable) {
        return new BaseRXQueriable<>(table, queriable);
    }
}
