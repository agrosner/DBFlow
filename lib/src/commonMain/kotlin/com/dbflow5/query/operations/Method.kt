package com.dbflow5.query.operations

import com.dbflow5.query.NameAlias
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
        arguments = arrayOf(literalOf(""))
    )


fun <ReturnType> method(
    valueConverter: SQLValueConverter<ReturnType>,
    name: String,
    vararg arguments: AnyOperator
): Method<ReturnType> =
    MethodImpl(
        valueConverter = valueConverter,
        name = name,
        innerOperator = OperatorGroup.nonGroupingClause().chain(
            Operation.Comma,
            arguments.toList(),
        )
    )

internal data class MethodImpl<ReturnType>(
    override val valueConverter: SQLValueConverter<ReturnType>,
    private val name: String,
    private val innerOperator: OperatorGroup,
    private val shouldAddIdentifierToAlias: Boolean = false,
) : Method<ReturnType>,
    List<AnyOperator> by innerOperator.operations {

    override val operations: List<AnyOperator> = innerOperator.operations

    override val query: String by lazy { nameAlias.fullQuery }

    /**
     * NameAlias of a method is its entire name plus arguments.
     */
    override val nameAlias: NameAlias = NameAlias.Builder("$name(${innerOperator.query})")
        .shouldAddIdentifierToName(false)
        .shouldStripIdentifier(false)
        .shouldAddIdentifierToAliasName(shouldAddIdentifierToAlias)
        .build()

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

    @Suppress("UNCHECKED_CAST")
    override fun `as`(
        name: String,
        shouldAddIdentifierToAlias: Boolean
    ): Operator<ReturnType> =
        copy(
            shouldAddIdentifierToAlias = shouldAddIdentifierToAlias,
            name = name,
        ) as Operator<ReturnType> // TODO: nasty cast
}