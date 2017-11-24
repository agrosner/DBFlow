@file:Suppress("UNCHECKED_CAST")

package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.annotation.Collate
import com.raizlabs.android.dbflow.appendOptional
import com.raizlabs.android.dbflow.config.FlowLog
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.converter.TypeConverter
import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.language.property.Property

/**
 * Description: The class that contains a column name, Operator<T>, and value.
 * This class is mostly reserved for internal use at this point. Using this class directly should be avoided
 * and use the generated [Property] instead.
</T> */
class Operator<T : Any?> : BaseOperator, IOperator<T> {

    private var typeConverter: TypeConverter<*, *>? = null
    private var convertToDB: Boolean = false

    override val query: String
        get() = appendToQuery()

    /**
     * Creates a new instance
     *
     * @param [nameAlias] The name of the column in the DB
     */
    internal constructor(nameAlias: NameAlias) : super(nameAlias)

    internal constructor(alias: NameAlias, typeConverter: TypeConverter<*, *>, convertToDB: Boolean) : super(alias) {
        this.typeConverter = typeConverter
        this.convertToDB = convertToDB
    }

    internal constructor(operator: Operator<*>) : super(operator.nameAlias) {
        this.typeConverter = operator.typeConverter
        this.convertToDB = operator.convertToDB
        this.value = operator.value
    }

    override fun appendConditionToQuery(queryBuilder: StringBuilder) {
        queryBuilder.append(columnName()).append(operation())

        // Do not use value for certain operators
        // If is raw, we do not want to convert the value to a string.
        if (isValueSet) {
            queryBuilder.append(convertObjectToString(value(), true))
        }

        if (postArgument() != null) {
            queryBuilder.append(" ${postArgument()}")
        }
    }

    override fun isNull() = apply {
        operation = " ${Operation.IS_NULL} "
    }

    override fun isNotNull() = apply {
        operation = " ${Operation.IS_NOT_NULL} "
    }

    override fun `is`(value: T?): Operator<T> {
        operation = Operation.EQUALS
        return value(value)
    }

    override fun eq(value: T?): Operator<T> = `is`(value)

    override fun isNot(value: T?): Operator<T> {
        operation = Operation.NOT_EQUALS
        return value(value)
    }

    override fun notEq(value: T?): Operator<T> = isNot(value)

    /**
     * Uses the LIKE operation. Case insensitive comparisons.
     *
     * @param value Uses sqlite LIKE regex to match rows.
     * It must be a string to escape it properly.
     * There are two wildcards: % and _
     * % represents [0,many) numbers or characters.
     * The _ represents a single number or character.
     * @return This condition
     */
    override fun like(value: String): Operator<T> {
        operation = String.format(" %1s ", Operation.LIKE)
        return value(value)
    }

    /**
     * Uses the NOT LIKE operation. Case insensitive comparisons.
     *
     * @param value Uses sqlite LIKE regex to inversely match rows.
     * It must be a string to escape it properly.
     * There are two wildcards: % and _
     * % represents [0,many) numbers or characters.
     * The _ represents a single number or character.
     * @return This condition
     */
    override fun notLike(value: String): Operator<T> {
        operation = String.format(" %1s ", Operation.NOT_LIKE)
        return value(value)
    }

    /**
     * Uses the GLOB operation. Similar to LIKE except it uses case sensitive comparisons.
     *
     * @param value Uses sqlite GLOB regex to match rows.
     * It must be a string to escape it properly.
     * There are two wildcards: * and ?
     * * represents [0,many) numbers or characters.
     * The ? represents a single number or character
     * @return This condition
     */
    override fun glob(value: String): Operator<T> {
        operation = String.format(" %1s ", Operation.GLOB)
        return value(value)
    }

    /**
     * The value of the parameter
     *
     * @param value The value of the column in the DB
     * @return This condition
     */
    fun value(value: Any?) = apply {
        this.value = value
        isValueSet = true
    }

    override fun greaterThan(value: T): Operator<T> {
        operation = Operation.GREATER_THAN
        return value(value)
    }

    override fun greaterThanOrEq(value: T): Operator<T> {
        operation = Operation.GREATER_THAN_OR_EQUALS
        return value(value)
    }

