package com.dbflow5.query2.operations

import com.dbflow5.query.NameAlias
import com.dbflow5.sql.Query

/**
 * Represents a literal value in a query.
 */
interface Scalar<ValueType> :
    BaseOperator.SingleValueOperator<ValueType>,
    OpStart<ValueType>,
    Query {

    companion object {
        val All = scalarOf("*")
        val WildCard = scalarOf(Operation.WildCard.value)
    }
}

/**
 * Constructs a [Scalar] value to be used in queries.
 */
inline fun <reified ValueType> scalarOf(value: ValueType) =
    scalarOf(
        valueConverter = inferValueConverter(),
        value = value
    )

fun <ValueType> scalarOf(
    valueConverter: SQLValueConverter<ValueType>,
    value: ValueType,
): Scalar<ValueType> = ScalarImpl(
    valueConverter = valueConverter,
    value = value,
)

internal data class ScalarImpl<ValueType>(
    override val valueConverter: SQLValueConverter<ValueType>,
    override val value: ValueType,
    /**
     * Scalars name (by default) is the raw representation of the value
     */
    override val nameAlias: NameAlias = NameAlias.rawBuilder(valueConverter.convert(value)).build(),
) : Scalar<ValueType> {
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