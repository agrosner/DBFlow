package com.dbflow5.query2.operations

import com.dbflow5.query.NameAlias
import com.dbflow5.query2.operations.BaseOperator.SingleValueOperator
import com.dbflow5.query2.operations.BaseOperator.ValuelessOperator

/**
 * Description: Simple interface for objects that can be used as Operators.
 */
interface OpStart<ValueType> {

    val valueConverter: SQLValueConverter<ValueType>

    val nameAlias: NameAlias

    /**
     * SQLite 'AS' operator.
     *
     * [shouldAddIdentifierToAlias] - if we should surround `` in the specified alias (default true).
     */
    fun `as`(
        name: String,
        shouldAddIdentifierToAlias: Boolean,
    ): Operator<ValueType>

    fun `as`(
        name: String,
    ): Operator<ValueType> = `as`(name, shouldAddIdentifierToAlias = true)

    /**
     * Returns and assigns [Operation.IsNull]. Returns a [ValuelessOperator]
     */
    fun isNull(): ValuelessOperator = operator(Operation.IsNull)

    /**
     * Returns and assigns [Operation.IsNotNull]. Returns a [ValuelessOperator]
     */
    fun isNotNull(): ValuelessOperator = operator(Operation.IsNotNull)

    /**
     * Represents equality selection with [Operation.Equals].
     */
    infix fun eq(value: ValueType): SingleValueOperator<ValueType> = operator(
        operation = Operation.Equals,
        value = value,
    )

    /**
     * Represents inequality selection with [Operation.NotEquals].
     */
    infix fun notEq(value: ValueType): SingleValueOperator<ValueType> = operator(
        operation = Operation.NotEquals,
        value = value,
    )

    /**
     * Assigns operation to [Operation.GreaterThan]
     */
    infix fun greaterThan(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.GreaterThan,
            value = value,
        )

    /**
     * Assigns operation to [Operation.GreaterThanOrEquals]
     */
    infix fun greaterThanOrEq(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.GreaterThanOrEquals,
            value = value,
        )


    /**
     * Assigns operation to [Operation.LessThan]
     */
    infix fun lessThan(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.LessThan,
            value = value,
        )


    /**
     * Assigns operation to [Operation.LessThanOrEquals]
     */
    infix fun lessThanOrEq(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.LessThanOrEquals,
            value = value,
        )

    infix fun between(value: ValueType): BaseOperator.BetweenStart<ValueType> =
        BetweenImpl(
            nameAlias = nameAlias,
            value = value,
            valueConverter = valueConverter,
        )

    fun `in`(firstValue: ValueType, vararg values: ValueType): BaseOperator.In<ValueType> =
        inOp(
            mutableListOf(firstValue).apply { addAll(values) },
            isIn = true,
        )

    fun notIn(firstValue: ValueType, vararg values: ValueType): BaseOperator.In<ValueType> = inOp(
        mutableListOf(firstValue).apply { addAll(values) },
        isIn = false,
    )

    infix fun `in`(values: Collection<ValueType>): BaseOperator.In<ValueType> =
        inOp(values.toList(), isIn = true)

    infix fun notIn(values: Collection<ValueType>): BaseOperator.In<ValueType> =
        inOp(values.toList(), isIn = false)

    /**
     * Adds this [OpStart] with the value
     * i.e. `name` + 5
     */
    infix operator fun plus(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.Plus,
            value = value,
        )

    /**
     * Subtracts this [OpStart] with the value
     * i.e. `name` - 5
     */
    infix operator fun minus(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.Minus,
            value = value,
        )

    /**
     * Divides this [OpStart] with the value
     * i.e. `name` / 5
     */
    infix operator fun div(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.Division,
            value = value,
        )

    /**
     * Multiplies this [OpStart] with the value
     * i.e. `name` * 5
     */
    infix operator fun times(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.Times,
            value = value,
        )

    /**
     * Find remainer on this [OpStart] with the value
     * i.e. `name` % 5
     */
    infix operator fun rem(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.Rem,
            value = value,
        )
}

/**
 * Extension provides default `AS` with shouldAddIdentifierToAlias = true.
 */
infix fun <ValueType> OpStart<ValueType>.`as`(name: String) =
    `as`(name, shouldAddIdentifierToAlias = true)

/**
 * Generates Expression using [Operation.Like].
 */
infix fun OpStart<String>.like(value: String): SingleValueOperator<String> =
    operator(
        value,
        operation = Operation.Like,
    )

/**
 * Generates Expression using [Operation.Like].
 */
@JvmName("likeNullable")
infix fun OpStart<String?>.like(value: String): SingleValueOperator<String?> =
    operator(
        value,
        operation = Operation.Like,
    )

/**
 * Generates Expression using [Operation.Match].
 */
infix fun OpStart<String>.match(value: String): SingleValueOperator<String> =
    operator(
        value,
        operation = Operation.Match,
    )

/**
 * Generates Expression using [Operation.Match].
 */
@JvmName("matchNullable")
infix fun OpStart<String?>.match(value: String): SingleValueOperator<String?> =
    operator(
        value,
        operation = Operation.Match,
    )

/**
 * Generates Expression using [Operation.NotLike].
 */
infix fun OpStart<String>.notLike(value: String): SingleValueOperator<String> =
    operator(
        value,
        operation = Operation.NotLike,
    )

/**
 * Generates Expression using [Operation.NotLike].
 */
@JvmName("notLikeNullable")
infix fun OpStart<String?>.notLike(value: String): SingleValueOperator<String?> =
    operator(
        value,
        operation = Operation.NotLike,
    )

/**
 * Generates Expression using [Operation.Glob].
 */
infix fun OpStart<String>.glob(value: String): SingleValueOperator<String> =
    operator(
        value,
        operation = Operation.Glob,
    )

/**
 * Generates Expression using [Operation.Glob].
 */
@JvmName("globNullable")
infix fun OpStart<String?>.glob(value: String): SingleValueOperator<String?> =
    operator(
        value,
        operation = Operation.Glob,
    )

internal fun <ValueType> OpStart<ValueType>.operator(
    value: ValueType,
    operation: Operation,
) =
    operator(
        nameAlias = nameAlias,
        operation = operation,
        value = value,
        valueConverter = valueConverter,
    )

internal fun <ValueType> OpStart<ValueType>.operator(
    operation: Operation,
) = operator(nameAlias, operation)

internal fun <ValueType> OpStart<ValueType>.inOp(
    values: List<ValueType>,
    isIn: Boolean,
) = InImpl(
    nameAlias = nameAlias,
    isIn = isIn,
    values = values,
    valueConverter = valueConverter,
)