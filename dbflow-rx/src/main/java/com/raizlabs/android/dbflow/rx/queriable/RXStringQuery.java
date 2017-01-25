package com.raizlabs.android.dbflow.rx.queriable;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.rx.language.BaseRXModelQueriable;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.queriable.StringQuery;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.concurrent.Callable;

import rx.Single;

/**
 * Description: Provides a very basic query mechanism for strings. Allows you to easily perform custom SQL query string
 * code where this library does not provide. It only runs a
 * {@link android.database.sqlite.SQLiteDatabase#rawQuery(String, String[])}.
 */
public class RXStringQuery<TModel> extends BaseRXModelQueriable<TModel> implements Query {

    /**
     * The full SQLite query to use
     */
    private final StringQuery<TModel> innerStringQuery;

    /**
     * Creates an instance of this class
     *
     * @param table The table to use
     * @param sql   The sql statement to query the DB with. Does not work with {@link com.raizlabs.android.dbflow.sql.language.Delete},
     *              this must be done with {@link android.database.sqlite.SQLiteDatabase#execSQL(String)}
     */
    public RXStringQuery(Class<TModel> table, String sql) {
        super(table);
        innerStringQuery = new StringQuery<>(table, sql);
    }

    @Override
    protected BaseModelQueriable<TModel> getInnerModelQueriable() {
        return innerStringQuery;
    }

    @Override
    public String getQuery() {
        return innerStringQuery.getQuery();
    }

    @Override
    public Single<Cursor> query() {
        return query(FlowManager.getDatabaseForTable(getTable()).getWritableDatabase());
    }

    @Override
    public Single<Cursor> query(final DatabaseWrapper databaseWrapper) {
        return Single.fromCallable(new Callable<Cursor>() {
            @Override
            public Cursor call() throws Exception {
                return innerStringQuery.query(databaseWrapper);
            }
        });
    }

    /**
     * Set selection arguments to execute on this raw query.
     */
    public RXStringQuery<TModel> setSelectionArgs(String[] args) {
        this.innerStringQuery.setSelectionArgs(args);
        return this;
    }

}
