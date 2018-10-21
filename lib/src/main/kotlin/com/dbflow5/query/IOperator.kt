package com.dbflow5.query

import com.dbflow5.sql.Query

/**
 * Description: Interface for objects that can be used as [Operator] that have a type parameter.
 */
interface IOperator<T> : Query, IConditional {

    /**
     * Assigns the operation to "="
     *
     * @param value The [T] that we express equality to.
     * @return A [Operator] that represents equality between this and the parameter.
     */
    infix fun `is`(value: T?): Operator<T>

    /**
     * Assigns the operation to "=". Identical to [.is]
     *
     * @param value The [T] that we express equality to.
     * @return A [Operator] that represents equality between this and the parameter.
     * @see .is
     */
    infix fun eq(value: T?): Operator<T>

    /**
     * Generates a [Operator] that concatenates this [IOperator] with the [T] via "||"
     * by columnName=columnName || value
     *
     * @param value The value to concatenate.
     * @return A [<] that represents concatenation.
     */
    infix fun concatenate(value: Any?): Operator<T>

    /**
     * Assigns the operation to "!="
     *
     * @param value The [T] that we express inequality to.
     * @return A [<] that represents inequality between this and the parameter.
     */
    infix fun isNot(value: T?): Operator<T>

    /**
     * Assigns the operation to "!="
     *
     * @param value The [T] that we express inequality to.
     * @return A [<] that represents inequality between this and the parameter.
     * @see .notEq
     */
    infix fun notEq(value: T?): Operator<T>

    /**
     * Assigns operation to "&gt;"
     *
     * @param value The [T] that this [IOperator] is greater than.
     * @return A [<] that represents greater than between this and the parameter.
     */
    infix fun greaterThan(value: T): Operator<T>

    /**
     * Assigns operation to "&gt;="
     *
     * @param value The [T] that this [IOperator] is greater than or equal to.
     * @return A [<] that represents greater than or equal between this and the parameter.
     */
    infix fun greaterThanOrEq(value: T): Operator<T>


    /**
     * Assigns operation to "&lt;"
     *
     * @param value The [T] that this [IOperator] is less than.
     * @return A [<] that represents less than between this and the parameter.
     */
    infix fun lessThan(value: T): Operator<T>


    /**
     * Assigns operation to "&lt;="
     *
     * @param value The [T] that this [IOperator] is less than or equal to.
     * @return A [<] that represents less than or equal to between this and the parameter.
     */
    infix fun lessThanOrEq(value: T): Operator<T>

    infix fun between(value: T): Operator.Between<T>

    /**
     * Turns this [IOperator] into an [.In][<]. It means that this object should
     * be represented by the set of [T] provided to follow.
     *
     * @param firstValue The first value (required to enforce >= 1)
     * @param values     The rest of the values to pass optionally.
     * @return A new [.In][<] built from this [IOperator].
     */
    fun `in`(firstValue: T, vararg values: T): Operator.In<T>

    /**
     * Turns this [IOperator] into an [.In][<] (not). It means that this object should NOT
     * be represented by the set of [T] provided to follow.
     *
     * @param firstValue The first value (required to enforce >= 1)
     * @param values     The rest of the values to pass optionally.
     * @return A new [.In][<] (not) built from this [IOperator].
     */
    fun notIn(firstValue: T, vararg values: T): Operator.In<T>

    /**
     * Turns this [IOperator] into an [.In][<]. It means that this object should
     * be represented by the set of [T] provided to follow.
     *
     * @param values The rest of the values to pass optionally.
     * @return A new [.In][<] built from this [IOperator].
     */
    infix fun `in`(values: Collection<T>): Operator.In<T>

    /**
     * Turns this [IOperator] into an [.In][<] (not). It means that this object should NOT
     * be represented by the set of [T] provided to follow.
     *
     * @param values The rest of the values to pass optionally.
     * @return A new [.In][<] (not) built from this [IOperator].
     */
    infix fun notIn(values: Collection<T>): Operator.In<T>

    /**
     * Adds another value and returns the operator. i.e p1 + p2
     *
     * @param value the value to add.
     */
    infix operator fun plus(value: T): Operator<T>

    /**
     * Subtracts another value and returns the operator. i.e p1 - p2
     *
     * @param value the value to subtract.
     */
    infix operator fun minus(value: T): Operator<T>

    /**
     * Divides another value and returns as the operator. i.e p1 / p2
     *
     * @param value the value to divide.
     * @return A new instance.
     */
    infix operator fun div(value: T): Operator<T>

    /**
     * Multiplies another value and returns as the operator. i.e p1 * p2
     *
     * @param value the value to multiply.
     */
    infix operator fun times(value: T): Operator<T>

    /**
     * Modulous another value and returns as the operator. i.e p1 % p2
     *
     * @param value the value to calculate remainder of.
     */
    infix operator fun rem(value: T): Operator<T>
}
