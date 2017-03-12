package com.raizlabs.android.dbflow.rx2.queriable;

import android.database.sqlite.SQLiteDatabase;

import com.raizlabs.android.dbflow.rx2.language.BaseRXModelQueriable;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.queriable.StringQuery;

/**
 * Description: Provides a very basic query mechanism for strings. Allows you to easily perform custom SQL query string
 * code where this library does not provide. It only runs a
 * {@link SQLiteDatabase#rawQuery(String, String[])}.
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
     * @param sql   The sql statement to query the DB with. Does not work with {@link Delete},
     *              this must be done with {@link SQLiteDatabase#execSQL(String)}
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

    /**
     * Set selection arguments to execute on this raw query.
     */
    public RXStringQuery<TModel> setSelectionArgs(String[] args) {
        this.innerStringQuery.setSelectionArgs(args);
        return this;
    }

}
