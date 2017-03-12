package com.raizlabs.android.dbflow.rx.language;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.BaseQueriable;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.concurrent.Callable;

import rx.Single;

import static rx.Single.fromCallable;

/**
 * Description: Represents {@link BaseQueriable} with RX constructs.
 */
public class BaseRXQueriable<T> implements RXQueriable {

    private final Class<T> table;
    private final Queriable queriable;

    BaseRXQueriable(Class<T> table, Queriable queriable) {
        this.table = table;
        this.queriable = queriable;
    }

    private Queriable getInnerQueriable() {
        return queriable;
    }

    @NonNull
    @Override
    public Single<Cursor> query() {
        return fromCallable(new Callable<Cursor>() {
            @Override
            public Cursor call() throws Exception {
                return getInnerQueriable().query();
            }
        });
    }

    @NonNull
    @Override
    public Single<Cursor> query(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Cursor>() {
            @Override
            public Cursor call() throws Exception {
                return getInnerQueriable().query(databaseWrapper);
            }
        });
    }

    @NonNull
    @Override
    public Single<DatabaseStatement> compileStatement() {
        return fromCallable(new Callable<DatabaseStatement>() {
            @Override
            public DatabaseStatement call() throws Exception {
                return getInnerQueriable().compileStatement();
            }
        });
    }

    @NonNull
    @Override
    public Single<DatabaseStatement> compileStatement(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<DatabaseStatement>() {
            @Override
            public DatabaseStatement call() throws Exception {
                return getInnerQueriable().compileStatement(databaseWrapper);
            }
        });
    }

    @NonNull
    @Override
    public Single<Long> count() {
        return fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return getInnerQueriable().count();
            }
        });
    }

    @NonNull
    @Override
    public Single<Long> count(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return getInnerQueriable().count(databaseWrapper);
            }
        });
    }

    @NonNull
    @Override
    public Single<Long> executeInsert() {
        return fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return getInnerQueriable().executeInsert(FlowManager.getWritableDatabaseForTable(table));
            }
        });
    }

    @NonNull
    @Override
    public Single<Long> executeInsert(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return getInnerQueriable().executeInsert(databaseWrapper);
            }
        });
    }

    @NonNull
    @Override
    public Single<Long> executeUpdateDelete(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return getInnerQueriable().executeUpdateDelete(databaseWrapper);
            }
        });
    }

    @NonNull
    @Override
    public Single<Long> executeUpdateDelete() {
        return fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return getInnerQueriable().executeUpdateDelete();
            }
        });
    }

    @NonNull
    @Override
    public Single<Boolean> hasData() {
        return fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return getInnerQueriable().hasData();
            }
        });
    }

    @NonNull
    @Override
    public Single<Boolean> hasData(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return getInnerQueriable().hasData(databaseWrapper);
            }
        });
    }

    @NonNull
    @Override
    public Single<Void> execute() {
        return fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getInnerQueriable().execute();
                return null;
            }
        });
    }

    @NonNull
    @Override
    public Single<Void> execute(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getInnerQueriable().execute(databaseWrapper);
                return null;
            }
        });
    }
}
