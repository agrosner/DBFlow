package com.raizlabs.android.dbflow.sql.language;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowTableList;
import com.raizlabs.android.dbflow.sql.ModelQueriable;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Description: The INDEXED BY part of a SELECT/UPDATE/DELETE
 */
public class IndexedBy<ModelClass extends Model> implements ModelQueriable<ModelClass>, Query {

    private final String mIndexName;

    private final WhereBase<ModelClass> mWhereBase;

    /**
     * Creates the INDEXED BY part of the clause.
     *
     * @param mIndexName The name of the index
     * @param whereBase  The base piece of this query
     */
    IndexedBy(@NonNull String mIndexName, WhereBase<ModelClass> whereBase) {
        this.mIndexName = mIndexName;
        this.mWhereBase = whereBase;
    }

    @Override
    public List<ModelClass> queryList() {
        return SqlUtils.convertToList(getTable(), query());
    }

    @Override
    public ModelClass querySingle() {
        return SqlUtils.convertToModel(false, getTable(), query());
    }

    @Override
    public Class<ModelClass> getTable() {
        return mWhereBase.getTable();
    }

    @Override
    public FlowCursorList<ModelClass> queryCursorList() {
        return new FlowCursorList<>(false, this);
    }

    @Override
    public FlowTableList<ModelClass> queryTableList() {
        return new FlowTableList<>(this);
    }

    @Override
    public Cursor query() {
        return FlowManager.getDatabaseForTable(getTable()).getWritableDatabase().rawQuery(getQuery(), null);
    }

    @Override
    public void queryClose() {
        Cursor cursor = query();
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(mWhereBase.getQuery())
                .append("INDEXED BY").appendSpaceSeparated(mIndexName);
        return queryBuilder.getQuery();
    }
}
