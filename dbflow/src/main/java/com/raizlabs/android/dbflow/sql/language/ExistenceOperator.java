package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.QueryBuilder;

/**
 * Description: The condition that represents EXISTS in a SQL statement.
 */
public class ExistenceOperator implements SQLOperator {

    private Where innerWhere;

    @Override
    public void appendConditionToQuery(QueryBuilder queryBuilder) {
        queryBuilder.appendQualifier("EXISTS", "(" + innerWhere.getQuery() + ")");
    }

    @Override
    public String columnName() {
        throw new RuntimeException("Method not valid for ExistenceOperator");
    }

    @Override
    public String separator() {
        throw new RuntimeException("Method not valid for ExistenceOperator");
    }

    @Override
    public SQLOperator separator(String separator) {
        // not used.
        throw new RuntimeException("Method not valid for ExistenceOperator");
    }

    @Override
    public boolean hasSeparator() {
        return false;
    }

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
}
