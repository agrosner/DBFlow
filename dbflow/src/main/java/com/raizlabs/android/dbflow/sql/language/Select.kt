package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.raizlabs.android.dbflow.sql.language.property.IProperty
import com.raizlabs.android.dbflow.sql.language.property.Property
import java.util.*

/**
 * Description: A SQL SELECT statement generator. It generates the SELECT part of the statement.
 */
class Select
/**
 * Creates this instance with the specified columns from the specified [com.raizlabs.android.dbflow.config.FlowManager]
 *
 * @param properties The properties to select from.
 */
(vararg properties: IProperty<*>) : Query {
    /**
     * The select qualifier to append to the SELECT statement
     */
    private var selectQualifier = NONE

    private val propertyList = ArrayList<IProperty<*>>()

    override val query: String
        get() {
            val queryBuilder = QueryBuilder("SELECT ")

            if (selectQualifier != NONE) {
                if (selectQualifier == DISTINCT) {
                    queryBuilder.append("DISTINCT")
                } else if (selectQualifier == ALL) {
                    queryBuilder.append("ALL")
                }
                queryBuilder.appendSpace()
            }

            queryBuilder.append(QueryBuilder.join(",", propertyList))
            queryBuilder.appendSpace()
            return queryBuilder.query
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
     * @param <TModel> The class that implements [com.raizlabs.android.dbflow.structure.Model]
     * @return the From part of this query
    </TModel> */
    fun <TModel> from(table: Class<TModel>): From<TModel> = From(this, table)

    /**
     * appends [.DISTINCT] to the query
     *
     * @return
     */
    fun distinct(): Select = selectQualifier(DISTINCT)

    override fun toString(): String = query


    /**
     * Helper method to pick the correct qualifier for a SELECT query
     *
     * @param qualifierInt Can be [.ALL], [.NONE], or [.DISTINCT]
     * @return
     */
    private fun selectQualifier(qualifierInt: Int) = apply {
        selectQualifier = qualifierInt
    }

    companion object {

        /**
         * Default does not include the qualifier
         */
        @JvmField
        val NONE = -1
        /**
         * SELECT DISTINCT call
         */
        @JvmField
        val DISTINCT = 0
        /**
         * SELECT ALL call
         */
        @JvmField
        val ALL = 1
    }
}
