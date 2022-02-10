package com.dbflow5.query2.operations

import com.dbflow5.query.NameAlias
import com.dbflow5.query.nameAlias
import com.dbflow5.sql.Query

interface OperatorGroup : Query, List<AnyOperator>, Operator<Query> {

    val operations: List<AnyOperator>

    override val valueConverter: SQLValueConverter<Query>
        get() = QueryConverter

    /**
     * Appends the [operator] with [Operation.Or]
     */
    infix fun or(operator: AnyOperator): OperatorGroup =
        chain(Operation.Or, operator)

    /**
     * Appends the [operator] with [Operation.And]
     */
    infix fun and(operator: AnyOperator): OperatorGroup =
        chain(Operation.And, operator)

    /**
     * Chain a single operator with same [Operation]
     */
    fun chain(operation: Operation, operator: AnyOperator): OperatorGroup

    /**
     * Chain operators with same [Operation]
     */
    fun chain(operation: Operation, vararg operators: AnyOperator): OperatorGroup =
        chain(operation, operators.toList())

    /**
     * Chain all operators with same [Operation]
     */
    fun chain(operation: Operation, operators: Collection<AnyOperator>): OperatorGroup

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
                append(operation.value + " ")
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
    override val operations: List<AnyOperator> = opPairings.map(
        transform = { it.operator }
    )
) : OperatorGroup, List<AnyOperator> by operations {
    override val nameAlias: NameAlias = "".nameAlias

    override val query: String by lazy {
        buildString {
            // no operations, short-cut empty
            if (opPairings.isEmpty()) return@buildString
            if (useParenthesis) append("(")
            append(opPairings.joinToString { it.query })
            if (useParenthesis) append(")")
        }
    }

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