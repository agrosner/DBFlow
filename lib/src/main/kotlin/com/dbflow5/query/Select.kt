package com.dbflow5.query

import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.query.property.IProperty
import com.dbflow5.query.property.Property
import com.dbflow5.sql.Query
import com.dbflow5.sql.QueryCloneable

private inline class QualifierType(val value: String)

private val Distinct = QualifierType("DISTINCT")
private val All = QualifierType("ALL")
private val None = QualifierType("NONE")

/**
 * Description: A SQL SELECT statement generator. It generates the SELECT part of the statement.
 */
class Select
/**
 * Creates this instance with the specified columns from the specified [com.dbflow5.config.FlowManager]
 *
 * @param properties The properties to select from.
 */
internal constructor(vararg properties: IProperty<*>) : Query, QueryCloneable<Select> {

    /**
     * The select qualifier to append to the SELECT statement
     */
    private var selectQualifier: QualifierType = None

    private val propertyList = arrayListOf<IProperty<*>>()

    override val query: String
        get() {
            val queryBuilder = StringBuilder("SELECT ")
            when (selectQualifier) {
                Distinct -> queryBuilder.append("DISTINCT ")
                All -> queryBuilder.append("ALL ")
            }

            queryBuilder.append(propertyList.joinToString(separator = ","))
            queryBuilder.append(" ")
            return queryBuilder.toString()
        }

    init {
        propertyList.addAll(properties.toList())
        if (propertyList.isEmpty()) {
            propertyList.add(Property.ALL_PROPERTY)
        }
    }

    /**
     * Passes this statement to the [From]
     *
     * @param table    The model table to run this query on
     * @param [T] The class that implements [com.dbflow5.structure.Model]
     * @return the From part of this query
     */
    infix fun <T : Any> from(adapter: SQLObjectAdapter<T>): From<T> = From(this, adapter)

    /**
     * Constructs a [From] with a [ModelQueriable] expression.
     */
    fun <T : Any> from(modelQueriable: ModelQueriable<T>) =
        From(this, modelQueriable.adapter as SQLObjectAdapter<T>, modelQueriable)

    /**
     * appends [.DISTINCT] to the query
     *
     * @return
     */
    fun distinct(): Select = selectQualifier(Distinct)

    override fun toString(): String = query

    override fun cloneSelf(): Select = Select(*propertyList.toTypedArray())

    /**
     * Helper method to pick the correct qualifier for a SELECT query
     *
     * @param qualifierInt Can be [.ALL], [.NONE], or [.DISTINCT]
     * @return
     */
    private fun selectQualifier(qualifier: QualifierType) = apply {
        selectQualifier = qualifier
    }
}

inline val select: Select
    get() = select()
