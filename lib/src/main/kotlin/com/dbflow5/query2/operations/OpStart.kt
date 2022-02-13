package com.dbflow5.query2.operations

import com.dbflow5.query.NameAlias
import com.dbflow5.query2.operations.BaseOperator.SingleValueOperator

/**
 * Description: Simple interface for objects that can be used as Operators.
 */
interface OpStart<ValueType> {

    val valueConverter: SQLValueConverter<ValueType>

    val nameAlias: NameAlias

    fun `as`(
        name: String,
        shouldAddIdentifierToAlias: Boolean,
    ): Operator<ValueType>

    fun `as`(
        name: String,
    ): Operator<ValueType> = `as`(name, shouldAddIdentifierToAlias = true)

    /**
     * Assigns the operation to "="
     *
     * @param value The [ValueType] that we express equality to.
     * @return A [Operator] that represents equality between this and the parameter.
     */
    infix fun `is`(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.Equals,
            value = value,
        )

    /**
     * Assigns the operation to "=". Identical to [.is]
     *
     * @param value The [ValueType] that we express equality to.
     * @return A [Operator] that represents equality between this and the parameter.
     * @see .is
     */
    infix fun eq(value: ValueType): SingleValueOperator<ValueType> = `is`(value)

    /**
     * Assigns the operation to "!="
     *
     * @param value The [ValueType] that we express inequality to.
     * @return A [<] that represents inequality between this and the parameter.
     */
    infix fun isNot(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.NotEquals,
            value = value,
        )

    /**
     * Assigns the operation to "!="
     *
     * @param value The [ValueType] that we express inequality to.
     * @return A [<] that represents inequality between this and the parameter.
     * @see .notEq
     */
    infix fun notEq(value: ValueType): SingleValueOperator<ValueType> = isNot(value)

    /**
     * Assigns operation to "&gt;"
     *
     * @param value The [ValueType] that this [IOperator] is greater than.
     * @return A [<] that represents greater than between this and the parameter.
     */
    infix fun greaterThan(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.GreaterThan,
            value = value,
        )

    /**
     * Assigns operation to "&gt;="
     *
     * @param value The [ValueType] that this [IOperator] is greater than or equal to.
     * @return A [<] that represents greater than or equal between this and the parameter.
     */
    infix fun greaterThanOrEq(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.GreaterThanOrEquals,
            value = value,
        )


    /**
     * Assigns operation to "&lt;"
     *
     * @param value The [ValueType] that this [IOperator] is less than.
     * @return A [<] that represents less than between this and the parameter.
     */
    infix fun lessThan(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.LessThan,
            value = value,
        )


    /**
     * Assigns operation to "&lt;="
     *
     * @param value The [ValueType] that this [IOperator] is less than or equal to.
     * @return A [<] that represents less than or equal to between this and the parameter.
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

    /**
     * Turns this [IOperator] into an [.In][<]. It means that this object should
     * be represented by the set of [ValueType] provided to follow.
     *
     * @param firstValue The first value (required to enforce >= 1)
     * @param values     The rest of the values to pass optionally.
     * @return A new [.In][<] built from this [IOperator].
     */
    fun `in`(firstValue: ValueType, vararg values: ValueType): BaseOperator.In<ValueType> =
        inOp(
            mutableListOf(firstValue).apply { addAll(values) },
            isIn = true,
        )

    /**
     * Turns this [IOperator] into an [.In][<] (not). It means that this object should NOT
     * be represented by the set of [ValueType] provided to follow.
     *
     * @param firstValue The first value (required to enforce >= 1)
     * @param values     The rest of the values to pass optionally.
     * @return A new [.In][<] (not) built from this [IOperator].
     */
    fun notIn(firstValue: ValueType, vararg values: ValueType): BaseOperator.In<ValueType> = inOp(
        mutableListOf(firstValue).apply { addAll(values) },
        isIn = false,
    )

