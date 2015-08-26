package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows combining of {@link SQLCondition} into one condition.
 */
public class ConditionGroup extends BaseCondition implements Query {

    /**
     * @return Starts an arbitrary clause of conditions to use.
     */
    public static ConditionGroup clause() {
        return new ConditionGroup();
    }

    private final List<SQLCondition> conditionsList = new ArrayList<>();

    private QueryBuilder query;
    private boolean isChanged;

    ConditionGroup(NameAlias columnName) {
        super(columnName);

        // default is AND
        separator = Condition.Operation.AND;
    }

    public ConditionGroup() {
        this(null);
    }

    /**
     * Appends the {@link SQLCondition} with an {@link Condition.Operation#OR}
     *
     * @param sqlCondition The condition to append.
     * @return This instance.
     */
    public ConditionGroup or(SQLCondition sqlCondition) {
        return operator(Condition.Operation.OR, sqlCondition);
    }

    /**
     * Appends the {@link SQLCondition} with an {@link Condition.Operation#AND}
     *
     * @param sqlCondition The condition to append.
     * @return This instance.
     */
    public ConditionGroup and(SQLCondition sqlCondition) {
        return operator(Condition.Operation.AND, sqlCondition);
    }

    /**
     * Appends the {@link SQLCondition} with the specified operator string.
     *
     * @param sqlCondition The condition to append.
     * @return This instance.
     */
    private ConditionGroup operator(String operator, SQLCondition sqlCondition) {
        setPreviousSeparator(operator);
        conditionsList.add(sqlCondition);
        isChanged = true;
        return this;
    }

    @Override
    public void appendConditionToQuery(QueryBuilder queryBuilder) {
        queryBuilder.append("(");
        for (SQLCondition condition : conditionsList) {
            condition.appendConditionToQuery(queryBuilder);
            if (condition.hasSeparator()) {
                queryBuilder.appendSpaceSeparated(condition.separator());
            }
        }
        queryBuilder.append(")");
    }

    /**
     * Sets the last condition to use the separator specified
     *
     * @param separator AND, OR, etc.
     */
    private void setPreviousSeparator(String separator) {
        if (conditionsList.size() > 0) {
            // set previous to use OR separator
            conditionsList.get(conditionsList.size() - 1).separator(separator);
        }
    }

    @Override
    public String getQuery() {
        if (isChanged) {
            query = new QueryBuilder();

            int count = 0;
            int size = conditionsList.size();
            for (int i = 0; i < size; i++) {
                SQLCondition condition = conditionsList.get(i);
                condition.appendConditionToQuery(query);
                if (count < size - 1) {
                    query.append(" ")
                            .append(condition.hasSeparator() ? condition.separator() : separator)
                            .append(" ");
                }
                count++;
            }
        }
        return query.toString();
    }
}
