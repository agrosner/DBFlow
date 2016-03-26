package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Provides a very basic query mechanism for strings. Allows you to easily perform custom SQL query string
 * code where this library does not provide. It only runs a
 * {@link android.database.sqlite.SQLiteDatabase#rawQuery(String, String[])}.
 */
public class StringQuery<TModel extends Model> extends BaseModelQueriable<TModel> implements Query, ModelQueriable<TModel> {

    /**
     * The full SQLite query to use
     */
    private final String query;

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
        return databaseWrapper.rawQuery(query, null);
    }

    @Override
    public long count() {
        return count(FlowManager.getDatabaseForTable(getTable()).getWritableDatabase());
    }

    @Override
    public long count(DatabaseWrapper databaseWrapper) {
        return SqlUtils.longForQuery(databaseWrapper, getQuery());
    }
}
