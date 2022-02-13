package com.dbflow5.query2.operations

import com.dbflow5.sql.Query

/**
 * This operator uses a selection string and args to form a query.
 * Not recommended for normal queries, but can be used as a convenience.
 */
data class UnSafeStringOperator(
    private val selection: String,
    private val selectionArgs: List<String>,
) : Operator<String> {
    override val query: String by lazy {
        selectionArgs.fold(selection) { selection, arg ->
            selection.replaceFirst("\\?".toRegex(), arg)
        }
    }

    override fun chain(operation: Operation, operator: AnyOperator): OperatorGrouping<Query> =
        OperatorGroup.clause()
            .chain(Operation.Empty, this)
            .chain(operation, operator)

    override fun chain(
        operation: Operation,
        operators: Collection<AnyOperator>
    ): OperatorGrouping<Query> =
        OperatorGroup.clause()
            .chain(Operation.Empty, this)
            .chain(operation, operators)
}