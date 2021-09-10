package com.raizlabs.android.dbflow.sql.language;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.raizlabs.android.dbflow.annotation.Collate;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.converter.TypeConverter;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Description: The class that contains a column name, Operator<T>, and value.
 * This class is mostly reserved for internal use at this point. Using this class directly should be avoided
 * and use the generated {@link Property} instead.
 */
public class Operator<T> extends BaseOperator implements IOperator<T> {


    private TypeConverter typeConverter;
    private boolean convertToDB;

    public static String convertValueToString(Object value) {
        return BaseOperator.convertValueToString(value, false);
    }

    @NonNull
    public static <T> Operator<T> op(NameAlias column) {
        return new Operator<>(column);
    }

    @NonNull
    public static <T> Operator<T> op(NameAlias alias, TypeConverter typeConverter, boolean convertToDB) {
        return new Operator<>(alias, typeConverter, convertToDB);
    }

    /**
     * Creates a new instance
     *
     * @param nameAlias The name of the column in the DB
     */
    Operator(NameAlias nameAlias) {
        super(nameAlias);
    }

    Operator(NameAlias alias, TypeConverter typeConverter, boolean convertToDB) {
        super(alias);
        this.typeConverter = typeConverter;
        this.convertToDB = convertToDB;
    }

    Operator(Operator operator) {
        super(operator.nameAlias);
        this.typeConverter = operator.typeConverter;
        this.convertToDB = operator.convertToDB;
        this.value = operator.value;
    }

    @Override
    public void appendConditionToQuery(@NonNull QueryBuilder queryBuilder) {
        queryBuilder.append(columnName()).append(operation());

        // Do not use value for certain operators
        // If is raw, we do not want to convert the value to a string.
        if (isValueSet) {
            queryBuilder.append(convertObjectToString(value(), true));
        }

        if (postArgument() != null) {
            queryBuilder.appendSpace().append(postArgument());
        }
    }

    @NonNull
    @Override
    public Operator<T> is(@Nullable T value) {
        operation = Operation.EQUALS;
        return value(value);
    }

    @NonNull
    @Override
    public Operator<T> eq(@Nullable T value) {
        return is(value);
    }

    @NonNull
    @Override
    public Operator<T> isNot(@Nullable T value) {
        operation = Operation.NOT_EQUALS;
        return value(value);
    }

