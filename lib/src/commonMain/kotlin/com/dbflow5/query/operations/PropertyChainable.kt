package com.dbflow5.query.operations

import com.dbflow5.sql.Query

/**
 * Allows [Property] types to also chain their operators
 */
interface PropertyChainable<ValueType>
    : Query,
    OpStart<ValueType>,
    Operator<ValueType> {

    infix fun eq(value: AnyOperator) = chainProperty(Operation.Equals, value)

    infix fun notEq(value: AnyOperator) =
        chainProperty(Operation.NotEquals, value)

    infix fun greaterThan(value: AnyOperator) =
        chainProperty(Operation.GreaterThan, value)

    infix fun greaterThanOrEq(value: AnyOperator) =
        chainProperty(Operation.GreaterThanOrEquals, value)

    infix fun lessThan(value: AnyOperator) =
        chainProperty(Operation.LessThan, value)

    infix fun lessThanOrEq(value: AnyOperator) =
        chainProperty(Operation.LessThanOrEquals, value)

    infix operator fun plus(value: AnyOperator) =
        chainProperty(Operation.Plus, value)

    infix operator fun minus(value: AnyOperator) =
        chainProperty(Operation.Minus, value)

    infix operator fun div(value: AnyOperator) =
        chainProperty(Operation.Division, value)

    infix operator fun times(value: AnyOperator) =
        chainProperty(Operation.Times, value)

    infix operator fun rem(value: AnyOperator) =
        chainProperty(Operation.Rem, value)

    fun `in`(firstValue: Query, vararg values: Query) =
        inOp(
            isIn = true,
            nameAlias = nameAlias,
            firstValue = firstValue,
            values = values,
        )

    fun notIn(firstValue: Query, vararg values: Query) =
        inOp(
            isIn = false,
            nameAlias = nameAlias,
            firstValue = firstValue,
            values = values,
        )

    fun chainProperty(
        operation: Operation,
        value: AnyOperator,
    ) = literalOf(
        OperatorGroup.nonGroupingClause().chain(operation, this)
            .chain(operation, value)
    )

}

infix fun PropertyChainable<String>.concatenate(
    value: AnyOperator
) = chainProperty(
    Operation.Concatenate,
    value
)

infix fun PropertyChainable<String?>.like(value: AnyOperator) = chainProperty(Operation.Like, value)

infix fun PropertyChainable<String?>.notLike(value: AnyOperator) =
    chainProperty(Operation.NotLike, value)

infix fun PropertyChainable<String?>.glob(value: AnyOperator) = chainProperty(Operation.Glob, value)

infix fun PropertyChainable<String?>.match(value: AnyOperator) =
    chainProperty(Operation.Match, value)
