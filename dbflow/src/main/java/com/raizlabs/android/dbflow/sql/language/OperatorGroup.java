package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.Operator.Operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Allows combining of {@link SQLOperator} into one condition.
 */
public class OperatorGroup extends BaseOperator implements Query, Iterable<SQLOperator> {

    /**
     * @return Starts an arbitrary clause of conditions to use.
     */
    @NonNull
    public static OperatorGroup clause() {
        return new OperatorGroup();
    }

    /**
     * @return Starts an arbitrary clause of conditions to use with first param as conditions separated by AND.
     */
    @NonNull
    public static OperatorGroup clause(SQLOperator... condition) {
        return new OperatorGroup().andAll(condition);
    }

    /**
     * @return Starts an arbitrary clause of conditions to use, that when included in other {@link SQLOperator},
     * does not append parenthesis to group it.
     */
    public static OperatorGroup nonGroupingClause() {
        return new OperatorGroup().setUseParenthesis(false);
    }

    /**
     * @return Starts an arbitrary clause of conditions (without parenthesis) to use with first param as conditions separated by AND.
     */
    public static OperatorGroup nonGroupingClause(SQLOperator... condition) {
        return new OperatorGroup().setUseParenthesis(false).andAll(condition);
    }

    @NonNull
    private final List<SQLOperator> conditionsList = new ArrayList<>();

    private QueryBuilder query;
    private boolean isChanged;
    private boolean allCommaSeparated;
    private boolean useParenthesis = true;

    protected OperatorGroup(NameAlias columnName) {
        super(columnName);

        // default is AND
        separator = Operation.AND;
    }

    protected OperatorGroup() {
        this(null);
    }

    /**
     * Will ignore all separators for the group and make them separated by comma. This is useful
     * in {@link Set} statements.
     *
     * @param allCommaSeparated All become comma separated.
     * @return This instance.
     */
    @NonNull
    public OperatorGroup setAllCommaSeparated(boolean allCommaSeparated) {
        this.allCommaSeparated = allCommaSeparated;
        isChanged = true;
        return this;
    }

    /**
     * Sets whether we use paranthesis when grouping this within other {@link SQLOperator}. The default
     * is true, but if no conditions exist there are no paranthesis anyways.
     *
     * @param useParenthesis true if we use them, false if not.
     */
    @NonNull
    public OperatorGroup setUseParenthesis(boolean useParenthesis) {
        this.useParenthesis = useParenthesis;
        isChanged = true;
        return this;
    }

    /**
     * Appends the {@link SQLOperator} with an {@link Operation#OR}
     *
     * @param sqlOperator The condition to append.
     * @return This instance.
     */
    @NonNull
    public OperatorGroup or(SQLOperator sqlOperator) {
        return operator(Operation.OR, sqlOperator);
    }

    /**
     * Appends the {@link SQLOperator} with an {@link Operation#AND}
     */
    @NonNull
    public OperatorGroup and(SQLOperator sqlOperator) {
        return operator(Operation.AND, sqlOperator);
    }

    /**
     * Applies the {@link Operation#AND} to all of the passed
     * {@link SQLOperator}.
     */
    @NonNull
    public OperatorGroup andAll(SQLOperator... sqlOperators) {
        for (SQLOperator sqlOperator : sqlOperators) {
            and(sqlOperator);
        }
        return this;
    }

    /**
     * Applies the {@link Operation#AND} to all of the passed
     * {@link SQLOperator}.
     */
    @NonNull
    public OperatorGroup andAll(Collection<SQLOperator> sqlOperators) {
        for (SQLOperator sqlOperator : sqlOperators) {
            and(sqlOperator);
        }
        return this;
    }

    /**
     * Applies the {@link Operation#AND} to all of the passed
     * {@link SQLOperator}.
     */
    @NonNull
    public OperatorGroup orAll(SQLOperator... sqlOperators) {
        for (SQLOperator sqlOperator : sqlOperators) {
            or(sqlOperator);
        }
        return this;
    }

    /**
     * Applies the {@link Operation#AND} to all of the passed
     * {@link SQLOperator}.
     */
    @NonNull
    public OperatorGroup orAll(Collection<SQLOperator> sqlOperators) {
        for (SQLOperator sqlOperator : sqlOperators) {
            or(sqlOperator);
        }
        return this;
    }

    /**
     * Appends the {@link SQLOperator} with the specified operator string.
     */
    @NonNull
    private OperatorGroup operator(String operator, SQLOperator sqlOperator) {
        setPreviousSeparator(operator);
        conditionsList.add(sqlOperator);
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
            SQLOperator condition = conditionsList.get(i);
            condition.appendConditionToQuery(queryBuilder);
            if (!allCommaSeparated && condition.hasSeparator() && i < conditionListSize - 1) {
                queryBuilder.appendSpaceSeparated(condition.separator());
            } else if (i < conditionListSize - 1) {
                queryBuilder.append(", ");
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
            query = getQuerySafe();
        }
        return query == null ? "" : query.toString();
    }

    @Override
    public String toString() {
        return getQuerySafe().toString();
    }

    public int size() {
        return conditionsList.size();
    }

    @NonNull
    public List<SQLOperator> getConditions() {
        return conditionsList;
    }

    @Override
    public Iterator<SQLOperator> iterator() {
        return conditionsList.iterator();
    }

    private QueryBuilder getQuerySafe() {
        QueryBuilder query = new QueryBuilder();
        appendConditionToQuery(query);
        return query;
    }
}
