package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Represents a SQLITE CASE argument.
 */
public class Case<TReturn> implements Query {

    private List<CaseCondition<TReturn>> caseConditions = new ArrayList<>();
    private String columnName;
    private TReturn elseValue;
    private boolean elseSpecified = false;

    Case() {
    }

    public CaseCondition<TReturn> when(SQLCondition sqlCondition) {
        CaseCondition<TReturn> caseCondition = new CaseCondition<>(this, sqlCondition);
        caseConditions.add(caseCondition);
        return caseCondition;
    }

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
        return new Property<>(null, new NameAlias(getQuery(), false).tickName(false));
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder(" CASE");
        //noinspection unchecked
        queryBuilder.appendList(caseConditions);
        if (elseSpecified) {
            queryBuilder.append(" ELSE ").append(BaseCondition.convertValueToString(elseValue, false));
        }
        queryBuilder.append(" END " + columnName);
        return queryBuilder.getQuery();
    }
}
