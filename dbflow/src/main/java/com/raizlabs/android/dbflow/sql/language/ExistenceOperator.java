package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;

/**
 * Description: The condition that represents EXISTS in a SQL statement.
 */
public class ExistenceOperator implements SQLOperator, Query {

    private Where innerWhere;

    @Override
    public void appendConditionToQuery(@NonNull QueryBuilder queryBuilder) {
        queryBuilder.appendQualifier("EXISTS", "(" + innerWhere.getQuery().trim() + ")");
    }

    @NonNull
    @Override
    public String columnName() {
        throw new RuntimeException("Method not valid for ExistenceOperator");
    }

    @Nullable
    @Override
    public String separator() {
        throw new RuntimeException("Method not valid for ExistenceOperator");
    }

    @NonNull
    @Override
    public SQLOperator separator(@NonNull String separator) {
        // not used.
        throw new RuntimeException("Method not valid for ExistenceOperator");
    }

    @Override
    public boolean hasSeparator() {
        return false;
    }

    @NonNull
    @Override
    public String operation() {
        return "";
    }

    @Override
    public Object value() {
        return innerWhere;
    }

    public ExistenceOperator where(@NonNull Where where) {
        this.innerWhere = where;
        return this;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();
        appendConditionToQuery(queryBuilder);
        return queryBuilder.getQuery();
    }
}
