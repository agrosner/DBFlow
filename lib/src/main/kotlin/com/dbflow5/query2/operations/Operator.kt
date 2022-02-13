package com.dbflow5.query2.operations

import com.dbflow5.query.NameAlias
import com.dbflow5.sql.Query

typealias AnyOperator = Operator<out Any?>

interface OperatorChain {
    /**
     * Appends the [operator] with [Operation.Or]
     */
    infix fun or(operator: AnyOperator): OperatorGrouping<Query> =
        chain(Operation.Or, operator)

    /**
     * Appends the [operator] with [Operation.And]
     */
    infix fun and(operator: AnyOperator): OperatorGrouping<Query> =
        chain(Operation.And, operator)

    /**
     * Chain a single operator with same [Operation]
     */
    fun chain(operation: Operation, operator: AnyOperator): OperatorGrouping<Query>

    /**
     * Chain operators with same [Operation]
     */
    fun chain(
        operation: Operation,
        vararg operators: AnyOperator
    ): OperatorGrouping<Query> =
        chain(operation, operators.toList())

    /**
     * Chain all operators with same [Operation]
     */
    fun chain(
        operation: Operation,
        operators: Collection<AnyOperator>
    ): OperatorGrouping<Query>

}

interface Operator<ValueType> : Query,
    OperatorChain {
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
 * Generates a [Operator] that concatenates this [IOperator] with the [ValueType] via "||"
 * by columnName=columnName || value
 *
 * @param value The value to concatenate.
 * @return A [<] that represents concatenation.
 */
infix fun Operator<String>.concatenate(value: String): ConcatStart =
    ConcatOperatorImpl(
        operations = listOf(this, literalOf(value)),
    )

/**
 * Represents a key, operation, + value type.
 *
 * (Property + operation + value) operation (property operation value)
 */
interface BaseOperator<ValueType> : Operator<ValueType> {

    val valueConverter: SQLValueConverter<ValueType>

    val nameAlias: NameAlias

    val key: String
        get() = nameAlias.query

    interface SingleValueOperator<ValueType> : BaseOperator<ValueType> {
        val value: ValueType

        val sqlValue: String
            get() = valueConverter.convert(value)
    }

    interface MultipleValueOperator<ValueType> : BaseOperator<ValueType> {
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

internal data class OperatorImpl<ValueType>(
    override val nameAlias: NameAlias,
    private val operation: Operation,
    override val value: ValueType,
    override val valueConverter: SQLValueConverter<ValueType>,
) : BaseOperator<ValueType>, BaseOperator.SingleValueOperator<ValueType> {
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
) : BaseOperator.BetweenStart<ValueType>,
    BaseOperator.BetweenComplete<ValueType> {
    override val query: String by lazy {
        buildString {
            append("${nameAlias.query} ${Operation.Between.value} ${sqlValue} ")
            if (secondValue != null) {
                append("${Operation.And} ${valueConverter.convert(secondValue)} ")
            }
            // TODO: post-arg
        }
    }

    override fun and(value: ValueType): BaseOperator.BetweenComplete<ValueType> =
        copy(
            secondValue = value,
        )
}

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