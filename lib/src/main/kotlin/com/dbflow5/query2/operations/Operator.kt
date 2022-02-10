package com.dbflow5.query2.operations

import com.dbflow5.query.NameAlias
import com.dbflow5.sql.Query

typealias AnyOperator = Operator<out Any?>

/**
 * Description:
 */
interface Operator<ValueType> : Query {

    val valueConverter: SQLValueConverter<ValueType>

    val nameAlias: NameAlias

    val key: String
        get() = nameAlias.query

    interface SingleValueOperator<ValueType> : Operator<ValueType> {
        val value: ValueType

        val sqlValue: String
            get() = valueConverter.convert(value)
    }

    interface MultipleValueOperator<ValueType> : Operator<ValueType> {
        val values: List<ValueType>
        val joinedSqlValue: String
            get() = values.joinToString(separator = ",") { valueConverter.convert(it) }
    }

    interface Between<ValueType> : SingleValueOperator<ValueType>

    interface BetweenStart<ValueType> : Between<ValueType> {
        infix fun and(value: ValueType): BetweenComplete<ValueType>
    }

    interface BetweenComplete<ValueType> : Between<ValueType>


    interface In<ValueType> : MultipleValueOperator<ValueType>
}

inline fun <reified ValueType> operator(
    nameAlias: NameAlias,
    operation: Operation = Operation.Empty,
    value: ValueType,
): Operator.SingleValueOperator<ValueType> = operator(
    nameAlias = nameAlias,
    value = value,
    operation = operation,
    valueConverter = inferValueConverter()
)

fun <ValueType> operator(
    nameAlias: NameAlias,
    operation: Operation = Operation.Empty,
    value: ValueType,
    valueConverter: SQLValueConverter<ValueType>,
): Operator.SingleValueOperator<ValueType> = OperatorImpl(
    nameAlias = nameAlias,
    value = value,
    operation = operation,
    valueConverter = valueConverter,
)

internal data class OperatorImpl<ValueType>(
    override val nameAlias: NameAlias,
    private val operation: Operation,
    override val value: ValueType,
    override val valueConverter: SQLValueConverter<ValueType>,
) : Operator<ValueType>, Operator.SingleValueOperator<ValueType> {
    override val query: String by lazy {
        buildString {
            append("${nameAlias.query} ${operation.value} $sqlValue")
        }
    }
}

internal data class BetweenImpl<ValueType>(
    override val valueConverter: SQLValueConverter<ValueType>,
    override val value: ValueType,
    /**
     * This will be fullfilled on completion.
     */
    private val secondValue: ValueType? = null,
    override val nameAlias: NameAlias,
) : Operator.BetweenStart<ValueType>,
    Operator.BetweenComplete<ValueType> {
    override val query: String by lazy {
        buildString {
            append("${nameAlias.query} ${Operation.Between.value} ${sqlValue} ")
            if (secondValue != null) {
                append("${Operation.And} ${valueConverter.convert(secondValue)} ")
            }
            // TODO: post-arg
        }
    }

    override fun and(value: ValueType): Operator.BetweenComplete<ValueType> =
        copy(
            secondValue = value,
        )
}

internal data class InImpl<ValueType>(
    override val valueConverter: SQLValueConverter<ValueType>,
    override val nameAlias: NameAlias,
    private val isIn: Boolean,
    override val values: List<ValueType>,
) : Operator.In<ValueType> {
    override val query: String by lazy {
        buildString {
            append("${nameAlias.query} ")
            append(if (isIn) Operation.In.value else Operation.NotIn.value)
            append("(${joinedSqlValue})")
        }
    }
}