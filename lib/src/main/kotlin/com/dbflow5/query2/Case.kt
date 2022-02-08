package com.dbflow5.query2

import com.dbflow5.query.BaseOperator
import com.dbflow5.query.Operator
import com.dbflow5.query.SQLOperator
import com.dbflow5.query.property.IProperty
import com.dbflow5.query.property.Property
import com.dbflow5.query.property.property
import com.dbflow5.quoteIfNeeded
import com.dbflow5.sql.Query

data class CaseCondition<T> internal constructor(
    val operator: SQLOperator? = null,
    val value: T? = null,
    /**
     * If true, value was used instead of Operator.
     */
    val valueSet: Boolean = false,
    val then: T?,
)

interface CaseWhenEnabled<T> {
    /**
     * Adds a WHEN clause.
     */
    fun whenever(
        operator: SQLOperator,
        then: T?
    ): CaseWithWhen<T>
}

interface CaseEfficientWhenEnabled<T> {
    /**
     * Starts an efficient CASE statement.
     * The [value] passed here is only evaluated once.
     */
    fun whenever(
        value: T?,
        then: T?
    ): CaseWithWhen<T>
}


interface CaseElseEnabled<T> {
    infix fun `else`(elseValue: T?): CaseWithElse<T>
}

interface CaseEndEnabled<T> {
    infix fun end(columnName: String): Property<CaseCompleted<T>>
    fun end(): Property<CaseCompleted<T>>

    fun endAsOperator(): Operator<T>
}

/**
 * Represents a SQLITE CASE argument.
 */
interface Case<T> : Query

/**
 * Description: Used in [Select] queries as a parameter.
 * Represents SQLITE CASE.
 */
interface CaseStart<T> : Case<T>,
    CaseWhenEnabled<T> {
    val column: IProperty<*>?
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
interface CaseCompleted<T> : Case<T>

/**
 * Efficient case method that skips to ELSE.
 */
fun <T> case(column: Property<T>): CaseEfficientWhenEnabled<T> = CaseImpl(
    column = column,
)

fun <T> case(): CaseStart<T> = CaseImpl()

internal data class CaseImpl<T>(
    override val column: IProperty<*>? = null,
    override val whenStatements: List<CaseCondition<T>> = listOf(),
    override val elseValue: T? = null,
    private val elseSpecified: Boolean = false,
    private val endColumnName: String? = null,
) : Query,
    CaseStart<T>,
    CaseWithWhen<T>,
    CaseEfficientWhenEnabled<T>,
    CaseWithElse<T>,
    CaseCompleted<T> {

    /**
     * If we use a column directly, we do not allow [whenever]
     * operations.
     */
    private val efficientCase = column != null

    override val query: String = buildString {
        append("CASE ")
        if (efficientCase) {
            append(
                "${
                    BaseOperator.convertValueToString(
                        column,
                        appendInnerQueryParenthesis = false
                    )
                } "
            )
        }
        append(whenStatements.joinToString(separator = " ") { condition ->
            buildString {
                append("WHEN ")
                if (efficientCase) {
                    append(
                        BaseOperator.convertValueToString(
                            condition.value,
                            appendInnerQueryParenthesis = false
                        )
                    )
                } else {
                    condition.operator?.appendConditionToQuery(this)
                }
                append(
                    " THEN ${
                        BaseOperator.convertValueToString(
                            condition.then,
                            appendInnerQueryParenthesis = false,
                        )
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

    override fun whenever(operator: SQLOperator, then: T?): CaseWithWhen<T> =
        copy(
            whenStatements = whenStatements.toMutableList().apply {
                add(CaseCondition(operator, then = then))
            }
        )

    override fun whenever(value: T?, then: T?): CaseWithWhen<T> =
        copy(
            whenStatements = whenStatements.toMutableList().apply {
                add(CaseCondition(value = value, valueSet = true, then = then))
            }
        )

    override fun `else`(elseValue: T?): CaseWithElse<T> =
        copy(
            elseValue = elseValue,
            elseSpecified = true,
        )

    override fun end(columnName: String): Property<CaseCompleted<T>> =
        copy(
            endColumnName = columnName.quoteIfNeeded(),
        ).property

    override fun end(): Property<CaseCompleted<T>> = property

    override fun endAsOperator(): Operator<T> =
        Operator.op(property.nameAlias)
}