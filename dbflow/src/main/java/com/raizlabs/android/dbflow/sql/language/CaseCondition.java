package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;

import static com.raizlabs.android.dbflow.sql.language.BaseOperator.convertValueToString;

/**
 * Description: Represents an individual condition inside a CASE.
 */
public class CaseCondition<TReturn> implements Query {

    private final Case<TReturn> caze;
    private TReturn whenValue;
    private SQLOperator sqlOperator;
    private TReturn thenValue;
    private IProperty property;
    private IProperty thenProperty;
    private boolean isThenPropertySet;

    CaseCondition(Case<TReturn> caze, @NonNull SQLOperator sqlOperator) {
        this.caze = caze;
        this.sqlOperator = sqlOperator;
    }

    CaseCondition(Case<TReturn> caze, TReturn whenValue) {
        this.caze = caze;
        this.whenValue = whenValue;
    }

    CaseCondition(Case<TReturn> caze, @NonNull IProperty property) {
        this.caze = caze;
        this.property = property;
    }

    /**
     * THEN part of this query, the value that gets set on column if condition is true.
     */
    @NonNull
    public Case<TReturn> then(TReturn value) {
        thenValue = value;
        return caze;
    }

    @NonNull
    public Case<TReturn> then(IProperty value) {
        thenProperty = value;
        // in case values are null in some sense.
        isThenPropertySet = true;
        return caze;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(" WHEN ");
        if (caze.isEfficientCase()) {
            queryBuilder.append(convertValueToString(property != null ? property : whenValue, false));
        } else {
            sqlOperator.appendConditionToQuery(queryBuilder);
        }
        queryBuilder.append(" THEN ")
            .append(convertValueToString(isThenPropertySet ?
                thenProperty : thenValue, false));
        return queryBuilder.getQuery();
    }

    @Override
    public String toString() {
        return getQuery();
    }
}