    @NonNull
    @Override
    public Operator<T> notEq(@Nullable T value) {
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
    @NonNull
    @Override
    public Operator<T> like(@NonNull String value) {
        operation = String.format(" %1s ", Operation.LIKE);
        return value(value);
    }

    /**
     * Uses the NOT LIKE operation. Case insensitive comparisons.
     *
     * @param value Uses sqlite LIKE regex to inversely match rows.
     *              It must be a string to escape it properly.
     *              There are two wildcards: % and _
     *              % represents [0,many) numbers or characters.
     *              The _ represents a single number or character.
     * @return This condition
     */
    @NonNull
    @Override
    public Operator<T> notLike(@NonNull String value) {
        operation = String.format(" %1s ", Operation.NOT_LIKE);
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
    @NonNull
    @Override
    public Operator<T> glob(@NonNull String value) {
        operation = String.format(" %1s ", Operation.GLOB);
        return value(value);
    }

    /**
     * The value of the parameter
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    public Operator<T> value(@Nullable Object value) {
        this.value = value;
        isValueSet = true;
        return this;
    }

    @NonNull
    @Override
    public Operator<T> greaterThan(@NonNull T value) {
        operation = Operation.GREATER_THAN;
        return value(value);
    }

    @NonNull
    @Override
    public Operator<T> greaterThanOrEq(@NonNull T value) {
        operation = Operation.GREATER_THAN_OR_EQUALS;
        return value(value);
    }

    @NonNull
    @Override
    public Operator<T> lessThan(@NonNull T value) {
        operation = Operation.LESS_THAN;
        return value(value);
    }

    @NonNull
    @Override
    public Operator<T> lessThanOrEq(@NonNull T value) {
        operation = Operation.LESS_THAN_OR_EQUALS;
        return value(value);
    }

    @NonNull
    @Override
    public Operator<T> plus(@NonNull T value) {
        return assignValueOp(value, Operation.PLUS);
    }

    @NonNull
    @Override
    public Operator<T> minus(@NonNull T value) {
        return assignValueOp(value, Operation.MINUS);
    }

    @NonNull
    @Override
    public Operator<T> div(@NonNull T value) {
        return assignValueOp(value, Operation.DIVISION);
    }

    @Override
    public Operator<T> times(@NonNull T value) {
        return assignValueOp(value, Operation.MULTIPLY);
    }

    @NonNull
    @Override
    public Operator<T> rem(@NonNull T value) {
        return assignValueOp(value, Operation.MOD);
    }

    /**
     * Add a custom operation to this argument
     *
     * @param operation The SQLite operator
     * @return This condition
     */
    @NonNull
    public Operator<T> operation(String operation) {
        this.operation = operation;
        return this;
    }

    /**
     * Adds a COLLATE to the end of this condition
     *
     * @param collation The SQLite collate function
     * @return This condition.
     */
    @NonNull
    public Operator<T> collate(@NonNull String collation) {
        postArg = "COLLATE " + collation;
        return this;
    }

    /**
     * Adds a COLLATE to the end of this condition using the {@link com.raizlabs.android.dbflow.annotation.Collate} enum.
     *
     * @param collation The SQLite collate function
     * @return This condition.
     */
    @NonNull
    public Operator<T> collate(@NonNull Collate collation) {
        if (collation.equals(Collate.NONE)) {
            postArg = null;
        } else {
            collate(collation.name());
        }

        return this;
    }

    /**
     * Appends an optional SQL string to the end of this condition
     */
    @NonNull
    public Operator<T> postfix(@NonNull String postfix) {
        postArg = postfix;
        return this;
    }

    @NonNull
    @Override
    public Operator<T> isNull() {
        operation = String.format(" %1s ", Operation.IS_NULL);
        return this;
    }

    @NonNull
    @Override
    public Operator<T> isNotNull() {
        operation = String.format(" %1s ", Operation.IS_NOT_NULL);
        return this;
    }

    /**
     * Optional separator when chaining this Operator within a {@link OperatorGroup}
     *
     * @param separator The separator to use
     * @return This instance
     */
    @NonNull
    @Override
    public Operator<T> separator(@NonNull String separator) {
        this.separator = separator;
        return this;
    }

    @NonNull
    @Override
    public Operator is(@NonNull IConditional conditional) {
        return assignValueOp(conditional, Operation.EQUALS);
    }

    @NonNull
    @Override
    public Operator eq(@NonNull IConditional conditional) {
        return assignValueOp(conditional, Operation.EQUALS);
    }

    @NonNull
    @Override
    public Operator isNot(@NonNull IConditional conditional) {
        return assignValueOp(conditional, Operation.NOT_EQUALS);
    }

    @NonNull
    @Override
    public Operator notEq(@NonNull IConditional conditional) {
        return assignValueOp(conditional, Operation.NOT_EQUALS);
    }

    @NonNull
    @Override
    public Operator<T> like(@NonNull IConditional conditional) {
        return like(conditional.getQuery());
    }

    @NonNull
    @Override
    public Operator<T> glob(@NonNull IConditional conditional) {
        return glob(conditional.getQuery());
    }

    @NonNull
    @Override
    public Operator<T> greaterThan(@NonNull IConditional conditional) {
        return assignValueOp(conditional, Operation.GREATER_THAN);
    }

    @NonNull
    @Override
    public Operator<T> greaterThanOrEq(@NonNull IConditional conditional) {
        return assignValueOp(conditional, Operation.GREATER_THAN_OR_EQUALS);
    }

    @NonNull
    @Override
    public Operator<T> lessThan(@NonNull IConditional conditional) {
        return assignValueOp(conditional, Operation.LESS_THAN);
    }

    @NonNull
    @Override
    public Operator<T> lessThanOrEq(@NonNull IConditional conditional) {
        return assignValueOp(conditional, Operation.LESS_THAN_OR_EQUALS);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public Between between(@NonNull IConditional conditional) {
        return new Between(this, conditional);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public In in(@NonNull IConditional firstConditional, @NonNull IConditional... conditionals) {
        return new In(this, firstConditional, true, conditionals);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public In notIn(@NonNull IConditional firstConditional, @NonNull IConditional... conditionals) {
        return new In(this, firstConditional, false, conditionals);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public In notIn(@NonNull BaseModelQueriable firstBaseModelQueriable, @NonNull BaseModelQueriable[] baseModelQueriables) {
        return new In(this, firstBaseModelQueriable, false, (Object[]) baseModelQueriables);
    }

    @NonNull
    @Override
    public Operator is(@NonNull BaseModelQueriable baseModelQueriable) {
        return assignValueOp(baseModelQueriable, Operation.EQUALS);
    }

    @NonNull
    @Override
    public Operator eq(@NonNull BaseModelQueriable baseModelQueriable) {
        return assignValueOp(baseModelQueriable, Operation.EQUALS);
    }

    @NonNull
    @Override
    public Operator isNot(@NonNull BaseModelQueriable baseModelQueriable) {
        return assignValueOp(baseModelQueriable, Operation.NOT_EQUALS);
    }

    @NonNull
    @Override
    public Operator notEq(@NonNull BaseModelQueriable baseModelQueriable) {
        return assignValueOp(baseModelQueriable, Operation.NOT_EQUALS);
    }

    @NonNull
    @Override
    public Operator<T> like(@NonNull BaseModelQueriable baseModelQueriable) {
        return assignValueOp(baseModelQueriable, Operation.LIKE);
    }

    @NonNull
    @Override
    public Operator notLike(@NonNull IConditional conditional) {
        return assignValueOp(conditional, Operation.NOT_LIKE);
    }

    @NonNull
    @Override
    public Operator notLike(@NonNull BaseModelQueriable baseModelQueriable) {
        return assignValueOp(baseModelQueriable, Operation.NOT_LIKE);
    }

    @NonNull
    @Override
    public Operator<T> glob(@NonNull BaseModelQueriable baseModelQueriable) {
        return assignValueOp(baseModelQueriable, Operation.GLOB);
    }

    @NonNull
    @Override
    public Operator<T> greaterThan(@NonNull BaseModelQueriable baseModelQueriable) {
        return assignValueOp(baseModelQueriable, Operation.GREATER_THAN);
    }

    @NonNull
    @Override
    public Operator<T> greaterThanOrEq(@NonNull BaseModelQueriable baseModelQueriable) {
        return assignValueOp(baseModelQueriable, Operation.GREATER_THAN_OR_EQUALS);
    }

    @NonNull
    @Override
    public Operator<T> lessThan(@NonNull BaseModelQueriable baseModelQueriable) {
        return assignValueOp(baseModelQueriable, Operation.LESS_THAN);
    }

    @NonNull
    @Override
    public Operator<T> lessThanOrEq(@NonNull BaseModelQueriable baseModelQueriable) {
        return assignValueOp(baseModelQueriable, Operation.LESS_THAN_OR_EQUALS);
    }

    @NonNull
    public Operator plus(IConditional value) {
        return assignValueOp(value, Operation.PLUS);
    }

    @NonNull
    public Operator minus(IConditional value) {
        return assignValueOp(value, Operation.MINUS);
    }

    @NonNull
    public Operator div(IConditional value) {
        return assignValueOp(value, Operation.DIVISION);
    }

    @NonNull
    public Operator times(IConditional value) {
        return assignValueOp(value, Operation.MULTIPLY);
    }

    @NonNull
    public Operator rem(IConditional value) {
        return assignValueOp(value, Operation.MOD);
    }

    @NonNull
    @Override
    public Operator plus(@NonNull BaseModelQueriable value) {
        return assignValueOp(value, Operation.PLUS);
    }

    @NonNull
    @Override
    public Operator minus(@NonNull BaseModelQueriable value) {
        return assignValueOp(value, Operation.MINUS);
    }

    @NonNull
    @Override
    public Operator div(@NonNull BaseModelQueriable value) {
        return assignValueOp(value, Operation.DIVISION);
    }

    @NonNull
    @Override
    public Operator times(@NonNull BaseModelQueriable value) {
        return assignValueOp(value, Operation.MULTIPLY);
    }

    @NonNull
    @Override
    public Operator rem(@NonNull BaseModelQueriable value) {
        return assignValueOp(value, Operation.MOD);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public Between between(@NonNull BaseModelQueriable baseModelQueriable) {
        return new Between(this, baseModelQueriable);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public In in(@NonNull BaseModelQueriable firstBaseModelQueriable, @NonNull BaseModelQueriable... baseModelQueriables) {
        return new In(this, firstBaseModelQueriable, true, baseModelQueriables);
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder();
        appendConditionToQuery(queryBuilder);
        return queryBuilder.getQuery();
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public Operator<T> concatenate(@Nullable Object value) {
        operation = new QueryBuilder(Operation.EQUALS).append(columnName()).toString();

        TypeConverter typeConverter = this.typeConverter;
        if (typeConverter == null && value != null) {
            typeConverter = FlowManager.getTypeConverterForClass(value.getClass());
        }
        if (typeConverter != null && convertToDB) {
            value = typeConverter.getDBValue(value);
        }
        if (value instanceof String || value instanceof IOperator
                || value instanceof Character) {
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

    @NonNull
    @Override
    public Operator<T> concatenate(@NonNull IConditional conditional) {
        return concatenate((Object) conditional);
    }

    /**
     * Turns this condition into a SQL BETWEEN operation
     *
     * @param value The value of the first argument of the BETWEEN clause
     * @return Between operator
     */
    @NonNull
    @Override
    public Between<T> between(@NonNull T value) {
        return new Between<>(this, value);
    }

    @NonNull
    @SafeVarargs
    @Override
    public final In<T> in(@NonNull T firstArgument, T... arguments) {
        return new In<>(this, firstArgument, true, arguments);
    }

    @NonNull
    @SafeVarargs
    @Override
    public final In<T> notIn(@NonNull T firstArgument, T... arguments) {
        return new In<>(this, firstArgument, false, arguments);
    }

    @NonNull
    @Override
    public In<T> in(@NonNull Collection<T> values) {
        return new In<>(this, values, true);
    }

    @NonNull
    @Override
    public In<T> notIn(@NonNull Collection<T> values) {
        return new In<>(this, values, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String convertObjectToString(Object object, boolean appendInnerParenthesis) {
        if (typeConverter != null) {
            Object converted = object;
            try {
                converted = convertToDB ? typeConverter.getDBValue(object) : object;
            } catch (ClassCastException c) {
                // if object type is not valid converted type, just use type as is here.
                FlowLog.log(FlowLog.Level.I, "Value passed to operation is not valid type for TypeConverter in the column. " +
                        "Preserving value " + object + " to be used as is.");
            }
            return BaseOperator.convertValueToString(converted, appendInnerParenthesis, false);
        } else {
            return super.convertObjectToString(object, appendInnerParenthesis);
        }
    }

    private Operator<T> assignValueOp(Object value, String operation) {
        this.operation = operation;
        return value(value);
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
         * Number subtraction
         */
        public static final String MINUS = "-";

        public static final String DIVISION = "/";

        public static final String MULTIPLY = "*";

        public static final String MOD = "%";

        /**
         * If something is LIKE another (a case insensitive search).
         * There are two wildcards: % and _
         * % represents [0,many) numbers or characters.
         * The _ represents a single number or character.
         */
        public static final String LIKE = "LIKE";

        /**
         * If something is NOT LIKE another (a case insensitive search).
         * There are two wildcards: % and _
         * % represents [0,many) numbers or characters.
         * The _ represents a single number or character.
         */
        public static final String NOT_LIKE = "NOT LIKE";

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
         * an operator will ignore the value of the {@link Operator} for it.
         */
        public static final String IS_NOT_NULL = "IS NOT NULL";

        /**
         * Special operation that specify if the column is null for a specified row. Use of this as
         * an operator will ignore the value of the {@link Operator} for it.
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
    public static class Between<T> extends BaseOperator implements Query {

        @Nullable
        private T secondValue;

        /**
         * Creates a new instance
         *
         * @param operator
         * @param value    The value of the first argument of the BETWEEN clause
         */
        private Between(Operator<T> operator, T value) {
            super(operator.nameAlias);
            this.operation = String.format(" %1s ", Operation.BETWEEN);
            this.value = value;
            isValueSet = true;
            this.postArg = operator.postArgument();
        }

        @NonNull
        public Between<T> and(@Nullable T secondValue) {
            this.secondValue = secondValue;
            return this;
        }

        @Nullable
        public T secondValue() {
            return secondValue;
        }

        @Override
        public void appendConditionToQuery(@NonNull QueryBuilder queryBuilder) {
            queryBuilder.append(columnName()).append(operation())
                    .append(convertObjectToString(value(), true))
                    .appendSpaceSeparated(Operation.AND)
                    .append(convertObjectToString(secondValue(), true))
                    .appendSpace().appendOptional(postArgument());
        }

        @Override
        public String getQuery() {
            QueryBuilder builder = new QueryBuilder();
            appendConditionToQuery(builder);
            return builder.getQuery();
        }
    }

    /**
     * The SQL IN and NOT IN operator that specifies a list of values to SELECT rows from.
     * EX: SELECT * FROM myTable WHERE columnName IN ('column1','column2','etc')
     */
    public static class In<T> extends BaseOperator implements Query {

        private List<T> inArguments = new ArrayList<>();

        /**
         * Creates a new instance
         *
         * @param operator      The operator object to pass in. We only use the column name here.
         * @param firstArgument The first value in the IN query as one is required.
         * @param isIn          if this is an {@link Operator.Operation#IN}
         *                      statement or a {@link Operator.Operation#NOT_IN}
         */
        @SafeVarargs
        private In(Operator<T> operator, T firstArgument, boolean isIn, T... arguments) {
            super(operator.columnAlias());
            inArguments.add(firstArgument);
            Collections.addAll(inArguments, arguments);
            operation = String.format(" %1s ", isIn ? Operation.IN : Operation.NOT_IN);
        }

        private In(Operator<T> operator, Collection<T> args, boolean isIn) {
            super(operator.columnAlias());
            inArguments.addAll(args);
            operation = String.format(" %1s ", isIn ? Operation.IN : Operation.NOT_IN);
        }

        /**
         * Appends another value to this In statement
         *
         * @param argument The non-type converted value of the object. The value will be converted
         *                 in a {@link OperatorGroup}.
         * @return
         */
        @NonNull
        public In<T> and(@Nullable T argument) {
            inArguments.add(argument);
            return this;
        }

        @Override
        public void appendConditionToQuery(@NonNull QueryBuilder queryBuilder) {
            queryBuilder.append(columnName()).append(operation())
                    .append("(").append(OperatorGroup.joinArguments(",", inArguments, this)).append(")");
        }

        @Override
        public String getQuery() {
            QueryBuilder builder = new QueryBuilder();
            appendConditionToQuery(builder);
            return builder.getQuery();
        }
    }

}
