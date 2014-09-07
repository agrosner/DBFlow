package com.raizlabs.android.dbflow.sql;

import android.database.DatabaseUtils;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.Arrays;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class From implements Query{

    private Query mQueryBuilderBase;

    private Class<? extends Model> mType;

    private String mAlias;

    private List<Join> mJoins;

    private String mWhere;

    private String mGroupBy;

    private String mHaving;

    private String mOrderBy;

    private String mLimit;

    private String mOffset;

    private List<Object> mArguments;

    public From(Query querybase, Class<? extends Model> table) {
        mQueryBuilderBase = querybase;
        mType = table;
    }

    public From as(String alias) {
        mAlias = alias;
        return this;
    }

    public Join join(Class<? extends Model> table, Join.JoinType joinType) {
        Join join = new Join(this, table, joinType);
        mJoins.add(join);
        return join;
    }

    public From where(String where, Object... args) {
        mWhere = where;
        mArguments.clear();
        if(args.length > 0) {
            mArguments.addAll(Arrays.asList(args));
        }

        return this;
    }

    public From groupBy(String groupBy) {
        mGroupBy = groupBy;
        return this;
    }

    public From having(String having) {
        mHaving = having;
        return this;
    }

    public From orderBy(String orderBy) {
        mOrderBy = orderBy;
        return this;
    }

    public From limit(Object limit) {
        return limit(String.valueOf(limit));
    }

    public From offset(Object offset) {
        return offset(String.valueOf(offset));
    }

    void addArguments(Object[] args) {
        mArguments.addAll(Arrays.asList(args));
    }

    public long count() {
        return DatabaseUtils.longForQuery(FlowConfig.getCache().getHelper().getWritableDatabase(),
                    getQuery(), getArguments());
    }

    public <ModelClass extends Model> List<ModelClass> queryList() {
        if(mQueryBuilderBase instanceof Select) {
            //return FlowConfig.getSqlHelper().getWritableDatabase().rawQuery(mT)
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
                .appendSpaceSeparated(FlowConfig.getCache().getTableName(mType))
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

        /*// Don't wast time building the string
        // unless we're going to log it.
        if (AALog.isEnabled()) {
            AALog.v(sql.toString() + " " + TextUtils.join(",", getArguments()));
        }*/

        return queryBuilder.getQuery().trim();
    }
}
