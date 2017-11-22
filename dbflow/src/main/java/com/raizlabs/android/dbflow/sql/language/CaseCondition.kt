package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.raizlabs.android.dbflow.sql.language.BaseOperator.convertValueToString
import com.raizlabs.android.dbflow.sql.language.property.IProperty

/**
 * Description: Represents an individual condition inside a CASE.
 */
class CaseCondition<TReturn> : Query {

    private val caze: Case<TReturn>
    private val whenValue: TReturn?
    private val sqlOperator: SQLOperator?
    private var thenValue: TReturn? = null
    private val property: IProperty<*>?
    private var thenProperty: IProperty<*>? = null
    private var isThenPropertySet: Boolean = false

    override val query: String
        get() {
            val queryBuilder = QueryBuilder(" WHEN ")
            if (caze.isEfficientCase) {
                queryBuilder.append(convertValueToString(property ?: whenValue, false))
            } else {
                sqlOperator?.appendConditionToQuery(queryBuilder)
            }
            queryBuilder.append(" THEN ")
                    .append(convertValueToString(if (isThenPropertySet)
                        thenProperty
                    else
                        thenValue, false))
            return queryBuilder.query
        }

    internal constructor(caze: Case<TReturn>, sqlOperator: SQLOperator) {
        this.caze = caze
        this.sqlOperator = sqlOperator
        this.whenValue = null
        this.property = null
    }

    internal constructor(caze: Case<TReturn>, whenValue: TReturn) {
        this.caze = caze
        this.whenValue = whenValue
        this.sqlOperator = null
        this.property = null
    }

    internal constructor(caze: Case<TReturn>, property: IProperty<*>) {
        this.caze = caze
        this.property = property
        this.whenValue = null
        this.sqlOperator = null
    }

    /**
     * THEN part of this query, the value that gets set on column if condition is true.
     */
    fun then(value: TReturn?): Case<TReturn> = caze.apply { thenValue = value }

    fun then(value: IProperty<*>): Case<TReturn> = caze.apply {
        thenProperty = value

        // in case values are null in some sense.
        isThenPropertySet = true
    }

    override fun toString(): String = query
}
