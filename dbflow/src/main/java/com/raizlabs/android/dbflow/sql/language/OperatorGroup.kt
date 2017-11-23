package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.language.Operator.Operation
import java.util.*

/**
 * Allows combining of [SQLOperator] into one condition.
 */
class OperatorGroup
@JvmOverloads
constructor(columnName: NameAlias? = null) : BaseOperator(columnName), Query, Iterable<SQLOperator> {

    private val conditionsList = ArrayList<SQLOperator>()

    private var internalQuery: String? = null
    private var isChanged: Boolean = false
    private var allCommaSeparated: Boolean = false
    private var useParenthesis = true

    val conditions: List<SQLOperator>
        get() = conditionsList

    private val querySafe: String
        get() = appendToQuery()

    init {

        // default is AND
        separator = Operation.AND
    }

    /**
     * Will ignore all separators for the group and make them separated by comma. This is useful
     * in [Set] statements.
     *
     * @param allCommaSeparated All become comma separated.
     * @return This instance.
     */
    fun setAllCommaSeparated(allCommaSeparated: Boolean) = apply {
        this.allCommaSeparated = allCommaSeparated
        isChanged = true
    }

    /**
     * Sets whether we use paranthesis when grouping this within other [SQLOperator]. The default
     * is true, but if no conditions exist there are no paranthesis anyways.
     *
     * @param useParenthesis true if we use them, false if not.
     */
    fun setUseParenthesis(useParenthesis: Boolean) = apply {
        this.useParenthesis = useParenthesis
        isChanged = true
    }

    /**
     * Appends the [SQLOperator] with an [Operation.OR]
     *
     * @param sqlOperator The condition to append.
     * @return This instance.
     */
    fun or(sqlOperator: SQLOperator): OperatorGroup = operator(Operation.OR, sqlOperator)

    /**
     * Appends the [SQLOperator] with an [Operation.AND]
     */
    fun and(sqlOperator: SQLOperator): OperatorGroup = operator(Operation.AND, sqlOperator)

    /**
     * Applies the [Operation.AND] to all of the passed
     * [SQLOperator].
     */
    fun andAll(vararg sqlOperators: SQLOperator) = apply {
        sqlOperators.forEach { and(it) }
    }

    /**
     * Applies the [Operation.AND] to all of the passed
     * [SQLOperator].
     */
    fun andAll(sqlOperators: Collection<SQLOperator>) = apply {
        sqlOperators.forEach { and(it) }
    }

    /**
     * Applies the [Operation.AND] to all of the passed
     * [SQLOperator].
     */
    fun orAll(vararg sqlOperators: SQLOperator) = apply {
        sqlOperators.forEach { or(it) }
    }

    /**
     * Applies the [Operation.AND] to all of the passed
     * [SQLOperator].
     */
    fun orAll(sqlOperators: Collection<SQLOperator>) = apply {
        sqlOperators.forEach { or(it) }
    }

    /**
     * Appends the [SQLOperator] with the specified operator string.
     */
    private fun operator(operator: String, sqlOperator: SQLOperator?) = apply {
        if (sqlOperator != null) {
            setPreviousSeparator(operator)
            conditionsList.add(sqlOperator)
            isChanged = true
        }
    }

    override fun appendConditionToQuery(queryBuilder: StringBuilder) {
        val conditionListSize = conditionsList.size
        if (useParenthesis && conditionListSize > 0) {
            queryBuilder.append("(")
        }
        for (i in 0 until conditionListSize) {
            val condition = conditionsList[i]
            condition.appendConditionToQuery(queryBuilder)
            if (!allCommaSeparated && condition.hasSeparator() && i < conditionListSize - 1) {
                queryBuilder.append(" ${condition.separator()} ")
            } else if (i < conditionListSize - 1) {
                queryBuilder.append(", ")
            }
        }
        if (useParenthesis && conditionListSize > 0) {
            queryBuilder.append(")")
        }
    }

    /**
     * Sets the last condition to use the separator specified
     *
     * @param separator AND, OR, etc.
     */
    private fun setPreviousSeparator(separator: String) {
        if (conditionsList.size > 0) {
            // set previous to use OR separator
            conditionsList[conditionsList.size - 1].separator(separator)
        }
    }

    override val query: String
        get() {
            if (isChanged) {
                internalQuery = querySafe
            }
            return internalQuery ?: ""
        }

    override fun toString(): String = querySafe

    val size: Int
        get() = conditionsList.size

    override fun iterator(): Iterator<SQLOperator> = conditionsList.iterator()

    companion object {

        /**
         * @return Starts an arbitrary clause of conditions to use.
         */
        @JvmStatic
        fun clause(): OperatorGroup = OperatorGroup()

        /**
         * @return Starts an arbitrary clause of conditions to use with first param as conditions separated by AND.
         */
        @JvmStatic
        fun clause(vararg condition: SQLOperator): OperatorGroup =
                OperatorGroup().andAll(*condition)

        /**
         * @return Starts an arbitrary clause of conditions to use, that when included in other [SQLOperator],
         * does not append parenthesis to group it.
         */
        @JvmStatic
        fun nonGroupingClause(): OperatorGroup = OperatorGroup().setUseParenthesis(false)

        /**
         * @return Starts an arbitrary clause of conditions (without parenthesis) to use with first param as conditions separated by AND.
         */
        @JvmStatic
        fun nonGroupingClause(vararg condition: SQLOperator): OperatorGroup =
                OperatorGroup().setUseParenthesis(false).andAll(*condition)
    }
}

fun SQLOperator.clause() = OperatorGroup.clause(this)

infix fun OperatorGroup.and(sqlOperator: SQLOperator) = and(sqlOperator)

infix fun OperatorGroup.or(sqlOperator: SQLOperator) = or(sqlOperator)

infix fun OperatorGroup.and(sqlOperator: OperatorGroup) = clause().and(sqlOperator)

infix fun OperatorGroup.or(sqlOperator: OperatorGroup) = clause().or(sqlOperator)