package com.dbflow5.query

import com.dbflow5.annotation.Collate
import com.dbflow5.query.property.IProperty
import com.dbflow5.sql.Query

/**
 * Description: Class that represents a SQL order-by.
 */
class OrderBy
@JvmOverloads
constructor(private val column: NameAlias? = null,
            private var isAscending: Boolean = false) : Query {

    private var collation: Collate? = null
    private var orderByString: String?

    override val query: String
        get() {
            val locOrderByString = orderByString
            return if (locOrderByString == null) {
                val query = StringBuilder()
                    .append(column)
                    .append(" ")
                if (collation != null) {
                    query.append("COLLATE $collation ")
                }
                query.append(if (isAscending) ASCENDING else DESCENDING)
                query.toString()
            } else {
                locOrderByString
            }
        }

    internal constructor(orderByString: String) : this(column = null) {
        this.orderByString = orderByString
    }

    init {
        this.orderByString = null
    }

    fun ascending() = apply {
        isAscending = true
    }

    fun descending() = apply {
        isAscending = false
    }

    fun collate(collate: Collate) = apply {
        this.collation = collate
    }

    override fun toString(): String = query

    companion object {

        @JvmField
        val ASCENDING = "ASC"

        @JvmField
        val DESCENDING = "DESC"

        @JvmStatic
        fun fromProperty(property: IProperty<*>): OrderBy = OrderBy(property.nameAlias)

        @JvmStatic
        fun fromNameAlias(nameAlias: NameAlias): OrderBy = OrderBy(nameAlias)

        @JvmStatic
        fun fromString(orderByString: String): OrderBy = OrderBy(orderByString)
    }
}

infix fun OrderBy.collate(collate: Collate) = collate(collate)