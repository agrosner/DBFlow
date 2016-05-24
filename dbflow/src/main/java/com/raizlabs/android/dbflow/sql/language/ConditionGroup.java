package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.Condition.Operation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Allows combining of {@link SQLCondition} into one condition.
 */
public class ConditionGroup extends BaseCondition implements Query, Iterable<SQLCondition> {

    /**
     * @return Starts an arbitrary clause of conditions to use.
     */
    public static ConditionGroup clause() {
        return new ConditionGroup();
    }

    /**
     * @return Starts an arbitrary clause of conditions to use, that when included in other {@link SQLCondition},
     * does not append parenthesis to group it.
     */
    public static ConditionGroup nonGroupingClause() {
        return new ConditionGroup().setUseParenthesis(false);
    }

    private final List<SQLCondition> conditionsList = new ArrayList<>();

    private QueryBuilder query;
    private boolean isChanged;
    private boolean allCommaSeparated;
    private boolean useParenthesis = true;

    protected ConditionGroup(NameAlias columnName) {
        super(columnName);

        // default is AND
        separator = Operation.AND;
    }

    protected ConditionGroup() {
        this(null);
    }

    /**
     * Will ignore all separators for the group and make them separated by comma. This is useful
     * in {@link Set} statements.
     *
     * @param allCommaSeparated All become comma separated.
     * @return This instance.
     */
    public ConditionGroup setAllCommaSeparated(boolean allCommaSeparated) {
        this.allCommaSeparated = allCommaSeparated;
        isChanged = true;
        return this;
    }

    /**
     * Sets whether we use paranthesis when grouping this within other {@link SQLCondition}. The default
     * is true, but if no conditions exist there are no paranthesis anyways.
     *
     * @param useParenthesis true if we use them, false if not.
     */
    public ConditionGroup setUseParenthesis(boolean useParenthesis) {
        this.useParenthesis = useParenthesis;
        isChanged = true;
        return this;
    }

    /**
     * Appends the {@link SQLCondition} with an {@link Operation#OR}
     *
     * @param sqlCondition The condition to append.
     * @return This instance.
     */
    public ConditionGroup or(SQLCondition sqlCondition) {
        return operator(Operation.OR, sqlCondition);
    }

    /**
     * Appends the {@link SQLCondition} with an {@link Operation#AND}
     *
     * @param sqlCondition The condition to append.
     * @return This instance.
     */
    public ConditionGroup and(SQLCondition sqlCondition) {
        return operator(Operation.AND, sqlCondition);
    }

    /**
     * Applies the {@link Operation#AND} to all of the passed
     * {@link SQLCondition}.
     *
     * @param sqlConditions
     * @return
     */
    public ConditionGroup andAll(SQLCondition... sqlConditions) {
        for (SQLCondition sqlCondition : sqlConditions) {
            and(sqlCondition);
        }
        return this;
    }

    /**
     * Applies the {@link Operation#AND} to all of the passed
     * {@link SQLCondition}.
     *
     * @param sqlConditions
     * @return
     */
    public ConditionGroup andAll(List<SQLCondition> sqlConditions) {
        for (SQLCondition sqlCondition : sqlConditions) {
            and(sqlCondition);
        }
        return this;
    }

    /**
     * Applies the {@link Operation#AND} to all of the passed
     * {@link SQLCondition}.
     *
     * @param sqlConditions
     * @return
     */
    public ConditionGroup orAll(SQLCondition... sqlConditions) {
        for (SQLCondition sqlCondition : sqlConditions) {
            or(sqlCondition);
        }
        return this;
    }

    /**
     * Applies the {@link Operation#AND} to all of the passed
     * {@link SQLCondition}.
     *
     * @param sqlConditions
     * @return
     */
    public ConditionGroup orAll(List<SQLCondition> sqlConditions) {
        for (SQLCondition sqlCondition : sqlConditions) {
            or(sqlCondition);
        }
        return this;
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
        int conditionListSize = conditionsList.size();
        if (useParenthesis && conditionListSize > 0) {
            queryBuilder.append("(");
        }
        for (int i = 0; i < conditionListSize; i++) {
            SQLCondition condition = conditionsList.get(i);
            condition.appendConditionToQuery(queryBuilder);
            if (condition.hasSeparator() && i < conditionListSize - 1) {
                queryBuilder.appendSpaceSeparated(condition.separator());
            }
        }
        if (useParenthesis && conditionListSize > 0) {
            queryBuilder.append(")");
        }
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
                    if (!allCommaSeparated) {
                        query.appendSpace().append(condition.hasSeparator() ? condition.separator() : separator);
                    } else {
                        query.append(",");
                    }
                    query.appendSpace();
                }
                count++;
            }
        }
        return query == null ? "" : query.toString();
    }

    @Override
    public String toString() {
        return getQuery();
    }

    public int size() {
        return conditionsList.size();
    }

    public List<SQLCondition> getConditions() {
        return conditionsList;
    }

    @Override
    public Iterator<SQLCondition> iterator() {
        return conditionsList.iterator();
    }
}
