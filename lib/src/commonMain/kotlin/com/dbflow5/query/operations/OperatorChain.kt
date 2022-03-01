package com.dbflow5.query.operations

import com.dbflow5.sql.Query

interface OperatorChain<Op : Operator<*>> {
    /**
     * Appends the [operator] with [Operation.Or]
     */
    infix fun or(operator: Op): OperatorGrouping<Query> =
        chain(Operation.Or, operator)

    /**
     * Appends the [operator] with [Operation.And]
     */
    infix fun and(operator: Op): OperatorGrouping<Query> =
        chain(Operation.And, operator)

    /**
     * Chain a single operator with same [Operation]
     */
    fun chain(operation: Operation, operator: Op): OperatorGrouping<Query>

    /**
     * Chain operators with same [Operation]
     */
    fun chain(
        operation: Operation,
        vararg operators: Op
    ): OperatorGrouping<Query> =
        chain(operation, operators.toList())

    /**
     * Chain all operators with same [Operation]
     */
    fun chain(
        operation: Operation,
        operators: Collection<Op>
    ): OperatorGrouping<Query>

}