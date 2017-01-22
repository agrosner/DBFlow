package com.raizlabs.android.dbflow.rx.language;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.concurrent.Callable;

import rx.Single;

import static rx.Single.fromCallable;

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
    public Single<Cursor> query() {
        return fromCallable(new Callable<Cursor>() {
            @Override
            public Cursor call() throws Exception {
                return getInnerQueriable().query();
            }
        });
    }

    @Nullable
    @Override
    public Single<Cursor> query(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Cursor>() {
            @Override
            public Cursor call() throws Exception {
                return getInnerQueriable().query(databaseWrapper);
            }
        });
    }

    @Override
    public Single<DatabaseStatement> compileStatement() {
        return fromCallable(new Callable<DatabaseStatement>() {
            @Override
            public DatabaseStatement call() throws Exception {
                return getInnerQueriable().compileStatement();
            }
        });
    }

    @Override
    public Single<DatabaseStatement> compileStatement(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<DatabaseStatement>() {
            @Override
            public DatabaseStatement call() throws Exception {
                return getInnerQueriable().compileStatement(databaseWrapper);
            }
        });
    }

    @Override
    public Single<Long> count() {
        return fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return getInnerQueriable().count();
            }
        });
    }

    @Override
    public Single<Long> count(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return getInnerQueriable().count(databaseWrapper);
            }
        });
    }

    @Override
    public Single<Long> executeUpdateDelete(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return getInnerQueriable().executeUpdateDelete(databaseWrapper);
            }
        });
    }

    @Override
    public Single<Long> executeUpdateDelete() {
        return fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return getInnerQueriable().executeUpdateDelete();
            }
        });
    }

    @Override
    public Single<Boolean> hasData() {
        return fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return getInnerQueriable().hasData();
            }
        });
    }

    @Override
    public Single<Boolean> hasData(final DatabaseWrapper databaseWrapper) {
        return fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return getInnerQueriable().hasData(databaseWrapper);
            }
        });
    }

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
