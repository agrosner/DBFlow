package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;

/**
 * Description: Represents an individual condition inside a CASE.
 */
public class CaseCondition<TReturn> implements Query {

    private final Case<TReturn> caze;
    private TReturn whenValue;
    private SQLCondition sqlCondition;
    private TReturn thenValue;

    CaseCondition(Case<TReturn> caze, @NonNull SQLCondition sqlCondition) {
        this.caze = caze;
        this.sqlCondition = sqlCondition;
    }

    CaseCondition(Case<TReturn> caze, TReturn whenValue) {
        this.caze = caze;
        this.whenValue = whenValue;
    }

    /**
     * THEN part of this query, the value that gets set on column if condition is true.
     */
    public Case<TReturn> then(TReturn value) {
        thenValue = value;
        return caze;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(" WHEN ");
        if (caze.isEfficientCase()) {
            queryBuilder.append(BaseCondition.convertValueToString(whenValue, false));
        } else {
            sqlCondition.appendConditionToQuery(queryBuilder);
        }
        queryBuilder.append(" THEN ").append(BaseCondition.convertValueToString(thenValue, false));
        return queryBuilder.getQuery();
    }

    @Override
    public String toString() {
        return getQuery();
    }
}
