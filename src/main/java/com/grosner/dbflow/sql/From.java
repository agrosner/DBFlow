package com.grosner.dbflow.sql;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.builder.QueryBuilder;
import com.grosner.dbflow.sql.builder.WhereQueryBuilder;
import com.grosner.dbflow.structure.Model;

import java.util.ArrayList;
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

    private List<Join> mJoins = new ArrayList<Join>();

    private final FlowManager mManager;

    public From(FlowManager flowManager, Query querybase, Class<ModelClass> table) {
        mManager = flowManager;
        mQueryBuilderBase = querybase;
        mType = table;
    }

    public From<ModelClass> as(String alias) {
        mAlias = alias;
        return this;
    }

    public Join join(Class<ModelClass> table, Join.JoinType joinType) {
        Join join = new Join(mManager, this, table, joinType);
        mJoins.add(join);
        return join;
    }

    public Where<ModelClass> where() {
        return new Where<ModelClass>(mManager, this);
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
                .appendSpaceSeparated(mManager.getTableName(mType))
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
