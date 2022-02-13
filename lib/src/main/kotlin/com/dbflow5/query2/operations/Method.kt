package com.dbflow5.query2.operations

import com.dbflow5.query.NameAlias
import com.dbflow5.query.nameAlias
import com.dbflow5.sql.Query

/**
 * Methods represent executable SQL operations that may take in parameters
 */
interface Method<ReturnType> : OpStart<ReturnType>,
    OperatorGrouping<Query>,
    Query

inline fun <reified ReturnType> method(
    name: String,
    vararg arguments: AnyOperator,
): Method<ReturnType> =
    method(
        valueConverter = inferValueConverter(),
        name = name,
        arguments = arguments
    )

inline fun <reified ReturnType> emptyMethod(
    name: String,
): Method<ReturnType> =
    method(
        valueConverter = inferValueConverter(),
        name = name,
        arguments = arrayOf(scalarOf(""))
    )


fun <ReturnType> method(
    valueConverter: SQLValueConverter<ReturnType>,
    name: String,
    vararg arguments: AnyOperator
): Method<ReturnType> =
    MethodImpl(
        valueConverter = valueConverter,
        nameAlias = name.nameAlias,
        innerOperator = OperatorGroup.clause().chain(
            Operation.Comma,
            arguments.toList(),
        )
    )

internal data class MethodImpl<ReturnType>(
    override val valueConverter: SQLValueConverter<ReturnType>,
    override val nameAlias: NameAlias,
    private val innerOperator: OperatorGroup,
) : Method<ReturnType>,
    List<AnyOperator> by innerOperator.operations {
    override val operations: List<AnyOperator> = innerOperator.operations

    override val query: String by lazy { "${nameAlias.query}${innerOperator.query}" }

    override fun chain(operation: Operation, operator: AnyOperator): Method<ReturnType> =
        copy(
            innerOperator = innerOperator.chain(operation, operator),
        )

    override fun chain(
        operation: Operation,
        operators: Collection<AnyOperator>
    ): Method<ReturnType> =
        copy(
            innerOperator = innerOperator.chain(operation, operators)
        )

    override fun `as`(
        name: String,
        shouldAddIdentifierToAlias: Boolean
    ): Operator<ReturnType> =
        copy(
            nameAlias = nameAlias.newBuilder()
                .shouldAddIdentifierToAliasName(shouldAddIdentifierToAlias)
                .`as`(name)
                .build()
        ) as Operator<ReturnType> // TODO: nasty cast
}