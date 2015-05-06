package com.raizlabs.android.dbflow.sql.builder;

import com.raizlabs.android.dbflow.annotation.Collate;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.ColumnAlias;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description: The class that contains a column name, operator, and value. The operator can be any Sqlite conditional
 * operator. The value is the {@link com.raizlabs.android.dbflow.structure.Model} value of the column and WILL be
 * converted into the database value when we run the query.
 */
public class Condition extends BaseCondition {

    /**
     * Creates a new instance with a raw condition query. The values will not be converted into
     * SQL-safe value. Ex: itemOrder =itemOrder + 1. If not raw, this becomes itemOrder ='itemOrder + 1'
     *
     * @param columnName
     * @return This raw condition
     */
    public static Condition columnRaw(String columnName) {
        Condition condition = column(columnName);
        condition.isRaw = true;
        return condition;
    }

    public static Condition column(String columnName) {
        return new Condition(ColumnAlias.column(columnName));
    }

    public static Condition exists() {
        return new Condition(ColumnAlias.columnRaw("EXISTS "));
    }

    /**
     * Constructs instance with the specified {@link ColumnAlias} to enable or disable back ticks on the name.
     *
     * @param columnAlias The alias to use.
     * @return A new {@link Condition}
     */
    public static Condition column(ColumnAlias columnAlias) {
        return new Condition(columnAlias);
    }

    /**
     * Creates a new instance
     *
     * @param columnAlias The name of the column in the DB
     */
    private Condition(ColumnAlias columnAlias) {
        super(columnAlias);
    }

    @Override
    public <ModelClass extends Model> void appendConditionToQuery(
            ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
        conditionQueryBuilder.append(columnName()).append(operation());

        // Do not use value for certain operators
        // If is raw, we do not want to convert the value to a string.
        if (isValueSet) {
            conditionQueryBuilder.append(isRaw ? value() : conditionQueryBuilder.convertValueToString(value()));
        }

        if (postArgument() != null) {
            conditionQueryBuilder.appendSpace().append(postArgument());
        }
    }

    @Override
    public void appendConditionToRawQuery(QueryBuilder queryBuilder) {
        queryBuilder.append(columnName());
        queryBuilder.append(operation())
                .append(value());
        if (postArgument() != null) {
            queryBuilder.appendSpace().append(postArgument());
        }
    }

    /**
     * Assigns the operation to "="
     *
     * @param value The value of the column from the {@link com.raizlabs.android.dbflow.structure.Model} Note
     *              this value may be type converted if used in a {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
     *              so a {@link com.raizlabs.android.dbflow.structure.Model} value is safe here.
     * @return This condition
     */
    public Condition is(Object value) {
        operation = Operation.EQUALS;
        return value(value);
    }

    /**
     * Assigns the operation to "=" the equals operator.
     *
     * @param value The value of the column from the {@link com.raizlabs.android.dbflow.structure.Model} Note
     *              this value may be type converted if used in a {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
     *              so a {@link com.raizlabs.android.dbflow.structure.Model} value is safe here.
     * @return This condition
     */
    public Condition eq(Object value) {
        return is(value);
    }

    /**
     * Assigns the operation to "!="
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    public Condition isNot(Object value) {
        operation = Operation.NOT_EQUALS;
        return value(value);
    }

    /**
     * Uses the LIKE operation. Case insensitive comparisons.
     *
     * @param likeRegex Uses sqlite LIKE regex to match rows.
     *                  It must be a string to escape it properly.
     *                  There are two wildcards: % and _
     *                  % represents [0,many) numbers or characters.
     *                  The _ represents a single number or character.
     * @return This condition
     */
    public Condition like(String likeRegex) {
        operation = String.format(" %1s ", Operation.LIKE);
        return value(likeRegex);
    }

    /**
     * Uses the GLOB operation. Similar to LIKE except it uses case sensitive comparisons.
     *
     * @param globRegex Uses sqlite GLOB regex to match rows.
     *                  It must be a string to escape it properly.
     *                  There are two wildcards: * and ?
     *                  * represents [0,many) numbers or characters.
     *                  The ? represents a single number or character
     * @return This condition
     */
    public Condition glob(String globRegex) {
        operation = String.format(" %1s ", Operation.GLOB);
        return value(globRegex);
    }

