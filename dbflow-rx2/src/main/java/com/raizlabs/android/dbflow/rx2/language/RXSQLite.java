package com.raizlabs.android.dbflow.rx2.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;

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

}
