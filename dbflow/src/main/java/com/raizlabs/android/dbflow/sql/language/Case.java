package com.raizlabs.android.dbflow.sql.language;

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

    Case() {
    }

    Case(IProperty caseColumn) {
        this.caseColumn = caseColumn;
        efficientCase = true;
    }

    public CaseCondition<TReturn> when(SQLCondition sqlCondition) {
        if (efficientCase) {
            throw new IllegalStateException("When using the efficient CASE method," +
                    "you must pass in value only, not condition.");
        }
        CaseCondition<TReturn> caseCondition = new CaseCondition<>(this, sqlCondition);
        caseConditions.add(caseCondition);
        return caseCondition;
    }

    public CaseCondition<TReturn> when(TReturn whenValue) {
        if (!efficientCase) {
            throw new IllegalStateException("When not using the efficient CASE method, " +
                    "you must pass in the condition as a parameter");
        }
        CaseCondition<TReturn> caseCondition = new CaseCondition<>(this, whenValue);
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
     * @return The final name given to this case.
     */
    public Property<Case<TReturn>> end(String columnName) {
        this.columnName = QueryBuilder.quoteIfNeeded(columnName);
        return new Property<>(null, NameAlias.rawBuilder(getQuery())
                .build());
    }

    boolean isEfficientCase() {
        return efficientCase;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(" CASE");
        if (isEfficientCase()) {
            queryBuilder.append(" " + BaseCondition.convertValueToString(caseColumn, false));
        }
        //noinspection unchecked
        queryBuilder.appendList(caseConditions);
        if (elseSpecified) {
            queryBuilder.append(" ELSE ").append(BaseCondition.convertValueToString(elseValue, false));
        }
        queryBuilder.append(" END " + columnName);
        return queryBuilder.getQuery();
    }
}
