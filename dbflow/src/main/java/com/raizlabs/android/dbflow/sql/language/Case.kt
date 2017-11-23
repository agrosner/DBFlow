package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.raizlabs.android.dbflow.sql.language.property.IProperty
import com.raizlabs.android.dbflow.sql.language.property.Property

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
            val queryBuilder = QueryBuilder(" CASE")
            if (isEfficientCase) {
                queryBuilder.append(" " + BaseOperator.convertValueToString(caseColumn, false)!!)
            }

            queryBuilder.append(QueryBuilder.join("", caseConditions))

            if (elseSpecified) {
                queryBuilder.append(" ELSE ").append(BaseOperator.convertValueToString(elseValue, false))
            }
            if (endSpecified) {
                queryBuilder.append(" END " + if (columnName != null) columnName else "")
            }
            return queryBuilder.query
        }

    internal constructor() : this(null)

    init {
        if (caseColumn != null) {
            isEfficientCase = true
        }
    }

    fun `when`(sqlOperator: SQLOperator): CaseCondition<TReturn> {
        if (isEfficientCase) {
            throw IllegalStateException("When using the efficient CASE method," + "you must pass in value only, not condition.")
        }
        val caseCondition = CaseCondition(this, sqlOperator)
        caseConditions.add(caseCondition)
        return caseCondition
    }

    fun `when`(whenValue: TReturn?): CaseCondition<TReturn> {
        if (!isEfficientCase) {
            throw IllegalStateException("When not using the efficient CASE method, " + "you must pass in the SQLOperator as a parameter")
        }
        val caseCondition = CaseCondition(this, whenValue)
        caseConditions.add(caseCondition)
        return caseCondition
    }

    fun `when`(property: IProperty<*>): CaseCondition<TReturn> {
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
    fun _else(elseValue: TReturn?) = apply {
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
            this.columnName = QueryBuilder.quoteIfNeeded(columnName)
        }
        return Property(null, NameAlias.rawBuilder(query)
                .build())
    }

    /**
     * @return The case complete as an operator.
     */
    fun <T : Any> endAsOperator(): Operator<T> = Operator.op(end().nameAlias)
}
/**
 * @return The case completed as a property.
 */
