package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.query.property.IProperty
import com.raizlabs.dbflow5.query.property.Property
import com.raizlabs.dbflow5.quoteIfNeeded
import com.raizlabs.dbflow5.sql.Query

/**
 * Description: Represents a SQLITE CASE argument.
 */
class Case<TReturn>(private val caseColumn: IProperty<*>? = null) : Query {

    private val caseConditions = arrayListOf<CaseCondition<TReturn>>()
    private var columnName: String? = null
    private var elseValue: TReturn? = null
    private var elseSpecified = false

    // when true, only WHEN value is supported. Not WHEN condition
    var isEfficientCase = false
        private set

    private var endSpecified = false

    override val query: String
        get() {
            val queryBuilder = StringBuilder(" CASE")
            if (isEfficientCase) {
                queryBuilder.append(" ${BaseOperator.convertValueToString(caseColumn, false)}")
            }

            queryBuilder.append(caseConditions.joinToString(separator = ""))

            if (elseSpecified) {
                queryBuilder.append(" ELSE ")
                    .append(BaseOperator.convertValueToString(elseValue, false))
            }
            if (endSpecified) {
                queryBuilder.append(" END ${if (columnName != null) columnName else ""}")
            }
            return queryBuilder.toString()
        }

    internal constructor() : this(null)

    init {
        if (caseColumn != null) {
            isEfficientCase = true
        }
    }

    @JvmName("when")
    infix fun whenever(sqlOperator: SQLOperator): CaseCondition<TReturn> {
        if (isEfficientCase) {
            throw IllegalStateException("When using the efficient CASE method," + "you must pass in value only, not condition.")
        }
        val caseCondition = CaseCondition(this, sqlOperator)
        caseConditions.add(caseCondition)
        return caseCondition
    }

    @JvmName("when")
    infix fun whenever(whenValue: TReturn?): CaseCondition<TReturn> {
        if (!isEfficientCase) {
            throw IllegalStateException("When not using the efficient CASE method, " + "you must pass in the SQLOperator as a parameter")
        }
        val caseCondition = CaseCondition(this, whenValue)
        caseConditions.add(caseCondition)
        return caseCondition
    }

    @JvmName("when")
    infix fun whenever(property: IProperty<*>): CaseCondition<TReturn> {
        if (!isEfficientCase) {
            throw IllegalStateException("When not using the efficient CASE method, " + "you must pass in the SQLOperator as a parameter")
        }
        val caseCondition = CaseCondition(this, property)
        caseConditions.add(caseCondition)
        return caseCondition
    }

    /**
     * Default case here. If not specified, value will be NULL.
     */
    @JvmName("_else")
    infix fun `else`(elseValue: TReturn?) = apply {
        this.elseValue = elseValue
        elseSpecified = true // ensure its set especially if null specified.
    }

    /**
     * @param columnName The name of the case that we return in a column.
     * @return The case completed as a property.
     */
    @JvmOverloads
    fun end(columnName: String? = null): Property<Case<TReturn>> {
        endSpecified = true
        if (columnName != null) {
            this.columnName = columnName.quoteIfNeeded()
        }
        return Property(null, NameAlias.rawBuilder(query).build())
    }

    /**
     * @return The case complete as an operator.
     */
    fun <T : Any> endAsOperator(): Operator<T> = Operator.op(end().nameAlias)
}

infix fun <T : Any> Case<T>.end(columnName: String) = end(columnName)