    override fun lessThan(value: T): Operator<T> {
        operation = Operation.LESS_THAN
        return value(value)
    }

    override fun lessThanOrEq(value: T): Operator<T> {
        operation = Operation.LESS_THAN_OR_EQUALS
        return value(value)
    }

    override fun plus(value: T): Operator<T> = assignValueOp(value, Operation.PLUS)

    override fun minus(value: T): Operator<T> = assignValueOp(value, Operation.MINUS)

    override fun div(value: T): Operator<T> = assignValueOp(value, Operation.DIVISION)

    override fun times(value: T): Operator<T> = assignValueOp(value, Operation.MULTIPLY)

    override fun rem(value: T): Operator<T> = assignValueOp(value, Operation.MOD)

    /**
     * Add a custom operation to this argument
     *
     * @param operation The SQLite operator
     * @return This condition
     */
    fun operation(operation: String) = apply {
        this.operation = operation
    }

    /**
     * Adds a COLLATE to the end of this condition
     *
     * @param collation The SQLite collate function
     * @return This condition.
     */
    fun collate(collation: String) = apply {
        postArg = "COLLATE " + collation
    }

    /**
     * Adds a COLLATE to the end of this condition using the [com.raizlabs.android.dbflow.annotation.Collate] enum.
     *
     * @param collation The SQLite collate function
     * @return This condition.
     */
    fun collate(collation: Collate) = apply {
        if (collation == Collate.NONE) {
            postArg = null
        } else {
            collate(collation.name)
        }
    }

    /**
     * Appends an optional SQL string to the end of this condition
     */
    fun postfix(postfix: String) = apply {
        postArg = postfix
    }

    /**
     * Optional separator when chaining this Operator within a [OperatorGroup]
     *
     * @param separator The separator to use
     * @return This instance
     */
    override fun separator(separator: String) = apply {
        this.separator = separator
    }

    override fun `is`(conditional: IConditional): Operator<*> =
            assignValueOp(conditional, Operation.EQUALS)

    override fun eq(conditional: IConditional): Operator<*> =
            assignValueOp(conditional, Operation.EQUALS)

    override fun isNot(conditional: IConditional): Operator<*> =
            assignValueOp(conditional, Operation.NOT_EQUALS)

    override fun notEq(conditional: IConditional): Operator<*> =
            assignValueOp(conditional, Operation.NOT_EQUALS)

    override fun like(conditional: IConditional): Operator<T> = like(conditional.query)

    override fun glob(conditional: IConditional): Operator<T> = glob(conditional.query)

    override fun greaterThan(conditional: IConditional): Operator<T> =
            assignValueOp(conditional, Operation.GREATER_THAN)

    override fun greaterThanOrEq(conditional: IConditional): Operator<T> =
            assignValueOp(conditional, Operation.GREATER_THAN_OR_EQUALS)

    override fun lessThan(conditional: IConditional): Operator<T> =
            assignValueOp(conditional, Operation.LESS_THAN)

    override fun lessThanOrEq(conditional: IConditional): Operator<T> =
            assignValueOp(conditional, Operation.LESS_THAN_OR_EQUALS)

    override fun between(conditional: IConditional): Between<*> = Between(this as Operator<Any>, conditional)

    override fun `in`(firstConditional: IConditional, vararg conditionals: IConditional): In<*> =
            In(this as Operator<Any>, firstConditional, true, *conditionals)

    override fun notIn(firstConditional: IConditional, vararg conditionals: IConditional): In<*> =
            In(this as Operator<Any>, firstConditional, false, *conditionals)

    override fun notIn(firstBaseModelQueriable: BaseModelQueriable<*>,
                       vararg baseModelQueriables: BaseModelQueriable<*>): In<*> =
            In(this as Operator<Any>, firstBaseModelQueriable, false, *baseModelQueriables)

