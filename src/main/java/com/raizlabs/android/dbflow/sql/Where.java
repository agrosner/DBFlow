package com.raizlabs.android.dbflow.sql;

import android.database.DatabaseUtils;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.builder.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.WhereQueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class Where<ModelClass extends Model> implements Query {

    private From<ModelClass> mFrom;

    private WhereQueryBuilder<ModelClass> mWhereQueryBuilder;

    private String mGroupBy;

    private String mHaving;

    private String mOrderBy;

    private String mLimit;

    private String mOffset;

    public Where(From<ModelClass> from) {
        mFrom = from;
        mWhereQueryBuilder = new WhereQueryBuilder<ModelClass>(mFrom.getType());
    }

    public Where<ModelClass> whereClause(String whereClause) {
        mWhereQueryBuilder.append(whereClause);
        return this;
    }

    public Where<ModelClass> whereQuery(WhereQueryBuilder<ModelClass> whereQueryBuilder) {
        mWhereQueryBuilder = whereQueryBuilder;
        return this;
    }

    public Where<ModelClass> param(String key, Object value) {
        mWhereQueryBuilder.param(key, value);
        return this;
    }

    public Where<ModelClass> params(Map<String, String> params) {
        mWhereQueryBuilder.params(params);
        return this;
    }

    public Where<ModelClass> primaryParams(Object... values) {
        mWhereQueryBuilder.primaryParams(values);
        return this;
    }

    public Where<ModelClass> groupBy(String groupBy) {
        mGroupBy = groupBy;
        return this;
    }

    public Where<ModelClass> having(String having) {
        mHaving = having;
        return this;
    }

    public Where<ModelClass> orderBy(String orderBy) {
        mOrderBy = orderBy;
        return this;
    }

    public Where<ModelClass> limit(Object limit) {
        mLimit = String.valueOf(limit);
        return this;
    }

    public Where<ModelClass> offset(Object offset) {
        mOffset = String.valueOf(offset);
        return this;
    }

    public long count() {
        return DatabaseUtils.longForQuery(FlowManager.getCache().getHelper().getWritableDatabase(),
                getQuery(), null);
    }

    public void query() {
        // Query the sql here
        FlowManager.getWritableDatabase().rawQuery(getQuery(), null);
    }

    public List<ModelClass> queryList() {
        if (mFrom.getQueryBuilderBase() instanceof Select) {
            return SqlUtils.queryList(mFrom.getType(), getQuery());
        } else {
            throw new IllegalArgumentException("Please use query(). The Querybase is not a Select");
        }
    }

    public ModelClass querySingle() {
        if (mFrom.getQueryBuilderBase() instanceof Select) {
            return SqlUtils.querySingle(mFrom.getType(), getQuery());
        } else {
            throw new IllegalArgumentException("Please use query(). The Querybase is not a Select");
        }
    }

    public boolean hasData() {
        if (mFrom.getQueryBuilderBase() instanceof Select) {
            return SqlUtils.hasData(mFrom.getType(), getQuery());
        } else {
            throw new IllegalArgumentException("Please use query(). The Querybase is not a Select");
        }
    }

    @Override
    public String getQuery() {
        String fromQuery = mFrom.getQuery();
        QueryBuilder queryBuilder = new QueryBuilder().append(fromQuery);

        queryBuilder.appendQualifier("WHERE", mWhereQueryBuilder.getQuery())
                .appendQualifier("GROUP BY", mGroupBy)
                .appendQualifier("HAVING", mHaving)
                .appendQualifier("ORDER BY", mOrderBy)
                .appendQualifier("LIMIT", mLimit)
                .appendQualifier("OFFSET", mOffset);

        // Don't wast time building the string
        // unless we're going to log it.
        if (FlowLog.isEnabled(FlowLog.Level.V)) {
            FlowLog.log(FlowLog.Level.V, queryBuilder.getQuery());
        }

        return queryBuilder.getQuery();
    }
}
