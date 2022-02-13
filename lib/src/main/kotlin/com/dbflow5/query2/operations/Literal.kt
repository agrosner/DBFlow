package com.dbflow5.query2.operations

import com.dbflow5.query.NameAlias
import com.dbflow5.sql.Query

/**
 * Represents a literal value in a query.
 */
interface Literal<ValueType> :
    BaseOperator.SingleValueOperator<ValueType>,
    OpStart<ValueType>,
    Query {

    companion object {
        val All = literalOf("*")
        val WildCard = literalOf(Operation.WildCard.value)
    }
}

/**
 * Constructs a [Literal] value to be used in queries.
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified ValueType> literalOf(value: ValueType): Literal<ValueType> =
    literalOf(
        valueConverter = LiteralValueConverter as SQLValueConverter<ValueType>,
        value = value
    )

fun <ValueType> literalOf(
    valueConverter: SQLValueConverter<ValueType>,
    value: ValueType,
): Literal<ValueType> = LiteralImpl(
    valueConverter = valueConverter,
    value = value,
)

internal data class LiteralImpl<ValueType>(
    override val valueConverter: SQLValueConverter<ValueType>,
    override val value: ValueType,
    /**
     * Literal name (by default) is the raw representation of the value
     */
    override val nameAlias: NameAlias = NameAlias.rawBuilder(valueConverter.convert(value)).build(),
) : Literal<ValueType> {
    override val query: String by lazy { sqlValue }

    override fun `as`(
        name: String,
        shouldAddIdentifierToAlias: Boolean
    ): Operator<ValueType> =
        copy(
            nameAlias = nameAlias.newBuilder()
                .shouldAddIdentifierToAliasName(shouldAddIdentifierToAlias)
                .`as`(name)
                .build()
        )
}