    /**
     * Turns this [IOperator] into an [.In][<]. It means that this object should
     * be represented by the set of [ValueType] provided to follow.
     *
     * @param values The rest of the values to pass optionally.
     * @return A new [.In][<] built from this [IOperator].
     */
    infix fun `in`(values: Collection<ValueType>): BaseOperator.In<ValueType> =
        inOp(values.toList(), isIn = true)

    /**
     * Turns this [IOperator] into an [.In][<] (not). It means that this object should NOT
     * be represented by the set of [ValueType] provided to follow.
     *
     * @param values The rest of the values to pass optionally.
     * @return A new [.In][<] (not) built from this [IOperator].
     */
    infix fun notIn(values: Collection<ValueType>): BaseOperator.In<ValueType> =
        inOp(values.toList(), isIn = false)

    /**
     * Adds another value and returns the operator. i.e p1 + p2
     *
     * @param value the value to add.
     */
    infix operator fun plus(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.Plus,
            value = value,
        )

    /**
     * Subtracts another value and returns the operator. i.e p1 - p2
     *
     * @param value the value to subtract.
     */
    infix operator fun minus(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.Minus,
            value = value,
        )

    /**
     * Divides another value and returns as the operator. i.e p1 / p2
     *
     * @param value the value to divide.
     * @return A new instance.
     */
    infix operator fun div(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.Division,
            value = value,
        )

    /**
     * Multiplies another value and returns as the operator. i.e p1 * p2
     *
     * @param value the value to multiply.
     */
    infix operator fun times(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.Times,
            value = value,
        )

    /**
     * Modulous another value and returns as the operator. i.e p1 % p2
     *
     * @param value the value to calculate remainder of.
     */
    infix operator fun rem(value: ValueType): SingleValueOperator<ValueType> =
        operator(
            operation = Operation.Rem,
            value = value,
        )
}

infix fun <ValueType> OpStart<ValueType>.`as`(name: String) =
    `as`(name, shouldAddIdentifierToAlias = true)

/**
 * Generates [Operator] using the SQLite LIKE operator.
 */
infix fun OpStart<String>.like(value: String): SingleValueOperator<String> =
    operator(
        value,
        operation = Operation.Like,
    )

/**
 * Generates [Operator] using the SQLite LIKE operator.
 */
@JvmName("likeNullable")
infix fun OpStart<String?>.like(value: String): SingleValueOperator<String?> =
    operator(
        value,
        operation = Operation.Like,
    )

/**
 * Generates [Operator] using the SQLite LIKE operator.
 */
infix fun OpStart<String>.match(value: String): SingleValueOperator<String> =
    operator(
        value,
        operation = Operation.Match,
    )

/**
 * Generates [Operator] using the SQLite LIKE operator.
 */
@JvmName("matchNullable")
infix fun OpStart<String?>.match(value: String): SingleValueOperator<String?> =
    operator(
        value,
        operation = Operation.Match,
    )

/**
 * Generates [Operator] using the SQLite NOT LIKE operator.
 */
infix fun OpStart<String>.notLike(value: String): SingleValueOperator<String> =
    operator(
        value,
        operation = Operation.NotLike,
    )

/**
 * Generates [Operator] using the SQLite NOT LIKE operator.
 */
@JvmName("notLikeNullable")
infix fun OpStart<String?>.notLike(value: String): SingleValueOperator<String?> =
    operator(
        value,
        operation = Operation.NotLike,
    )

/**
 * Generates [Operator] using the SQLite GLOB operator.
 */
infix fun OpStart<String>.glob(value: String): SingleValueOperator<String> =
    operator(
        value,
        operation = Operation.Glob,
    )

/**
 * Generates [Operator] using the SQLite GLOB operator.
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
    OperatorImpl(
        nameAlias = nameAlias,
        operation = operation,
        value = value,
        valueConverter = valueConverter,
    )

internal fun <ValueType> OpStart<ValueType>.inOp(
    values: List<ValueType>,
    isIn: Boolean,
) = InImpl(
    nameAlias = nameAlias,
    isIn = isIn,
    values = values,
    valueConverter = valueConverter,
)