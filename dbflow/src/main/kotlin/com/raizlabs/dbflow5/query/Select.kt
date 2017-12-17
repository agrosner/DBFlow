package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.property.IProperty
import com.raizlabs.dbflow5.query.property.Property
import com.raizlabs.dbflow5.sql.Query
import com.raizlabs.dbflow5.sql.QueryCloneable
import kotlin.reflect.KClass

/**
 * Description: A SQL SELECT statement generator. It generates the SELECT part of the statement.
 */
class Select
/**
 * Creates this instance with the specified columns from the specified [com.raizlabs.android.dbflow.config.FlowManager]
 *
 * @param properties The properties to select from.
 */
internal constructor(vararg properties: IProperty<*>) : Query, QueryCloneable<Select> {
    /**
     * The select qualifier to append to the SELECT statement
     */
    private var selectQualifier = NONE

    private val propertyList = arrayListOf<IProperty<*>>()

    override val query: String
        get() {
            val queryBuilder = StringBuilder("SELECT ")

            if (selectQualifier != NONE) {
                if (selectQualifier == DISTINCT) {
                    queryBuilder.append("DISTINCT")
                } else if (selectQualifier == ALL) {
                    queryBuilder.append("ALL")
                }
                queryBuilder.append(" ")
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
     * @param [T] The class that implements [com.raizlabs.android.dbflow.structure.Model]
     * @return the From part of this query
     */
    infix fun <T : Any> from(table: Class<T>): From<T> = From(this, table)

    inline fun <reified T : Any> from() = from(T::class.java)

    infix fun <T : Any> from(table: KClass<T>) = from(table.java)

    /**
     * Constructs a [From] with a [ModelQueriable] expression.
     */
    fun <T : Any> from(modelQueriable: ModelQueriable<T>) = From(this, modelQueriable.table, modelQueriable)

    /**
     * appends [.DISTINCT] to the query
     *
     * @return
     */
    fun distinct(): Select = selectQualifier(DISTINCT)

    override fun toString(): String = query

    override fun cloneSelf(): Select = Select(*propertyList.toTypedArray())

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

inline val select: Select
    get() = select()
