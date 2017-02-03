package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Provides a very basic query mechanism for strings. Allows you to easily perform custom SQL query string
 * code where this library does not provide. It only runs a
 * {@link android.database.sqlite.SQLiteDatabase#rawQuery(String, String[])}.
 */
public class StringQuery<TModel> extends BaseModelQueriable<TModel> implements Query, ModelQueriable<TModel> {

    /**
     * The full SQLite query to use
     */
    private final String query;
    private String[] args;

    /**
     * Creates an instance of this class
     *
     * @param table The table to use
     * @param sql   The sql statement to query the DB with. Does not work with {@link com.raizlabs.android.dbflow.sql.language.Delete},
     *              this must be done with {@link android.database.sqlite.SQLiteDatabase#execSQL(String)}
     */
    public StringQuery(Class<TModel> table, String sql) {
        super(table);
        query = sql;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public Cursor query() {
        return query(FlowManager.getDatabaseForTable(getTable()).getWritableDatabase());
    }

    @Override
    public Cursor query(DatabaseWrapper databaseWrapper) {
        return databaseWrapper.rawQuery(query, args);
    }

    /**
     * Set selection arguments to execute on this raw query.
     */
    public StringQuery<TModel> setSelectionArgs(String[] args) {
        this.args = args;
        return this;
    }

    @Override
    public BaseModel.Action getPrimaryAction() {
        return BaseModel.Action.CHANGE; // we don't explicitly know the change, but something changed.
    }
}
