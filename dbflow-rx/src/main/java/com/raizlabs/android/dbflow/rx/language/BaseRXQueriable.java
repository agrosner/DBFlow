package com.raizlabs.android.dbflow.rx.language;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.concurrent.Callable;

import rx.Observable;

import static rx.Observable.fromCallable;

/**
 * Description:
 */
public abstract class BaseRXQueriable<T> implements RXQueriable {

    private final Class<T> table;

    public BaseRXQueriable(Class<T> table) {
        this.table = table;
    }

    protected abstract Queriable getInnerQueriable();

    @Nullable
    @Override
    public Observable<Cursor> query() {
        return fromCallable(new Callable<Cursor>() {
            @Override
            public Cursor call() throws Exception {
                return getInnerQueriable().query();
            }
        });
    }

    @Nullable
    @Override
    public Observable<Cursor> query(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Cursor>() {
            @Override
            public Cursor call() throws Exception {
                return getInnerQueriable().query(databaseWrapper);
            }
        });
    }

    @Override
    public Observable<DatabaseStatement> compileStatement() {
        return fromCallable(new Callable<DatabaseStatement>() {
            @Override
            public DatabaseStatement call() throws Exception {
                return getInnerQueriable().compileStatement();
            }
        });
    }

    @Override
    public Observable<DatabaseStatement> compileStatement(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<DatabaseStatement>() {
            @Override
            public DatabaseStatement call() throws Exception {
                return getInnerQueriable().compileStatement(databaseWrapper);
            }
        });
    }

    @Override
    public Observable<Long> count() {
        return fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return getInnerQueriable().count();
            }
        });
    }

    @Override
    public Observable<Long> count(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return getInnerQueriable().count(databaseWrapper);
            }
        });
    }

    @Override
    public Observable<Long> executeUpdateDelete(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return getInnerQueriable().executeUpdateDelete(databaseWrapper);
            }
        });
    }

    @Override
    public Observable<Long> executeUpdateDelete() {
        return fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return getInnerQueriable().executeUpdateDelete();
            }
        });
    }

    @Override
    public Observable<Boolean> hasData() {
        return fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return getInnerQueriable().hasData();
            }
        });
    }

    @Override
    public Observable<Boolean> hasData(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return getInnerQueriable().hasData(databaseWrapper);
            }
        });
    }

    @Override
    public Observable<Void> execute() {
        return fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getInnerQueriable().execute();
                return null;
            }
        });
    }

    @Override
    public Observable<Void> execute(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getInnerQueriable().execute(databaseWrapper);
                return null;
            }
        });
    }
}