    /**
     * The value of the parameter
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    public Condition value(Object value) {
        this.value = value;
        isValueSet = true;
        return this;
    }

    /**
     * Assigns operation to ">"
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    public Condition greaterThan(Object value) {
        operation = Operation.GREATER_THAN;
        return value(value);
    }

    /**
     * Assigns operation to "<"
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    public Condition lessThan(Object value) {
        operation = Operation.LESS_THAN;
        return value(value);
    }

    /**
     * Add a custom operation to this argument
     *
     * @param operation The SQLite operator
     * @return This condition
     */
    public Condition operation(String operation) {
        this.operation = operation;
        return this;
    }

    /**
     * Adds a COLLATE to the end of this condition
     *
     * @param collation The SQLite collate function
     * @return This condition.
     */
    public Condition collate(String collation) {
        postArg = "COLLATE " + collation;
        return this;
    }

    /**
     * Adds a COLLATE to the end of this condition using the {@link com.raizlabs.android.dbflow.annotation.Collate} enum.
     *
     * @param collation The SQLite collate function
     * @return This condition.
     */
    public Condition collate(Collate collation) {
        if (collation.equals(Collate.NONE)) {
            postArg = null;
        } else {
            collate(collation.name());
        }

        return this;
    }

    /**
     * Appends an optional SQL string to the end of this condition
     *
     * @param postfix
     * @return
     */
    public Condition postfix(String postfix) {
        postArg = postfix;
        return this;
    }

    /**
     * Appends IS NULL to the end of this condition
     *
     * @return
     */
    public Condition isNull() {
        operation = String.format(" %1s ", Operation.IS_NULL);
        return this;
    }

    /**
     * Appends IS NOT NULL to the end of this condition
     *
     * @return
     */
    public Condition isNotNull() {
        operation = String.format(" %1s ", Operation.IS_NOT_NULL);
        return this;
    }

    /**
     * Optional separator when chaining this Condition within a {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
     *
     * @param separator The separator to use
     * @return This instance
     */
    public Condition separator(String separator) {
        this.separator = separator;
        return this;
    }

    /**
     * Will concatenate a value to the specified condition such that: itemOrder=itemOrder + value or
     * if its a SQL string: name=name||'value'
     *
     * @param value
     * @return
     */
    @SuppressWarnings("unchecked")
    public Condition concatenateToColumn(Object value) {
        operation = new QueryBuilder(Operation.EQUALS).append(columnName()).toString();
        if (value != null && !isRaw) {
            TypeConverter typeConverter = FlowManager.getTypeConverterForClass(value.getClass());
            if (typeConverter != null) {
                value = typeConverter.getDBValue(value);
            }
        }
        if (value instanceof String) {
            operation = String.format("%1s %1s ", operation, Operation.CONCATENATE);
        } else if (value instanceof Number) {
            operation = String.format("%1s %1s ", operation, Operation.PLUS);
        } else {
            throw new IllegalArgumentException(
                    String.format("Cannot concatenate the %1s", value != null ? value.getClass() : "null"));
        }
        this.value = value;
        isValueSet = true;
        return this;
    }

    /**
     * Turns this condition into a SQL BETWEEN operation
     *
     * @param value The value of the first argument of the BETWEEN clause
     * @return Between operator
     */
    public Between between(Object value) {
        return new Between(this, value);
    }

    /**
     * Turns this condition into a SQL IN operation.
     *
     * @param firstArgument The first value in the IN query as one is required.
     * @param arguments     The 2nd to N values in the IN query. Not required.
     * @return In operator
     */
    public In in(Object firstArgument, Object... arguments) {
        return new In(this, firstArgument, true, arguments);
    }

