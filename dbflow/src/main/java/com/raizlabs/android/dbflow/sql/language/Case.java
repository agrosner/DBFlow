package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Represents a SQLITE CASE argument.
 */
public class Case<TReturn> implements Query {

    private IProperty caseColumn;
    private List<CaseCondition<TReturn>> caseConditions = new ArrayList<>();
    private String columnName;
    private TReturn elseValue;
    private boolean elseSpecified = false;

    // when true, only WHEN value is supported. Not WHEN condition
    private boolean efficientCase = false;

    private boolean endSpecified = false;

    Case() {
    }

    Case(IProperty caseColumn) {
        this.caseColumn = caseColumn;
        if (caseColumn != null) {
            efficientCase = true;
        }
    }

    public CaseCondition<TReturn> when(SQLOperator sqlOperator) {
        if (efficientCase) {
            throw new IllegalStateException("When using the efficient CASE method," +
                "you must pass in value only, not condition.");
        }
        CaseCondition<TReturn> caseCondition = new CaseCondition<>(this, sqlOperator);
        caseConditions.add(caseCondition);
        return caseCondition;
    }

    public CaseCondition<TReturn> when(TReturn whenValue) {
        if (!efficientCase) {
            throw new IllegalStateException("When not using the efficient CASE method, " +
                "you must pass in the SQLConditions as a parameter");
        }
        CaseCondition<TReturn> caseCondition = new CaseCondition<>(this, whenValue);
        caseConditions.add(caseCondition);
        return caseCondition;
    }

    public CaseCondition<TReturn> when(IProperty property) {
        if (!efficientCase) {
            throw new IllegalStateException("When not using the efficient CASE method, " +
                "you must pass in the SQLOperator as a parameter");
        }
        CaseCondition<TReturn> caseCondition = new CaseCondition<>(this, property);
        caseConditions.add(caseCondition);
        return caseCondition;
    }

    /**
     * Default case here. If not specified, value will be NULL.
     */
    public Case<TReturn> _else(TReturn elseValue) {
        this.elseValue = elseValue;
        elseSpecified = true; // ensure its set especially if null specified.
        return this;
    }

    /**
     * @param columnName The name of the case that we return in a column.
     * @return The case completed as a property.
     */
    public Property<Case<TReturn>> end(@Nullable String columnName) {
        endSpecified = true;
        if (columnName != null) {
            this.columnName = QueryBuilder.quoteIfNeeded(columnName);
        }
        return new Property<>(null, NameAlias.rawBuilder(getQuery())
            .build());
    }

    /**
     * @return The case completed as a property.
     */
    public Property<Case<TReturn>> end() {
        return end(null);
    }

    /**
     * @return The case complete as an operator.
     */
    public Operator endAsOperator() {
        return Operator.op(end().getNameAlias());
    }

    boolean isEfficientCase() {
        return efficientCase;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(" CASE");
        if (isEfficientCase()) {
            queryBuilder.append(" " + BaseOperator.convertValueToString(caseColumn, false));
        }

        queryBuilder.append(QueryBuilder.join("", caseConditions));

        if (elseSpecified) {
            queryBuilder.append(" ELSE ").append(BaseOperator.convertValueToString(elseValue, false));
        }
        if (endSpecified) {
            queryBuilder.append(" END " + (columnName != null ? columnName : ""));
        }
        return queryBuilder.getQuery();
    }
}
