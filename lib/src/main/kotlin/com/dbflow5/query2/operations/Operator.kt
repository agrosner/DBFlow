package com.dbflow5.query2.operations

import com.dbflow5.annotation.Collate
import com.dbflow5.query.NameAlias
import com.dbflow5.sql.Query

typealias AnyOperator = Operator<out Any?>

interface Operator<ValueType> : Query,
    OperatorChain<AnyOperator> {
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

/**
 * Generates a [Operator] that concatenates this [Operator] with the [value] via "||"
 * by columnName=columnName || value
 *
 * @param value The value to concatenate.
 * @return A [<] that represents concatenation.
 */
infix fun Operator<String>.concatenate(value: String): ConcatStart =
    ConcatOperatorImpl(
        operations = listOf(this, sqlLiteralOf(value)),
    )


infix fun <ValueType> Operator<ValueType>.collate(collate: Collate) =
    literalOf(query + " ${collate.query}")

/**
 * Represents a key, operation, + value type.
 *
 * (Property + operation + value) operation (property operation value)
 */
interface BaseOperator<ValueType> : Operator<ValueType> {

    val nameAlias: NameAlias

    val key: String
        get() = nameAlias.query

    infix operator fun plus(value: AnyOperator) =
        chain(Operation.Plus, value)

    infix operator fun minus(value: AnyOperator) =
        chain(Operation.Minus, value)

    infix operator fun div(value: AnyOperator) =
        chain(Operation.Division, value)

    infix operator fun times(value: AnyOperator) =
        chain(Operation.Times, value)

    infix operator fun rem(value: AnyOperator) =
        chain(Operation.Rem, value)

    interface ValuelessOperator : BaseOperator<Nothing>

    interface SingleValueOperator<ValueType> : BaseOperator<ValueType> {
        val valueConverter: SQLValueConverter<ValueType>
        val value: ValueType
        val sqlValue: String
            get() = valueConverter.convert(value)
    }

    interface MultipleValueOperator<ValueType> : BaseOperator<ValueType> {
        val valueConverter: SQLValueConverter<ValueType>
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
): BaseOperator.SingleValueOperator<ValueType> = operator(
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
): BaseOperator.SingleValueOperator<ValueType> = OperatorImpl(
    nameAlias = nameAlias,
    value = value,
    operation = operation,
    valueConverter = valueConverter,
)

/**
 * Creates an operator without a value.
 */
fun operator(
    nameAlias: NameAlias,
    operation: Operation = Operation.Empty,
): BaseOperator.ValuelessOperator = ValuelessOperatorImpl(
    nameAlias = nameAlias,
    operation = operation,
)

internal data class OperatorImpl<ValueType>(
    override val nameAlias: NameAlias,
    private val operation: Operation,
    override val value: ValueType,
    override val valueConverter: SQLValueConverter<ValueType>,
) : BaseOperator<ValueType>, BaseOperator.SingleValueOperator<ValueType> {
    override val query: String by lazy {
        buildString {
            append(nameAlias.query)
            if (operation != Operation.Empty) append(" ${operation.value}")
            append(" $sqlValue")
        }
    }
}

internal data class ValuelessOperatorImpl(
    override val nameAlias: NameAlias,
    private val operation: Operation,
) : BaseOperator.ValuelessOperator {
    override val query: String by lazy {
        buildString {
            append(nameAlias.query)
            if (operation != Operation.Empty) append(" ${operation.value}")
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
    /**
     * used for arguments like [Collate].
     */
    private val postArg: String? = null,
) : BaseOperator.BetweenStart<ValueType>,
    BaseOperator.BetweenComplete<ValueType> {
    override val query: String by lazy {
        buildString {
            append("${nameAlias.query} ${Operation.Between.value} ${sqlValue} ")
            if (secondValue != null) {
                append("${Operation.And.value} ${valueConverter.convert(secondValue)} ")
            }
            if (postArg != null) {
                append(" $postArg")
            }
        }
    }

    override fun and(value: ValueType): BaseOperator.BetweenComplete<ValueType> =
        copy(
            secondValue = value,
        )
}

inline fun <reified ValueType> inOp(
    isIn: Boolean = true,
    nameAlias: NameAlias,
    firstValue: ValueType,
    vararg values: ValueType,
): BaseOperator.In<ValueType> = inOp(
    isIn = isIn,
    nameAlias = nameAlias,
    firstValue = firstValue,
    valueConverter = inferValueConverter(),
    values = values,
)


fun <ValueType> inOp(
    isIn: Boolean = true,
    nameAlias: NameAlias,
    firstValue: ValueType,
    valueConverter: SQLValueConverter<ValueType>,
    vararg values: ValueType,
): BaseOperator.In<ValueType> = InImpl(
    nameAlias = nameAlias,
    valueConverter = valueConverter,
    values = mutableListOf(firstValue).apply { addAll(values) },
    isIn = isIn,
)

internal data class InImpl<ValueType>(
    override val valueConverter: SQLValueConverter<ValueType>,
    override val nameAlias: NameAlias,
    private val isIn: Boolean,
    override val values: List<ValueType>,
) : BaseOperator.In<ValueType> {
    override val query: String by lazy {
        buildString {
            append("${nameAlias.query} ")
            append(if (isIn) Operation.In.value else Operation.NotIn.value)
            append("(${joinedSqlValue})")
        }
    }
}