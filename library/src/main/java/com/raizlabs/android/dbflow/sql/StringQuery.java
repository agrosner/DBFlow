package com.raizlabs.android.dbflow.sql;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Description: Provides a very basic query mechanism for strings. It runs a modification query and will only run
 * {@link android.database.sqlite.SQLiteDatabase#rawQuery(String, String[])}.
 */
public class StringQuery<ModelClass extends Model> implements Query, Queriable<ModelClass> {

    /**
     * The full SQLite query to use
     */
    private final String mQuery;

    private final Class<ModelClass> mTable;

    /**
     * Creates an instance of this class
     *
     * @param table The table to use
     * @param sql   The sql statement to query the DB with. Does not work with {@link com.raizlabs.android.dbflow.sql.language.Delete},
     *              this must be done with {@link android.database.sqlite.SQLiteDatabase#execSQL(String)}
     */
    public StringQuery(Class<ModelClass> table, String sql) {
        mQuery = sql;
        mTable = table;
    }

    @Override
    public String getQuery() {
        return mQuery;
    }

    @Override
    public Cursor query() {
        return FlowManager.getDatabaseForTable(mTable).getWritableDatabase().rawQuery(mQuery, null);
    }

    @Override
    public void queryClose() {
        Cursor query = query();
        if (query != null) {
            query.close();
        }
    }

    @Override
    public List<ModelClass> queryList() {
        return SqlUtils.queryList(mTable, mQuery);
    }

    @Override
    public ModelClass querySingle() {
        return SqlUtils.querySingle(mTable, mQuery);
    }
}
