package com.dbflow5.query2.operations

import com.dbflow5.sql.Query

interface OperatorGrouping<ValueType> : Query, List<AnyOperator>,
    Operator<ValueType> {

    val operations: List<AnyOperator>
}

interface OperatorGroup : OperatorGrouping<Query> {
    override fun chain(operation: Operation, operator: AnyOperator): OperatorGroup

    override fun chain(
        operation: Operation,
        operators: Collection<AnyOperator>
    ): OperatorGroup

    override fun chain(
        operation: Operation,
        vararg operators: AnyOperator
    ): OperatorGroup = super.chain(operation, *operators) as OperatorGroup

    companion object {
        fun nonGroupingClause(): OperatorGroup =
            OperatorGroupImpl(
                useParenthesis = false,
            )

        fun clause(): OperatorGroup = OperatorGroupImpl()
    }
}

/**
 * Holds current and previous operation.
 */
internal data class OpPairing(
    val operation: Operation = Operation.Empty,
    val operator: AnyOperator
) : Query {
    override val query: String by lazy {
        buildString {
            if (operation != Operation.Empty) {
                append("${operation.value} ")
            }
            append(operator.query)
        }
    }
}

/**
 * Description: Holds a grouping of [Operator]
 */
internal data class OperatorGroupImpl(
    private val opPairings: List<OpPairing> = listOf(),
    /**
     * Default true, wraps the [query] in parenthesis.
     */
    private val useParenthesis: Boolean = true,
) : OperatorGroup, List<AnyOperator> by opPairings.map(transform = {
    it.operator
}) {
    override val query: String by lazy {
        buildString {
            // no operations, short-cut empty
            if (opPairings.isEmpty()) return@buildString
            if (useParenthesis) append("(")
            append(opPairings.joinToString(separator = "") { it.query })
            if (useParenthesis) append(")")
        }
    }

    override val operations: List<AnyOperator> = opPairings.map { it.operator }

    override fun chain(operation: Operation, operator: AnyOperator): OperatorGroup =
        copy(
            opPairings = opPairings.toMutableList().apply {
                // if the group is empty, don't append an operation at beginning.
                add(OpPairing(if (isEmpty()) Operation.Empty else operation, operator))
            }
        )

    override fun chain(operation: Operation, operators: Collection<AnyOperator>): OperatorGroup =
        copy(
            opPairings = opPairings.toMutableList().apply {
                addAll(operators.mapIndexed { index, operator ->
                    // if the group is empty, don't append an operation at beginning.
                    if (this@OperatorGroupImpl.opPairings.isEmpty()
                        && index == 0
                    ) {
                        OpPairing(Operation.Empty, operator)
                    } else {
                        OpPairing(operation, operator)
                    }
                })
            }
        )
}