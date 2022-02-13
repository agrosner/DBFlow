package com.dbflow5.query2

import com.dbflow5.query.BaseOperator
import com.dbflow5.query2.operations.Operator
import com.dbflow5.query2.operations.Property
import com.dbflow5.query2.operations.SQLValueConverter
import com.dbflow5.query2.operations.inferValueConverter
import com.dbflow5.query2.operations.nullableConverter
import com.dbflow5.quoteIfNeeded
import com.dbflow5.sql.Query

data class CaseCondition<ValueType> internal constructor(
    val operator: Operator<ValueType>? = null,
    val value: ValueType? = null,
    /**
     * If true, value was used instead of Operator.
     */
    val valueSet: Boolean = false,
    val then: ValueType?,
)

interface CaseWhenEnabled<ThenValueType> {
    /**
     * Adds a WHEN clause.
     */
    fun whenever(
        operator: Operator<ThenValueType>,
        then: ThenValueType?
    ): CaseWithWhen<ThenValueType>
}

interface CaseEfficientWhenEnabled<ValueType> {
    /**
     * Starts an efficient CASE statement.
     * The [value] passed here is only evaluated once.
     */
    fun whenever(
        value: ValueType,
        then: ValueType?
    ): CaseWithWhen<ValueType>
}


interface CaseElseEnabled<ValueType> {
    infix fun `else`(elseValue: ValueType?): CaseWithElse<ValueType>
}

interface CaseEndEnabled<ValueType> {
    infix fun end(columnName: String): CaseCompleted<ValueType>
    fun end(): CaseCompleted<ValueType>
}

/**
 * Represents a SQLITE CASE argument.
 */
interface Case<ValueType> : Query

/**
 * Description: Used in [Select] queries as a parameter.
 * Represents SQLITE CASE.
 */
interface CaseStart<ValueType> : Case<ValueType>,
    CaseWhenEnabled<ValueType> {
    val column: Property<ValueType, *>?
}

interface CaseWithWhen<T> :
    Case<T>,
    CaseWhenEnabled<T>,
    CaseElseEnabled<T>,
    CaseEndEnabled<T> {
    val whenStatements: List<CaseCondition<T>>
}

interface CaseWithElse<T> :
    Case<T>,
    CaseEndEnabled<T> {
    val elseValue: T?
}

/**
 * Represents a completed case since they have an "end" operator.
 */
interface CaseCompleted<ValueType> : Case<ValueType>,
    Operator<ValueType>

/**
 * Efficient case method that skips to ELSE.
 */
inline fun <reified ValueType> case(column: Property<ValueType, *>) =
    case(
        column = column,
        valueConverter = inferValueConverter()
    )

/**
 * Efficient case method that skips to ELSE.
 */
fun <ValueType> case(
    valueConverter: SQLValueConverter<ValueType>,
    column: Property<ValueType, *>
): CaseEfficientWhenEnabled<ValueType> =
    CaseImpl(
        valueConverter = valueConverter,
        column = column,
    )

inline fun <reified ValueType> case(): CaseStart<ValueType> = case(
    valueConverter = inferValueConverter()
)

fun <ValueType> case(valueConverter: SQLValueConverter<ValueType>): CaseStart<ValueType> = CaseImpl(
    valueConverter = valueConverter,
)

internal data class CaseImpl<ValueType>(
    val valueConverter: SQLValueConverter<ValueType>,
    override val column: Property<ValueType, *>? = null,
    override val whenStatements: List<CaseCondition<ValueType>> = listOf(),
    override val elseValue: ValueType? = null,
    private val elseSpecified: Boolean = false,
    private val endColumnName: String? = null,
) : Query,
    CaseStart<ValueType>,
    CaseWithWhen<ValueType>,
    CaseEfficientWhenEnabled<ValueType>,
    CaseWithElse<ValueType>,
    CaseCompleted<ValueType> {

    /**
     * If we use a column directly, we do not allow [whenever]
     * operations.
     */
    private val efficientCase = column != null

    override val query: String = buildString {
        append("CASE ")
        if (efficientCase && column != null) {
            append("${column.query} ")
        }
        append(whenStatements.joinToString(separator = " ") { condition ->
            buildString {
                append("WHEN ")
                if (efficientCase) {
                    append(valueConverter.nullableConverter().convert(condition.value))
                } else {
                    condition.operator?.let { append(it.query) }
                }
                append(
                    " THEN ${
                        valueConverter.nullableConverter().convert(condition.then)
                    }"
                )
            }
        })

        if (elseSpecified) {
            append(
                " ELSE ${
                    BaseOperator.convertValueToString(
                        elseValue,
                        appendInnerQueryParenthesis = false
                    )
                }"
            )
        }
        append(" END ${endColumnName ?: ""}")
    }

    override fun whenever(
        operator: Operator<ValueType>,
        then: ValueType?
    ): CaseWithWhen<ValueType> =
        copy(
            whenStatements = whenStatements.toMutableList().apply {
                add(CaseCondition(operator, then = then))
            }
        )

    override fun whenever(value: ValueType, then: ValueType?): CaseWithWhen<ValueType> =
        copy(
            whenStatements = whenStatements.toMutableList().apply {
                add(CaseCondition(value = value, valueSet = true, then = then))
            }
        )

    override fun `else`(elseValue: ValueType?): CaseWithElse<ValueType> =
        copy(
            elseValue = elseValue,
            elseSpecified = true,
        )

    override fun end(columnName: String): CaseCompleted<ValueType> =
        copy(
            endColumnName = columnName.quoteIfNeeded(),
        )

    override fun end(): CaseCompleted<ValueType> = this

}