    override fun `is`(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
            assignValueOp(baseModelQueriable, Operation.EQUALS)

    override fun eq(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
            assignValueOp(baseModelQueriable, Operation.EQUALS)

    override fun isNot(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
            assignValueOp(baseModelQueriable, Operation.NOT_EQUALS)

    override fun notEq(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
            assignValueOp(baseModelQueriable, Operation.NOT_EQUALS)

    override fun like(baseModelQueriable: BaseModelQueriable<*>): Operator<T> =
            assignValueOp(baseModelQueriable, Operation.LIKE)

    override fun notLike(conditional: IConditional): Operator<*> =
            assignValueOp(conditional, Operation.NOT_LIKE)

    override fun notLike(baseModelQueriable: BaseModelQueriable<*>): Operator<*> =
            assignValueOp(baseModelQueriable, Operation.NOT_LIKE)

    override fun glob(baseModelQueriable: BaseModelQueriable<*>): Operator<T> =
            assignValueOp(baseModelQueriable, Operation.GLOB)

    override fun greaterThan(baseModelQueriable: BaseModelQueriable<*>): Operator<T> =
            assignValueOp(baseModelQueriable, Operation.GREATER_THAN)

    override fun greaterThanOrEq(baseModelQueriable: BaseModelQueriable<*>): Operator<T> =
            assignValueOp(baseModelQueriable, Operation.GREATER_THAN_OR_EQUALS)

    override fun lessThan(baseModelQueriable: BaseModelQueriable<*>): Operator<T> =
            assignValueOp(baseModelQueriable, Operation.LESS_THAN)

    override fun lessThanOrEq(baseModelQueriable: BaseModelQueriable<*>): Operator<T> =
            assignValueOp(baseModelQueriable, Operation.LESS_THAN_OR_EQUALS)

    operator fun plus(value: IConditional): Operator<*> = assignValueOp(value, Operation.PLUS)

    operator fun minus(value: IConditional): Operator<*> = assignValueOp(value, Operation.MINUS)

    operator fun div(value: IConditional): Operator<*> = assignValueOp(value, Operation.DIVISION)

    operator fun times(value: IConditional): Operator<*> = assignValueOp(value, Operation.MULTIPLY)

    operator fun rem(value: IConditional): Operator<*> = assignValueOp(value, Operation.MOD)

    override fun plus(value: BaseModelQueriable<*>): Operator<*> =
            assignValueOp(value, Operation.PLUS)

    override fun minus(value: BaseModelQueriable<*>): Operator<*> =
            assignValueOp(value, Operation.MINUS)

    override fun div(value: BaseModelQueriable<*>): Operator<*> =
            assignValueOp(value, Operation.DIVISION)

    override fun times(value: BaseModelQueriable<*>): Operator<*> =
            assignValueOp(value, Operation.MULTIPLY)

    override fun rem(value: BaseModelQueriable<*>): Operator<*> =
            assignValueOp(value, Operation.MOD)

    override fun between(baseModelQueriable: BaseModelQueriable<*>): Between<*> =
            Between(this as Operator<Any>, baseModelQueriable)

    override fun `in`(firstBaseModelQueriable: BaseModelQueriable<*>, vararg baseModelQueriables: BaseModelQueriable<*>): In<*> =
            In(this as Operator<Any>, firstBaseModelQueriable, true, *baseModelQueriables)

    override fun concatenate(value: Any?): Operator<T> {
        var _value = value
        operation = "${Operation.EQUALS}${columnName()}"

        var typeConverter: TypeConverter<*, Any>? = this.typeConverter as TypeConverter<*, Any>?
        if (typeConverter == null && _value != null) {
            typeConverter = FlowManager.getTypeConverterForClass(_value.javaClass) as TypeConverter<*, Any>?
        }
        if (typeConverter != null && convertToDB) {
            _value = typeConverter.getDBValue(_value)
        }
        operation = when (_value) {
            is String, is IOperator<*>, is Char -> "$operation ${Operation.CONCATENATE} "
            is Number -> "$operation ${Operation.PLUS} "
            else -> throw IllegalArgumentException(
                    "Cannot concatenate the ${if (_value != null) _value.javaClass else "null"}")
        }
        this.value = _value
        isValueSet = true
        return this
    }

    override fun concatenate(conditional: IConditional): Operator<T> =
            concatenate(conditional as Any)

    /**
     * Turns this condition into a SQL BETWEEN operation
     *
     * @param value The value of the first argument of the BETWEEN clause
     * @return Between operator
     */
    override fun between(value: T): Between<T> = Between(this, value)

    @SafeVarargs
    override fun `in`(firstValue: T, vararg values: T): In<T> =
            In(this, firstValue, true, *values)

    @SafeVarargs
    override fun notIn(firstValue: T, vararg values: T): In<T> =
            In(this, firstValue, false, *values)

    override fun `in`(values: Collection<T>): In<T> = In(this, values, true)

    override fun notIn(values: Collection<T>): In<T> = In(this, values, false)

    override fun convertObjectToString(obj: Any?, appendInnerParenthesis: Boolean): String? =
            (typeConverter as? TypeConverter<*, Any>?)?.let { typeConverter ->
                var converted = obj
                try {
                    converted = if (convertToDB) typeConverter.getDBValue(obj) else obj
                } catch (c: ClassCastException) {
                    // if object type is not valid converted type, just use type as is here.
                    FlowLog.log(FlowLog.Level.W, throwable = c)
                }

                BaseOperator.convertValueToString(converted, appendInnerParenthesis, false)
            } ?: super.convertObjectToString(obj, appendInnerParenthesis)

    private fun assignValueOp(value: Any?, operation: String): Operator<T> {
        this.operation = operation
        return value(value)
    }

    /**
     * Static constants that define condition operations
     */
    object Operation {

        /**
         * Equals comparison
         */
        val EQUALS = "="

        /**
         * Not-equals comparison
         */
        val NOT_EQUALS = "!="

        /**
         * String concatenation
         */
        val CONCATENATE = "||"

        /**
         * Number addition
         */
        val PLUS = "+"

        /**
         * Number subtraction
         */
        val MINUS = "-"

        val DIVISION = "/"

        val MULTIPLY = "*"

        val MOD = "%"

        /**
         * If something is LIKE another (a case insensitive search).
         * There are two wildcards: % and _
         * % represents [0,many) numbers or characters.
         * The _ represents a single number or character.
         */
        val LIKE = "LIKE"

        /**
         * If something is NOT LIKE another (a case insensitive search).
         * There are two wildcards: % and _
         * % represents [0,many) numbers or characters.
         * The _ represents a single number or character.
         */
        val NOT_LIKE = "NOT LIKE"

        /**
         * If something is case sensitive like another.
         * It must be a string to escape it properly.
         * There are two wildcards: * and ?
         * * represents [0,many) numbers or characters.
         * The ? represents a single number or character
         */
        val GLOB = "GLOB"

        /**
         * Greater than some value comparison
         */
        val GREATER_THAN = ">"

        /**
         * Greater than or equals to some value comparison
         */
        val GREATER_THAN_OR_EQUALS = ">="

        /**
         * Less than some value comparison
         */
        val LESS_THAN = "<"

        /**
         * Less than or equals to some value comparison
         */
        val LESS_THAN_OR_EQUALS = "<="

        /**
         * Between comparison. A simplification of X&lt;Y AND Y&lt;Z to Y BETWEEN X AND Z
         */
        val BETWEEN = "BETWEEN"

        /**
         * AND comparison separator
         */
        val AND = "AND"

        /**
         * OR comparison separator
         */
        val OR = "OR"

        /**
         * An empty value for the condition.
         */
        val EMPTY_PARAM = "?"

        /**
         * Special operation that specify if the column is not null for a specified row. Use of this as
         * an operator will ignore the value of the [Operator] for it.
         */
        val IS_NOT_NULL = "IS NOT NULL"

        /**
         * Special operation that specify if the column is null for a specified row. Use of this as
         * an operator will ignore the value of the [Operator] for it.
         */
        val IS_NULL = "IS NULL"

        /**
         * The SQLite IN command that will select rows that are contained in a list of values.
         * EX: SELECT * from Table where column IN ('first', 'second', etc)
         */
        val IN = "IN"

        /**
         * The reverse of the [.IN] command that selects rows that are not contained
         * in a list of values specified.
         */
        val NOT_IN = "NOT IN"
    }

    /**
     * The SQL BETWEEN operator that contains two values instead of the normal 1.
     */
    class Between<T>
    /**
     * Creates a new instance
     *
     * @param operator
     * @param value    The value of the first argument of the BETWEEN clause
     */
    internal constructor(operator: Operator<T>, value: T) : BaseOperator(operator.nameAlias), Query {

        private var secondValue: T? = null

        override val query: String
            get() = appendToQuery()

        init {
            this.operation = " ${Operation.BETWEEN} "
            this.value = value
            isValueSet = true
            this.postArg = operator.postArgument()
        }

        fun and(secondValue: T?) = apply {
            this.secondValue = secondValue
        }

        fun secondValue(): T? = secondValue

        override fun appendConditionToQuery(queryBuilder: StringBuilder) {
            queryBuilder.append(columnName()).append(operation())
                    .append(convertObjectToString(value(), true))
                    .append(" ${Operation.AND} ")
                    .append(convertObjectToString(secondValue(), true))
                    .append(" ")
                    .appendOptional(postArgument())
        }
    }

    /**
     * The SQL IN and NOT IN operator that specifies a list of values to SELECT rows from.
     * EX: SELECT * FROM myTable WHERE columnName IN ('column1','column2','etc')
     */
    class In<T> : BaseOperator, Query {

        private val inArguments = arrayListOf<T?>()

        override val query: String
            get() = appendToQuery()

        /**
         * Creates a new instance
         *
         * @param operator      The operator object to pass in. We only use the column name here.
         * @param firstArgument The first value in the IN query as one is required.
         * @param isIn          if this is an [Operator.Operation.IN]
         * statement or a [Operator.Operation.NOT_IN]
         */
        @SafeVarargs
        internal constructor(operator: Operator<T>, firstArgument: T?, isIn: Boolean, vararg arguments: T?) : super(operator.columnAlias()) {
            inArguments.add(firstArgument)
            inArguments.addAll(arguments)
            operation = " ${if (isIn) Operation.IN else Operation.NOT_IN} "
        }

        internal constructor(operator: Operator<T>, args: Collection<T>, isIn: Boolean) : super(operator.columnAlias()) {
            inArguments.addAll(args)
            operation = " ${if (isIn) Operation.IN else Operation.NOT_IN} "
        }

        /**
         * Appends another value to this In statement
         *
         * @param argument The non-type converted value of the object. The value will be converted
         * in a [OperatorGroup].
         * @return
         */
        fun and(argument: T?): In<T> {
            inArguments.add(argument)
            return this
        }

        override fun appendConditionToQuery(queryBuilder: StringBuilder) {
            queryBuilder.append(columnName())
                    .append(operation())
                    .append("(")
                    .append(BaseOperator.joinArguments(",", inArguments, this))
                    .append(")")
        }
    }

    companion object {

        @JvmStatic
        fun convertValueToString(value: Any?): String? =
                BaseOperator.convertValueToString(value, false)

        @JvmStatic
        fun <T> op(column: NameAlias): Operator<T> = Operator(column)

        @JvmStatic
        fun <T> op(alias: NameAlias, typeConverter: TypeConverter<*, *>, convertToDB: Boolean): Operator<T> =
                Operator(alias, typeConverter, convertToDB)
    }

}

fun <T : Any> NameAlias.op() = Operator.op<T>(this)

fun <T : Any> String.op(): Operator<T> = nameAlias.op()

infix fun <T : Any> Operator<T>.collate(collation: Collate) = collate(collation)

infix fun <T : Any> Operator<T>.collate(collation: String) = collate(collation)

infix fun <T : Any> Operator<T>.postfix(collation: String) = postfix(collation)

infix fun <T : Any> Operator.Between<T>.and(value: T?) = and(value)

infix fun <T : Any> Operator.In<T>.and(value: T?) = and(value)

infix fun <T : Any> Operator<T>.and(sqlOperator: SQLOperator) = OperatorGroup.clause(this).and(sqlOperator)

infix fun <T : Any> Operator<T>.or(sqlOperator: SQLOperator) = OperatorGroup.clause(this).or(sqlOperator)

infix fun <T : Any> Operator<T>.andAll(sqlOperator: Collection<SQLOperator>) = OperatorGroup.clause(this).andAll(sqlOperator)

infix fun <T : Any> Operator<T>.orAll(sqlOperator: Collection<SQLOperator>) = OperatorGroup.clause(this).orAll(sqlOperator)