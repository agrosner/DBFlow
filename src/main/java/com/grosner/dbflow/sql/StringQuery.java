package com.grosner.dbflow.sql;

import android.database.Cursor;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Description: Provides a very basic query mechanism for strings. It runs a modification query and will only run
 * {@link android.database.sqlite.SQLiteDatabase#rawQuery(String, String[])}.
 */
public class StringQuery<ModelClass extends Model> implements Query, Queriable<ModelClass> {

    private final String mQuery;

    private final Class<ModelClass> mTable;

    public StringQuery(Class<ModelClass> tabel, String sql) {
        mQuery = sql;
        mTable = tabel;
    }

    @Override
    public String getQuery() {
        return mQuery;
    }

    @Override
    public Cursor query() {
        return FlowManager.getManagerForTable(mTable).getWritableDatabase().rawQuery(mQuery, null);
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
