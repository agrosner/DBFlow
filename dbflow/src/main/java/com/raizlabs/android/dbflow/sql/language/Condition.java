package com.raizlabs.android.dbflow.sql.language;

import com.raizlabs.android.dbflow.annotation.Collate;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.sql.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description: The class that contains a column name, operator, and value. The operator can be any Sqlite conditional
 * operator. The value is the {@link com.raizlabs.android.dbflow.structure.Model} value of the column and WILL be
 * converted into the database value when we run the query.
 */
public class Condition extends BaseCondition implements ITypeConditional {

    public static Condition column(NameAlias column) {
        return new Condition(column);
    }

    /**
     * Creates a new instance
     *
     * @param nameAlias The name of the column in the DB
     */
    Condition(NameAlias nameAlias) {
        super(nameAlias);
    }

    @Override
    public void appendConditionToQuery(QueryBuilder queryBuilder) {
        queryBuilder.append(columnName()).append(operation());

        // Do not use value for certain operators
        // If is raw, we do not want to convert the value to a string.
        if (isValueSet) {
            queryBuilder.append(isRaw ? value() : BaseCondition.convertValueToString(value()));
        }

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
    @Override
    public Condition is(Object value) {
        operation = Operation.EQUALS;
        return value(value);
    }

    /**
     * Assigns the operation to "=" the equals operator.
     *
     * @param value The value of the column from the {@link com.raizlabs.android.dbflow.structure.Model} Note
     *              this value may be type converted if used in a {@link ConditionGroup}
     *              so a {@link com.raizlabs.android.dbflow.structure.Model} value is safe here.
     * @return This condition
     */
    @Override
    public Condition eq(Object value) {
        return is(value);
    }

    /**
     * Assigns the operation to "!="
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    @Override
    public Condition isNot(Object value) {
        operation = Operation.NOT_EQUALS;
        return value(value);
    }

    /**
     * Assigns the operation to "!="
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    @Override
    public Condition notEq(Object value) {
        return isNot(value);
    }

    /**
     * Uses the LIKE operation. Case insensitive comparisons.
     *
     * @param value Uses sqlite LIKE regex to match rows.
     *              It must be a string to escape it properly.
     *              There are two wildcards: % and _
     *              % represents [0,many) numbers or characters.
     *              The _ represents a single number or character.
     * @return This condition
     */
    @Override
    public Condition like(Object value) {
        operation = String.format(" %1s ", Operation.LIKE);
        return value(value);
    }

    /**
     * Uses the GLOB operation. Similar to LIKE except it uses case sensitive comparisons.
     *
     * @param value Uses sqlite GLOB regex to match rows.
     *              It must be a string to escape it properly.
     *              There are two wildcards: * and ?
     *              * represents [0,many) numbers or characters.
     *              The ? represents a single number or character
     * @return This condition
     */
    @Override
    public Condition glob(Object value) {
        operation = String.format(" %1s ", Operation.GLOB);
        return value(value);
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
     * Assigns operation to "&gt;"
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    @Override
    public Condition greaterThan(Object value) {
        operation = Operation.GREATER_THAN;
        return value(value);
    }

    /**
     * Assigns operation to "&gt;="
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    @Override
    public Condition greaterThanOrEq(Object value) {
        operation = Operation.GREATER_THAN_OR_EQUALS;
        return value(value);
    }

    /**
     * Assigns operation to "&lt;"
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    @Override
    public Condition lessThan(Object value) {
        operation = Operation.LESS_THAN;
        return value(value);
    }

    /**
     * Assigns operation to "&lt;="
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    @Override
    public Condition lessThanOrEq(Object value) {
        operation = Operation.LESS_THAN_OR_EQUALS;
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

    @Override
    public Condition isNull() {
        operation = String.format(" %1s ", Operation.IS_NULL);
        return this;
    }

    @Override
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
    @Override
    public Condition separator(String separator) {
        this.separator = separator;
        return this;
    }

    @Override
    public Condition is(ITypeConditional conditional) {
        return is((Object) conditional);
    }

    @Override
    public Condition eq(ITypeConditional conditional) {
        return eq((Object) conditional);
    }

    @Override
    public Condition isNot(ITypeConditional conditional) {
        return isNot((Object) conditional);
    }

    @Override
    public Condition notEq(ITypeConditional conditional) {
        return notEq((Object) conditional);
    }

    @Override
    public Condition like(ITypeConditional conditional) {
        return like((Object) conditional);
    }

    @Override
    public Condition glob(ITypeConditional conditional) {
        return glob((Object) conditional);
    }

    @Override
    public Condition greaterThan(ITypeConditional conditional) {
        return greaterThan((Object) conditional);
    }

    @Override
    public Condition greaterThanOrEq(ITypeConditional conditional) {
        return greaterThanOrEq((Object) conditional);
    }

    @Override
    public Condition lessThan(ITypeConditional conditional) {
        return lessThan((Object) conditional);
    }

    @Override
    public Condition lessThanOrEq(ITypeConditional conditional) {
        return lessThanOrEq((Object) conditional);
    }

    @Override
    public Between between(ITypeConditional conditional) {
        return between((Object) conditional);
    }

    @Override
    public In in(ITypeConditional firstConditional, ITypeConditional... conditionals) {
        return in((Object) firstConditional, conditionals);
    }

    @Override
    public In notIn(ITypeConditional firstConditional, ITypeConditional... conditionals) {
        return notIn((Object) firstConditional, conditionals);
    }

    @Override
    public In notIn(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable[] baseModelQueriables) {
        return notIn((Object) firstBaseModelQueriable, baseModelQueriables);
    }

    @Override
    public Condition is(BaseModelQueriable baseModelQueriable) {
        return is((Object) baseModelQueriable);
    }

    @Override
    public Condition eq(BaseModelQueriable baseModelQueriable) {
        return eq((Object) baseModelQueriable);
    }

    @Override
    public Condition isNot(BaseModelQueriable baseModelQueriable) {
        return isNot((Object) baseModelQueriable);
    }

    @Override
    public Condition notEq(BaseModelQueriable baseModelQueriable) {
        return notEq((Object) baseModelQueriable);
    }

    @Override
    public Condition like(BaseModelQueriable baseModelQueriable) {
        return like((Object) baseModelQueriable);
    }

    @Override
    public Condition glob(BaseModelQueriable baseModelQueriable) {
        return glob((Object) baseModelQueriable);
    }

    @Override
    public Condition greaterThan(BaseModelQueriable baseModelQueriable) {
        return greaterThan((Object) baseModelQueriable);
    }

    @Override
    public Condition greaterThanOrEq(BaseModelQueriable baseModelQueriable) {
        return greaterThanOrEq((Object) baseModelQueriable);
    }

    @Override
    public Condition lessThan(BaseModelQueriable baseModelQueriable) {
        return lessThan((Object) baseModelQueriable);
    }

    @Override
    public Condition lessThanOrEq(BaseModelQueriable baseModelQueriable) {
        return lessThanOrEq((Object) baseModelQueriable);
    }

    @Override
    public Between between(BaseModelQueriable baseModelQueriable) {
        return between((Object) baseModelQueriable);
    }

    @Override
    public In in(BaseModelQueriable firstBaseModelQueriable, BaseModelQueriable[] baseModelQueriables) {
        return in((Object) firstBaseModelQueriable, baseModelQueriables);
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();
        appendConditionToQuery(queryBuilder);
        return queryBuilder.getQuery();
    }

    @Override
    public Condition concatenate(Object value) {
        operation = new QueryBuilder(Operation.EQUALS).append(columnName()).toString();
        if (value != null && !isRaw) {
            TypeConverter typeConverter = FlowManager.getTypeConverterForClass(value.getClass());
            if (typeConverter != null) {
                //noinspection unchecked
                value = typeConverter.getDBValue(value);
            }
        }
        if (value instanceof String || value instanceof ITypeConditional) {
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

    @Override
    public Condition concatenate(ITypeConditional conditional) {
        return concatenate((Object) conditional);
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
         * Greater than or equals to some value comparison
         */
        public static final String GREATER_THAN_OR_EQUALS = ">=";

        /**
         * Less than some value comparison
         */
        public static final String LESS_THAN = "<";

        /**
         * Less than or equals to some value comparison
         */
        public static final String LESS_THAN_OR_EQUALS = "<=";

        /**
         * Between comparison. A simplification of X&lt;Y AND Y&lt;Z to Y BETWEEN X AND Z
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
         * An empty value for the condition.
         */
        public static final String EMPTY_PARAM = "?";

        /**
         * Special operation that specify if the column is not null for a specified row. Use of this as
         * an operator will ignore the value of the {@link Condition} for it.
         */
        public static final String IS_NOT_NULL = "IS NOT NULL";

        /**
         * Special operation that specify if the column is null for a specified row. Use of this as
         * an operator will ignore the value of the {@link Condition} for it.
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
            super(condition.nameAlias);
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
        public void appendConditionToQuery(QueryBuilder queryBuilder) {
            queryBuilder.append(columnName()).append(operation())
                    .append(isRaw ? value() : BaseCondition.convertValueToString(value()))
                    .appendSpaceSeparated(Operation.AND)
                    .append(isRaw ? secondValue() : BaseCondition.convertValueToString(secondValue()))
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
         * @param isIn          if this is an {@link Condition.Operation#IN}
         *                      statement or a {@link Condition.Operation#NOT_IN}
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
        public void appendConditionToQuery(QueryBuilder queryBuilder) {
            queryBuilder.append(columnName()).append(operation())
                    .append("(").append(ConditionGroup.joinArguments(",", inArguments)).append(")");
        }
    }

}
