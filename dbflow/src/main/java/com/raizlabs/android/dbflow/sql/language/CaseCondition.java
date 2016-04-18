package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;

/**
 * Description: Represents an individual condition inside a CASE.
 */
public class CaseCondition<TReturn> implements Query {

    private final Case<TReturn> caze;
    private final SQLCondition sqlCondition;
    private TReturn thenValue;

    CaseCondition(Case<TReturn> caze, @NonNull SQLCondition sqlCondition) {
        this.caze = caze;
        this.sqlCondition = sqlCondition;
    }

    public Case<TReturn> then(TReturn value) {
        thenValue = value;
        return caze;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(" WHEN ");
        sqlCondition.appendConditionToQuery(queryBuilder);
        queryBuilder.append(" THEN").append(BaseCondition.convertValueToString(thenValue, false));
        return queryBuilder.getQuery();
    }
}
