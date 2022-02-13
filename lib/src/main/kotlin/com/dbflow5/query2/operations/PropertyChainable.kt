package com.dbflow5.query2.operations

/**
 * Allows [OpStart] types to also chain their operators
 */
interface PropertyChainable<Value>
    : Operator<Value> {

    infix fun `is`(value: AnyOperator) =
        chainProperty(Operation.Equals, value)

    infix fun eq(value: AnyOperator) = `is`(value)

    infix fun isNot(value: AnyOperator) =
        chainProperty(Operation.NotEquals, value)

    infix fun notEq(value: AnyOperator) = isNot(value)

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

    fun chainProperty(
        operation: Operation,
        value: AnyOperator,
    ) = scalarOf(
        OperatorGroup.clause().chain(operation, this)
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
