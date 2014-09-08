package com.raizlabs.android.dbflow.sql;

import android.database.DatabaseUtils;
import android.text.TextUtils;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.builder.AbstractWhereQueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.FromQueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.Arrays;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class From<ModelClass extends Model> implements Query{

    private Query mQueryBuilderBase;

    private Class<ModelClass> mType;

    private String mAlias;

    private List<Join> mJoins;

    private String mWhere;

    private String mGroupBy;

    private String mHaving;

    private String mOrderBy;

    private String mLimit;

    private String mOffset;

    private List<Object> mArguments;

    public From(Query querybase, Class<ModelClass> table) {
        mQueryBuilderBase = querybase;
        mType = table;
    }

    public From<ModelClass> as(String alias) {
        mAlias = alias;
        return this;
    }

    public Join join(Class<ModelClass> table, Join.JoinType joinType) {
        Join join = new Join(this, table, joinType);
        mJoins.add(join);
        return join;
    }

    public From<ModelClass> where(String where, Object... args) {
        mWhere = where;
        mArguments.clear();
        if(args.length > 0) {
            mArguments.addAll(Arrays.asList(args));
        }

        return this;
    }

    public From<ModelClass> where(AbstractWhereQueryBuilder<ModelClass> abstractWhereQueryBuilder, String...fieldValues) {
        mWhere = abstractWhereQueryBuilder.getWhereQueryForArgs(fieldValues);
        return this;
    }

    public From<ModelClass> groupBy(String groupBy) {
        mGroupBy = groupBy;
        return this;
    }

    public From<ModelClass> having(String having) {
        mHaving = having;
        return this;
    }

    public From<ModelClass> orderBy(String orderBy) {
        mOrderBy = orderBy;
        return this;
    }

    public From<ModelClass> limit(Object limit) {
        return limit(String.valueOf(limit));
    }

    public From<Model> offset(Object offset) {
        return offset(String.valueOf(offset));
    }

    void addArguments(Object[] args) {
        mArguments.addAll(Arrays.asList(args));
    }

    public long count() {
        return DatabaseUtils.longForQuery(FlowManager.getCache().getHelper().getWritableDatabase(),
                    getQuery(), getArguments());
    }

    public void query() {
        // Query the sql here
        FlowManager.getWritableDatabase().rawQuery(getQuery(), getArguments());
    }

    public List<ModelClass> queryList() {
        if(mQueryBuilderBase instanceof Select) {
            return SqlUtils.queryList(mType, getQuery(), getArguments());
        } else {
            throw new IllegalArgumentException("Please use query(). The Querybase is not a Select");
        }
    }

    public ModelClass querySingle() {
        if(mQueryBuilderBase instanceof Select) {
            return SqlUtils.querySingle(mType, getQuery(), getArguments());
        } else {
            throw new IllegalArgumentException("Please use query(). The Querybase is not a Select");
        }
    }

    public String[] getArguments() {
        final int size = mArguments.size();
        final String[] args = new String[size];

        for (int i = 0; i < size; i++) {
            args[i] = mArguments.get(i).toString();
        }

        return args;
    }

    @Override
    public String getQuery() {
        FromQueryBuilder queryBuilder = new FromQueryBuilder()
                .append(mQueryBuilderBase.getQuery())
                .append("FROM")
                .appendSpaceSeparated(FlowManager.getCache().getTableName(mType))
                .appendQualifier("AS", mAlias);

        for (Join join : mJoins) {
            queryBuilder.append(join.getQuery());
        }

        queryBuilder.appendQualifier("WHERE", mWhere)
                .appendQualifier("GROUP BY", mGroupBy)
                .appendQualifier("HAVING", mHaving)
                .appendQualifier("ORDER BY", mOrderBy)
                .appendQualifier("LIMIT", mLimit)
                .appendQualifier("OFFSET", mOffset);

        // Don't wast time building the string
        // unless we're going to log it.
        if (FlowLog.isEnabled()) {
            FlowLog.v(getClass().getSimpleName(), queryBuilder.toString() + " " +
                    TextUtils.join(",", getArguments()));
        }

        return queryBuilder.getQuery().trim();
    }
}
