package com.raizlabs.android.dbflow.sql;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.builder.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.WhereQueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

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

    public Where<ModelClass> where() {
        return new Where<ModelClass>(this);
    }

    public Where<ModelClass> where(String whereClause) {
        return where().whereClause(whereClause);
    }

    public Where<ModelClass> where(WhereQueryBuilder<ModelClass> whereQueryBuilder) {
        return where().whereQuery(whereQueryBuilder);
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder()
                .append(mQueryBuilderBase.getQuery())
                .append("FROM")
                .appendSpaceSeparated(FlowManager.getCache().getTableName(mType))
                .appendQualifier("AS", mAlias);

        for (Join join : mJoins) {
            queryBuilder.append(join.getQuery());
        }

        return queryBuilder.getQuery().trim();
    }

    Query getQueryBuilderBase() {
        return mQueryBuilderBase;
    }

    public Class<ModelClass> getType() {
        return mType;
    }
}
