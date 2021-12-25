@file:Suppress("UNCHECKED_CAST")

package com.dbflow5.query

import com.dbflow5.annotation.Collate
import com.dbflow5.appendOptional
import com.dbflow5.config.FlowLog
import com.dbflow5.config.FlowManager
import com.dbflow5.converter.TypeConverter
import com.dbflow5.query.property.Property
import com.dbflow5.query.property.TypeConvertedProperty
import com.dbflow5.sql.Query

/**
 * Description: The class that contains a column name, Operator<T>, and value.
 * This class is mostly reserved for internal use at this point. Using this class directly should be avoided
 * and use the generated [Property] instead.
</T> */
class Operator<T : Any?>
internal constructor(
    nameAlias: NameAlias?,
    private val table: Class<*>? = null,
    private val getter: TypeConvertedProperty.TypeConverterGetter? = null,
    private val convertToDB: Boolean
) : BaseOperator(nameAlias), IOperator<T> {

    private val typeConverter: TypeConverter<*, *>? by lazy {
        table?.let { table ->
            getter?.getTypeConverter(
                table
            )
        }
    }

    private var convertToString = true

    override val query: String
        get() = appendToQuery()


    internal constructor(operator: Operator<*>)
        : this(operator.nameAlias, operator.table, operator.getter, operator.convertToDB) {
        this.value = operator.value
    }

    override fun appendConditionToQuery(queryBuilder: StringBuilder) {
        queryBuilder.append(columnName()).append(operation())

        // Do not use value for certain operators
        // If is raw, we do not want to convert the value to a string.
        if (isValueSet) {
            queryBuilder.append(
                if (convertToString) convertObjectToString(
                    value(),
                    true
                ) else value()
            )
        }

        postArgument()?.let { queryBuilder.append(" $it") }
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
        operation = " ${Operation.LIKE} "
        return value(value)
    }


    /**
     * Uses the MATCH operation. Faster than [like], using an FTS4 Table.
     *
     * . If the WHERE clause of the SELECT statement contains a sub-clause of the form "<column> MATCH ?",
     * FTS is able to use the built-in full-text index to restrict the search to those documents
     * that match the full-text query string specified as the right-hand operand of the MATCH clause.
     *
     * @param value a simple string to match.

     */
    override fun match(value: String): Operator<T> {
        operation = " ${Operation.MATCH} "
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
     *
     */
    override fun notLike(value: String): Operator<T> {
        operation = " ${Operation.NOT_LIKE} "
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
     *
     */
    override fun glob(value: String): Operator<T> {
        operation = " ${Operation.GLOB} "
        return value(value)
    }

    /**
     * The value of the parameter
     *
     * @param value The value of the column in the DB
     *
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
     *
     */
    fun operation(operation: String) = apply {
        this.operation = operation
    }

    /**
     * Adds a COLLATE to the end of this condition
     *
     * @param collation The SQLite collate function
     * .
     */
    infix fun collate(collation: String) = apply {
        postArg = "COLLATE $collation"
    }

    /**
     * Adds a COLLATE to the end of this condition using the [Collate] enum.
     *
     * @param collation The SQLite collate function
     * .
     */
    infix fun collate(collation: Collate) = apply {
        if (collation == Collate.NONE) {
            postArg = null
        } else {
            collate(collation.name)
        }
    }

    /**
     * Appends an optional SQL string to the end of this condition
     */
    infix fun postfix(postfix: String) = apply {
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

    override fun between(conditional: IConditional): Between<*> =
        Between(this as Operator<Any>, conditional)

    override fun `in`(firstConditional: IConditional, vararg conditionals: IConditional): In<*> =
        In(this as Operator<Any>, firstConditional, true, *conditionals)

    override fun notIn(firstConditional: IConditional, vararg conditionals: IConditional): In<*> =
        In(this as Operator<Any>, firstConditional, false, *conditionals)

    override fun notIn(
        firstBaseModelQueriable: BaseModelQueriable<*>,
        vararg baseModelQueriables: BaseModelQueriable<*>
    ): In<*> =
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

    override fun `in`(
        firstBaseModelQueriable: BaseModelQueriable<*>,
        vararg baseModelQueriables: BaseModelQueriable<*>
    ): In<*> =
        In(this as Operator<Any>, firstBaseModelQueriable, true, *baseModelQueriables)

    override fun concatenate(value: Any?): Operator<T> {
        var _value = value
        operation = "${Operation.EQUALS}${columnName()}"

        var typeConverter: TypeConverter<*, Any>? = this.typeConverter as TypeConverter<*, Any>?
        if (typeConverter == null && _value != null) {
            typeConverter =
                FlowManager.getTypeConverterForClass(_value.javaClass) as TypeConverter<*, Any>?
        }
        if (typeConverter != null && convertToDB && _value != null) {
            _value = typeConverter.getDBValue(_value)
        }
        operation = when (_value) {
            is String, is IOperator<*>, is Char -> "$operation ${Operation.CONCATENATE} "
            is Number -> "$operation ${Operation.PLUS} "
            else -> throw IllegalArgumentException(
                "Cannot concatenate the ${if (_value != null) _value.javaClass else "null"}"
            )
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

    override fun convertObjectToString(obj: Any?, appendInnerParenthesis: Boolean): String =
        (typeConverter as? TypeConverter<*, Any>?)?.let { typeConverter ->
            var converted = obj
            try {
                converted = if (convertToDB && obj != null) typeConverter.getDBValue(obj) else obj
            } catch (c: ClassCastException) {
                // if object type is not valid converted type, just use type as is here.
                // if object type is not valid converted type, just use type as is here.
                FlowLog.log(
                    FlowLog.Level.I, "Value passed to operation is not valid type" +
                        " for TypeConverter in the column. Preserving value $obj to be used as is."
                )
            }

            convertValueToString(converted, appendInnerParenthesis, false)
        } ?: super.convertObjectToString(obj, appendInnerParenthesis)

    private fun assignValueOp(value: Any?, operation: String): Operator<T> {
        return if (!isValueSet) {
            this.operation = operation
            value(value)
        } else {
            convertToString = false
            // convert value to a string value because of conversion.
            value(convertValueToString(this.value) + operation + convertValueToString(value))
        }
    }

    /**
     * Static constants that define condition operations
     */
    object Operation {

        /**
         * Equals comparison
         */
        const val EQUALS = "="

        /**
         * Not-equals comparison
         */
        const val NOT_EQUALS = "!="

        /**
         * String concatenation
         */
        const val CONCATENATE = "||"

        /**
         * Number addition
         */
        const val PLUS = "+"

        /**
         * Number subtraction
         */
        const val MINUS = "-"

        const val DIVISION = "/"

        const val MULTIPLY = "*"

        const val MOD = "%"

        /**
         * If something is LIKE another (a case insensitive search).
         * There are two wildcards: % and _
         * % represents [0,many) numbers or characters.
         * The _ represents a single number or character.
         */
        const val LIKE = "LIKE"

        /**
         * If the WHERE clause of the SELECT statement contains a sub-clause of the form "<column> MATCH ?",
         * FTS is able to use the built-in full-text index to restrict the search to those documents
         * that match the full-text query string specified as the right-hand operand of the MATCH clause.
         */
        const val MATCH = "MATCH"

        /**
         * If something is NOT LIKE another (a case insensitive search).
         * There are two wildcards: % and _
         * % represents [0,many) numbers or characters.
         * The _ represents a single number or character.
         */
        const val NOT_LIKE = "NOT LIKE"

        /**
         * If something is case sensitive like another.
         * It must be a string to escape it properly.
         * There are two wildcards: * and ?
         * * represents [0,many) numbers or characters.
         * The ? represents a single number or character
         */
        const val GLOB = "GLOB"

        /**
         * Greater than some value comparison
         */
        const val GREATER_THAN = ">"

        /**
         * Greater than or equals to some value comparison
         */
        const val GREATER_THAN_OR_EQUALS = ">="

        /**
         * Less than some value comparison
         */
        const val LESS_THAN = "<"

        /**
         * Less than or equals to some value comparison
         */
        const val LESS_THAN_OR_EQUALS = "<="

        /**
         * Between comparison. A simplification of X&lt;Y AND Y&lt;Z to Y BETWEEN X AND Z
         */
        const val BETWEEN = "BETWEEN"

        /**
         * AND comparison separator
         */
        const val AND = "AND"

        /**
         * OR comparison separator
         */
        const val OR = "OR"

        /**
         * An empty value for the condition.
         */
        @Deprecated(
            replaceWith = ReplaceWith(
                expression = "Property.WILDCARD",
                imports = ["com.dbflow5.query.Property"]
            ),
            message = "Deprecated. This will translate to '?' in the query as it get's SQL-escaped. " +
                "Use the Property.WILDCARD instead to get desired ? behavior."
        )
        const val EMPTY_PARAM = "?"

        /**
         * Special operation that specify if the column is not null for a specified row. Use of this as
         * an operator will ignore the value of the [Operator] for it.
         */
        const val IS_NOT_NULL = "IS NOT NULL"

        /**
         * Special operation that specify if the column is null for a specified row. Use of this as
         * an operator will ignore the value of the [Operator] for it.
         */
        const val IS_NULL = "IS NULL"

        /**
         * The SQLite IN command that will select rows that are contained in a list of values.
         * EX: SELECT * from Table where column IN ('first', 'second', etc)
         */
        const val IN = "IN"

        /**
         * The reverse of the [.IN] command that selects rows that are not contained
         * in a list of values specified.
         */
        const val NOT_IN = "NOT IN"
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
    internal constructor(operator: Operator<T>, value: T) : BaseOperator(operator.nameAlias),
        Query {

        private var secondValue: T? = null

        override val query: String
            get() = appendToQuery()

        init {
            this.operation = " ${Operation.BETWEEN} "
            this.value = value
            isValueSet = true
            this.postArg = operator.postArgument()
        }

        infix fun and(secondValue: T?) = apply {
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
        internal constructor(
            operator: Operator<T>,
            firstArgument: T?,
            isIn: Boolean,
            vararg arguments: T?
        ) : super(operator.columnAlias()) {
            inArguments.add(firstArgument)
            inArguments.addAll(arguments)
            operation = " ${if (isIn) Operation.IN else Operation.NOT_IN} "
        }

        internal constructor(operator: Operator<T>, args: Collection<T>, isIn: Boolean) : super(
            operator.columnAlias()
        ) {
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
        infix fun and(argument: T?): In<T> {
            inArguments.add(argument)
            return this
        }

        override fun appendConditionToQuery(queryBuilder: StringBuilder) {
            queryBuilder.append(columnName())
                .append(operation())
                .append("(")
                .append(joinArguments(",", inArguments, this))
                .append(")")
        }
    }

    companion object {

        @JvmStatic
        fun convertValueToString(value: Any?): String? =
            convertValueToString(value, false)

        @JvmStatic
        fun <T> op(column: NameAlias): Operator<T> = Operator(column, convertToDB = false)

        @JvmStatic
        fun <T> op(
            alias: NameAlias, table: Class<*>,
            getter: TypeConvertedProperty.TypeConverterGetter,
            convertToDB: Boolean
        ): Operator<T> =
            Operator(alias, table, getter, convertToDB)
    }

}

fun <T> NameAlias.op() = Operator.op<T>(this)

fun <T> String.op(): Operator<T> = nameAlias.op()

infix fun <T> Operator<T>.and(sqlOperator: SQLOperator): OperatorGroup =
    OperatorGroup.clause(this).and(sqlOperator)

infix fun <T> Operator<T>.or(sqlOperator: SQLOperator): OperatorGroup =
    OperatorGroup.clause(this).or(sqlOperator)

infix fun <T> Operator<T>.andAll(sqlOperator: Collection<SQLOperator>): OperatorGroup =
    OperatorGroup.clause(this).andAll(sqlOperator)

infix fun <T> Operator<T>.orAll(sqlOperator: Collection<SQLOperator>): OperatorGroup =
    OperatorGroup.clause(this).orAll(sqlOperator)

infix fun <T : Any> Operator<T>.`in`(values: Array<T>): Operator.In<T> = when (values.size) {
    1 -> `in`(values[0])
    else -> this.`in`(values[0], *values.sliceArray(IntRange(1, values.size)))
}

infix fun <T : Any> Operator<T>.notIn(values: Array<T>): Operator.In<T> = when (values.size) {
    1 -> notIn(values[0])
    else -> this.notIn(values[0], *values.sliceArray(IntRange(1, values.size)))
}