    /**
     * Turns this condition into a SQL NOT IN operation.
     *
     * @param firstArgument The first value in the NOT IN query as one is required.
     * @param arguments     The 2nd to N values in the IN query. Not required.
     * @return Not In operator
     */
    public In notIn(Object firstArgument, Object... arguments) {
        return new In(this, firstArgument, false, arguments);
    }

    /**
     * Static constants that define condition operations
     */
    public static class Operation {

        /**
         * Equals comparison
         */
        public static final String EQUALS = "=";

        /**
         * Not-equals comparison
         */
        public static final String NOT_EQUALS = "!=";

        /**
         * String concatenation
         */
        public static final String CONCATENATE = "||";

        /**
         * Number addition
         */
        public static final String PLUS = "+";

        /**
         * If something is LIKE another (a case insensitive search).
         * There are two wildcards: % and _
         * % represents [0,many) numbers or characters.
         * The _ represents a single number or character.
         */
        public static final String LIKE = "LIKE";

        /**
         * If something is case sensitive like another.
         * It must be a string to escape it properly.
         * There are two wildcards: * and ?
         * * represents [0,many) numbers or characters.
         * The ? represents a single number or character
         */
        public static final String GLOB = "GLOB";

        /**
         * Greater than some value comparison
         */
        public static final String GREATER_THAN = ">";

        /**
         * Less than some value comparison
         */
        public static final String LESS_THAN = "<";

        /**
         * Between comparison. A simplification of X<Y AND Y<Z to Y BETWEEN X AND Z
         */
        public static final String BETWEEN = "BETWEEN";

        /**
         * AND comparison separator
         */
        public static final String AND = "AND";

        /**
         * OR comparison separator
         */
        public static final String OR = "OR";
        /**
         * An empty value for the condition. If empty in a {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
         * you need to replace all of these with actual values.
         */
        public static final String EMPTY_PARAM = "?";

        /**
         * Special operation that specify if the column is not null for a specified row. Use of this as
         * an operator will ignore the value of the {@link com.raizlabs.android.dbflow.sql.builder.Condition} for it.
         */
        public static final String IS_NOT_NULL = "IS NOT NULL";

        /**
         * Special operation that specify if the column is null for a specified row. Use of this as
         * an operator will ignore the value of the {@link com.raizlabs.android.dbflow.sql.builder.Condition} for it.
         */
        public static final String IS_NULL = "IS NULL";

        /**
         * The SQLite IN command that will select rows that are contained in a list of values.
         * EX: SELECT * from Table where column IN ('first', 'second', etc)
         */
        public static final String IN = "IN";

        /**
         * The reverse of the {@link #IN} command that selects rows that are not contained
         * in a list of values specified.
         */
        public static final String NOT_IN = "NOT IN";
    }

    /**
     * The SQL BETWEEN operator that contains two values instead of the normal 1.
     */
    public static class Between extends BaseCondition {

        private Object secondValue;

        /**
         * Creates a new instance
         *
         * @param condition
         * @param value     The value of the first argument of the BETWEEN clause
         */
        private Between(Condition condition, Object value) {
            super(condition.columnAlias);
            this.operation = String.format(" %1s ", Operation.BETWEEN);
            this.value = value;
            isValueSet = true;
            this.postArg = condition.postArgument();
        }

        public Between and(Object secondValue) {
            this.secondValue = secondValue;
            return this;
        }

        public Object secondValue() {
            return secondValue;
        }

        @Override
        public <ModelClass extends Model> void appendConditionToQuery(
                ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
            conditionQueryBuilder.append(columnName()).append(operation())
                    .append(conditionQueryBuilder.convertValueToString(value()))
                    .appendSpaceSeparated("AND")
                    .append(conditionQueryBuilder.convertValueToString(secondValue()))
                    .appendSpace().appendOptional(postArgument());
        }

        @Override
        public void appendConditionToRawQuery(QueryBuilder queryBuilder) {
            queryBuilder.append(columnName()).append(operation())
                    .append((value()))
                    .appendSpaceSeparated(Operation.AND)
                    .append(secondValue())
                    .appendSpace().appendOptional(postArgument());
        }
    }

