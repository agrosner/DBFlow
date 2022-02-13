package com.dbflow5.query2.operations

import com.dbflow5.query.NameAlias
import com.dbflow5.query2.Aliasable

/**
 * Special operator that treats all operations as string concatenations.
 */
interface ConcatOperator : Operator<String>

interface ConcatStart : ConcatOperator, Aliasable<ConcatWithAlias> {
    infix fun concatenate(operator: Operator<String>): ConcatStart
}

interface ConcatWithAlias : ConcatOperator {
    val nameAlias: NameAlias
}

internal data class ConcatOperatorImpl(
    private val operations: List<Operator<String>> = listOf(),
    private val aliasName: String? = null,
    private val shouldAddIdentifierToAlias: Boolean = false,
) : ConcatOperator,
    ConcatStart,
    ConcatWithAlias {
    override fun concatenate(operator: Operator<String>): ConcatStart =
        copy(
            operations = operations.toMutableList().apply { add(operator) }
        )

    override val nameAlias: NameAlias = NameAlias.joinNames(
        Operation.Concatenate,
        *operations.map { it.query }.toTypedArray()
    ).newBuilder()
        .shouldAddIdentifierToAliasName(shouldAddIdentifierToAlias)
        .`as`(aliasName)
        .build()

    override val query: String by lazy { nameAlias.fullQuery }

    override fun `as`(name: String, shouldAddIdentifierToAlias: Boolean): ConcatWithAlias =
        copy(
            aliasName = name,
            shouldAddIdentifierToAlias = shouldAddIdentifierToAlias,
        )
}