    /**
     * The SQL IN and NOT IN operator that specifies a list of values to SELECT rows from.
     * EX: SELECT * FROM myTable WHERE columnName IN ('column1','column2','etc')
     */
    public static class In extends BaseCondition {

        private List<Object> inArguments = new ArrayList<>();

        /**
         * Creates a new instance
         *
         * @param condition     The condition object to pass in. We only use the column name here.
         * @param firstArgument The first value in the IN query as one is required.
         * @param isIn          if this is an {@link com.raizlabs.android.dbflow.sql.builder.Condition.Operation#IN}
         *                      statement or a {@link com.raizlabs.android.dbflow.sql.builder.Condition.Operation#NOT_IN}
         */
        private In(Condition condition, Object firstArgument, boolean isIn, Object... arguments) {
            super(condition.columnAlias());
            inArguments.add(firstArgument);
            Collections.addAll(inArguments, arguments);
            operation = String.format(" %1s ", isIn ? Operation.IN : Operation.NOT_IN);
        }

        /**
         * Appends another value to this In statement
         *
         * @param argument The non-type converted value of the object. The value will be converted
         *                 in a {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}.
         * @return
         */
        public In and(Object argument) {
            inArguments.add(argument);
            return this;
        }

        @Override
        public <ModelClass extends Model> void appendConditionToQuery(
                ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
            conditionQueryBuilder.append(columnName()).append(operation())
                    .append("(").appendArgumentList(inArguments).append(")");
        }

        @Override
        @SuppressWarnings("unchecked")
        public void appendConditionToRawQuery(QueryBuilder queryBuilder) {
            queryBuilder.append(columnName()).append(operation())
                    .append("(").appendList(inArguments).append(")");
        }
    }

    /**
     * Allows combining of {@link SQLCondition} into one condition.
     */
    public static class CombinedCondition extends BaseCondition {

        /**
         * Constructs a new instance of the combined condition.
         *
         * @return A new instance.
         */
        public static CombinedCondition begin(SQLCondition condition) {
            return new CombinedCondition(condition);
        }

        private final List<SQLCondition> conditions = new ArrayList<>();

        private CombinedCondition(SQLCondition condition) {
            conditions.add(condition);
        }

        /**
         * Appends the {@link SQLCondition} with an {@link Operation#OR}
         *
         * @param sqlCondition The condition to append.
         * @return This instance.
         */
        public CombinedCondition or(SQLCondition sqlCondition) {
            return operator(Operation.OR, sqlCondition);
        }

        /**
         * Appends the {@link SQLCondition} with an {@link Operation#AND}
         *
         * @param sqlCondition The condition to append.
         * @return This instance.
         */
        public CombinedCondition and(SQLCondition sqlCondition) {
            return operator(Operation.AND, sqlCondition);
        }

        /**
         * Appends the {@link SQLCondition} with the specified operator string.
         *
         * @param sqlCondition The condition to append.
         * @return This instance.
         */
        public CombinedCondition operator(String operator, SQLCondition sqlCondition) {
            setPreviousSeparator(operator);
            conditions.add(sqlCondition);
            return this;
        }

        @Override
        public <ModelClass extends Model> void appendConditionToQuery(
                ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
            conditionQueryBuilder.append("(");
            for (SQLCondition condition : conditions) {
                condition.appendConditionToQuery(conditionQueryBuilder);
                if(condition.hasSeparator()) {
                    conditionQueryBuilder.appendSpaceSeparated(condition.separator());
                }
            }
            conditionQueryBuilder.append(")");
        }

        @Override
        public void appendConditionToRawQuery(QueryBuilder queryBuilder) {
            queryBuilder.append("(");
            for (SQLCondition condition : conditions) {
                condition.appendConditionToRawQuery(queryBuilder);
            }
            queryBuilder.append(")");
        }

        /**
         * Sets the last condition to use the separator specified
         *
         * @param separator AND, OR, etc.
         */
        private void setPreviousSeparator(String separator) {
            if (conditions.size() > 0) {
                // set previous to use OR separator
                conditions.get(conditions.size() - 1).separator(separator);
            }
        }
    }
}
